package com.rentora.service;

import com.rentora.dao.impl.BookingDAOImpl;
import com.rentora.dao.impl.MaintenanceDAOImpl;
import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.BookingDAO;
import com.rentora.dao.interfaces.MaintenanceDAO;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Booking;
import com.rentora.model.MaintenanceRecord;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.observer.NotificationEvent;
import com.rentora.observer.NotificationSubject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implements the shop's post-rental maintenance workflow:
 *
 *   1. Vehicle is returned -> booking marked RETURNED, vehicle goes to CHECKING,
 *      a maintenance record is opened and shown on the admin maintenance board.
 *   2. Admin/staff inspects the vehicle:
 *      - No problem found -> vehicle goes back to AVAILABLE, booking COMPLETED,
 *        vehicle disappears from the maintenance board.
 *      - Problem found -> vehicle goes to MAINTENANCE, admin records how many
 *        days it will be under maintenance and (optionally) a repair cost.
 *        All admins are notified "Vehicle {number} is under maintenance for
 *        {days} days".
 *   3. If the customer caused the damage, the repair cost gets a 20% surcharge
 *      and the customer is notified they must pay the total within 24 hours.
 *   4. Once repairs are done, admin resolves the record and the vehicle goes
 *      back to AVAILABLE.
 */
public class MaintenanceService {

    /** Customer-at-fault repairs are billed at cost + this percentage. */
    private static final BigDecimal CUSTOMER_FAULT_SURCHARGE_PCT = new BigDecimal("20.00");
    private static final int PAYMENT_WINDOW_HOURS = 24;

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final NotificationSubject notificationSubject;

    public MaintenanceService(NotificationSubject notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    /** Step 1: admin/booking-staff marks a rented-out vehicle as returned. Opens the CHECKING record and calculates any late fee. */
    public long markReturned(long bookingId) throws Exception {
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty()) {
            throw new ValidationException("Booking not found.");
        }
        Booking booking = maybeBooking.get();
        if (!"CONFIRMED".equals(booking.getStatus()) && !"ONGOING".equals(booking.getStatus())) {
            throw new ValidationException("This booking isn't currently out on rent.");
        }

        BigDecimal lateFee = calculateLateFee(booking);

        bookingDAO.markReturned(bookingId, lateFee);
        vehicleDAO.updateStatus(booking.getVehicleId(), "CHECKING");

        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicleId(booking.getVehicleId());
        record.setBookingId(bookingId);
        record.setStage("CHECKING");
        return maintenanceDAO.create(record);
    }

    /**
     * 30% of the vehicle's daily rental price for every day the vehicle comes
     * back late (today vs the booked end date) — calculated the moment a
     * return is confirmed, since that's the first point the actual return
     * date is known. Returns zero if it's on time or early.
     */
    private BigDecimal calculateLateFee(Booking booking) throws Exception {
        long lateDays = java.time.temporal.ChronoUnit.DAYS.between(booking.getEndDate(), java.time.LocalDate.now());
        if (lateDays <= 0) {
            return BigDecimal.ZERO;
        }
        Optional<Vehicle> vehicle = vehicleDAO.findById(booking.getVehicleId());
        if (vehicle.isEmpty()) {
            return BigDecimal.ZERO; // shouldn't happen, but don't block confirming a return over it
        }
        BigDecimal perDayLateFee = vehicle.get().getPricePerDay().multiply(new BigDecimal("0.30"));
        return perDayLateFee.multiply(BigDecimal.valueOf(lateDays)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Step 2/3: admin completes the inspection.
     *
     * @param problemFound   whether an issue was found on inspection
     * @param notes          inspection notes (what was checked / what's wrong)
     * @param estimatedDays  only used when a problem was found — how many days the repair will take
     * @param repairCost     optional repair cost; when present and customerAtFault, a 20% surcharge is added
     * @param customerAtFault whether the renter caused the damage (drives the surcharge + who pays)
     */
    public void completeCheck(long maintenanceId, long adminUserId, boolean problemFound, String notes,
                               Integer estimatedDays, BigDecimal repairCost, boolean customerAtFault) throws Exception {

        Optional<MaintenanceRecord> maybeRecord = maintenanceDAO.findById(maintenanceId);
        if (maybeRecord.isEmpty()) {
            throw new ValidationException("Maintenance record not found.");
        }
        MaintenanceRecord record = maybeRecord.get();
        record.setProblemFound(problemFound);
        record.setNotes(notes);
        record.setCheckedBy(adminUserId);

        if (!problemFound) {
            // No problem found -> vehicle goes back into service, booking is complete,
            // vehicle no longer shows up under maintenance.
            record.setStage("OK");
            vehicleDAO.updateStatus(record.getVehicleId(), "AVAILABLE");
            if (record.getBookingId() != null) {
                bookingDAO.updateStatus(record.getBookingId(), "COMPLETED");
            }
            maintenanceDAO.update(record);
            return;
        }

        // Problem found -> vehicle stays out of service under maintenance.
        record.setEstimatedDays(estimatedDays);
        vehicleDAO.updateStatus(record.getVehicleId(), "MAINTENANCE");

        Optional<Vehicle> vehicle = vehicleDAO.findById(record.getVehicleId());
        String vehicleNumber = vehicle.map(Vehicle::getVehicleNumber).orElse("#" + record.getVehicleId());
        String days = estimatedDays != null ? String.valueOf(estimatedDays) : "an unspecified number of";

        if (repairCost != null) {
            record.setRepairCost(repairCost);
            record.setCustomerAtFault(customerAtFault);

            if (customerAtFault) {
                BigDecimal extraAmount = repairCost
                        .multiply(CUSTOMER_FAULT_SURCHARGE_PCT)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal total = repairCost.add(extraAmount);
                LocalDateTime dueAt = LocalDateTime.now().plusHours(PAYMENT_WINDOW_HOURS);

                record.setExtraChargePct(CUSTOMER_FAULT_SURCHARGE_PCT);
                record.setExtraChargeAmount(extraAmount);
                record.setTotalCharge(total);
                record.setPaymentDueAt(dueAt);
                record.setStage("AWAITING_CUSTOMER_PAYMENT");

                // Notify the customer: repair cost + 20% surcharge is due within 24 hours.
                if (record.getRenterId() != null) {
                    notificationSubject.notifyAll(new NotificationEvent(
                            record.getRenterId(),
                            "Vehicle Damage Charge",
                            String.format(
                                    "Vehicle %s was found damaged on return. Repair cost is Rs. %.2f plus a 20%% " +
                                    "damage surcharge (Rs. %.2f) = Rs. %.2f total, due within 24 hours.",
                                    vehicleNumber, repairCost, extraAmount, total)));
                }
            } else {
                record.setStage("UNDER_MAINTENANCE");
            }
        } else {
            record.setStage("UNDER_MAINTENANCE");
        }

        maintenanceDAO.update(record);

        // Notify every admin: "Vehicle {number} is under maintenance for {days} days."
        for (User admin : userDAO.findAllByRole("ADMIN")) {
            notificationSubject.notifyAll(new NotificationEvent(
                    admin.getUserId(),
                    "Vehicle Under Maintenance",
                    String.format("Vehicle %s is under maintenance for %s day(s). %s",
                            vehicleNumber, days, notes != null ? notes : "")));
        }
    }

    /**
     * Lets maintenance staff edit the estimated duration, notes, repair cost,
     * or fault determination at any point while a record is still open —
     * not just once at the initial inspection. Recalculates the customer
     * surcharge/total/due-by if the cost or fault status changed, and this
     * also reshapes the vehicle's calendar buffer (see VehicleCalendarServlet):
     * shrinking estimatedDays below 2 pulls the red buffer in, extending it
     * beyond 2 pushes the buffer further out.
     */
    public void updateInProgress(long maintenanceId, Integer estimatedDays, String notes,
                                  BigDecimal repairCost, boolean customerAtFault) throws Exception {
        Optional<MaintenanceRecord> maybeRecord = maintenanceDAO.findById(maintenanceId);
        if (maybeRecord.isEmpty()) {
            throw new ValidationException("Maintenance record not found.");
        }
        MaintenanceRecord record = maybeRecord.get();
        if (!List.of("CHECKING", "UNDER_MAINTENANCE", "AWAITING_CUSTOMER_PAYMENT").contains(record.getStage())) {
            throw new ValidationException("This maintenance record is already closed and can't be edited.");
        }

        record.setEstimatedDays(estimatedDays);
        record.setNotes(notes);
        record.setRepairCost(repairCost);
        record.setCustomerAtFault(customerAtFault);

        if (repairCost != null && customerAtFault) {
            BigDecimal extraAmount = repairCost
                    .multiply(CUSTOMER_FAULT_SURCHARGE_PCT)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            record.setExtraChargePct(CUSTOMER_FAULT_SURCHARGE_PCT);
            record.setExtraChargeAmount(extraAmount);
            record.setTotalCharge(repairCost.add(extraAmount));
            if (record.getPaymentDueAt() == null) {
                record.setPaymentDueAt(LocalDateTime.now().plusHours(PAYMENT_WINDOW_HOURS));
            }
            if (!"AWAITING_CUSTOMER_PAYMENT".equals(record.getStage())) {
                record.setStage("AWAITING_CUSTOMER_PAYMENT");
            }
        } else {
            record.setExtraChargePct(null);
            record.setExtraChargeAmount(null);
            record.setTotalCharge(repairCost);
            record.setPaymentDueAt(null);
            if ("AWAITING_CUSTOMER_PAYMENT".equals(record.getStage())) {
                record.setStage("UNDER_MAINTENANCE");
            }
        }

        maintenanceDAO.update(record);
    }

    /** Admin marks repairs finished — vehicle goes back into service. */
    public void markResolved(long maintenanceId) throws Exception {
        Optional<MaintenanceRecord> maybeRecord = maintenanceDAO.findById(maintenanceId);
        if (maybeRecord.isEmpty()) {
            throw new ValidationException("Maintenance record not found.");
        }
        MaintenanceRecord record = maybeRecord.get();
        record.setStage("RESOLVED");
        maintenanceDAO.update(record);
        vehicleDAO.updateStatus(record.getVehicleId(), "AVAILABLE");

        // The rental episode is now fully finished from the customer's side too —
        // without this, a booking that had a problem found on inspection would
        // never reach COMPLETED, and the renter could never leave a review.
        if (record.getBookingId() != null) {
            bookingDAO.updateStatus(record.getBookingId(), "COMPLETED");
        }
    }

    /** Customer pays the outstanding damage charge. */
    public void payCharge(long maintenanceId) throws Exception {
        Optional<MaintenanceRecord> maybeRecord = maintenanceDAO.findById(maintenanceId);
        if (maybeRecord.isEmpty()) {
            throw new ValidationException("Maintenance record not found.");
        }
        MaintenanceRecord record = maybeRecord.get();
        record.setChargePaid(true);
        maintenanceDAO.update(record);
    }

    /**
     * Manually sets a vehicle's status (Available / Checking / Maintenance)
     * from a quick-action dropdown — used by both admin and maintenance
     * staff outside the normal "return a booking" flow (e.g. proactively
     * pulling a car in for service). Unlike a raw status flip, this keeps
     * the maintenance board in sync:
     *  - Checking/Maintenance opens (or reuses) a real MaintenanceRecord,
     *    so the vehicle immediately shows up on the maintenance dashboard
     *    with an editable estimated-days timeline, exactly like a record
     *    opened through the normal return workflow.
     *  - Available resolves that open record properly (so it doesn't stay
     *    stuck on the maintenance board) rather than just hiding the
     *    mismatch.
     *
     * @param estimatedDays only meaningful when targetStatus is MAINTENANCE
     * @param notes         optional note explaining the manual change
     */
    public void setVehicleStatus(long vehicleId, String targetStatus, Integer estimatedDays, String notes) throws Exception {
        if (!List.of("AVAILABLE", "CHECKING", "MAINTENANCE").contains(targetStatus)) {
            throw new ValidationException("Invalid vehicle status.");
        }
        Optional<Vehicle> vehicle = vehicleDAO.findById(vehicleId);
        if (vehicle.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }

        Optional<MaintenanceRecord> openRecord = findOpenRecord(vehicleId);

        if ("AVAILABLE".equals(targetStatus)) {
            if (vehicle.get().isBookedToday()) {
                throw new ValidationException("This vehicle is out on an active booking today — it can't be marked Available manually.");
            }
            if (openRecord.isPresent()) {
                MaintenanceRecord record = openRecord.get();
                if ("AWAITING_CUSTOMER_PAYMENT".equals(record.getStage()) && !record.isChargePaid()) {
                    throw new ValidationException(
                            "This vehicle has an unpaid damage charge awaiting payment — confirm it from the Maintenance dashboard instead.");
                }
                markResolved(record.getMaintenanceId());
            } else {
                vehicleDAO.updateStatus(vehicleId, "AVAILABLE");
            }
            return;
        }

        // targetStatus is CHECKING or MAINTENANCE
        String stage = "CHECKING".equals(targetStatus) ? "CHECKING" : "UNDER_MAINTENANCE";

        if (openRecord.isPresent()) {
            // Already has an open record for this vehicle — move/update it rather than creating a duplicate.
            MaintenanceRecord record = openRecord.get();
            record.setStage(stage);
            if ("UNDER_MAINTENANCE".equals(stage)) record.setEstimatedDays(estimatedDays);
            if (notes != null && !notes.isBlank()) record.setNotes(notes);
            maintenanceDAO.update(record);
        } else {
            MaintenanceRecord record = new MaintenanceRecord();
            record.setVehicleId(vehicleId);
            record.setStage(stage);
            if ("UNDER_MAINTENANCE".equals(stage)) record.setEstimatedDays(estimatedDays);
            record.setNotes(notes);
            maintenanceDAO.create(record);
        }
        vehicleDAO.updateStatus(vehicleId, targetStatus);
    }

    private Optional<MaintenanceRecord> findOpenRecord(long vehicleId) throws Exception {
        for (String stage : List.of("CHECKING", "UNDER_MAINTENANCE", "AWAITING_CUSTOMER_PAYMENT")) {
            Optional<MaintenanceRecord> r = maintenanceDAO.findByVehicleAndStage(vehicleId, stage);
            if (r.isPresent()) return r;
        }
        return Optional.empty();
    }

    /** Everything currently open — feeds the admin maintenance dashboard. */
    public List<MaintenanceRecord> getActive() throws Exception {
        return maintenanceDAO.findActive();
    }

    public Optional<MaintenanceRecord> getById(long maintenanceId) throws Exception {
        return maintenanceDAO.findById(maintenanceId);
    }

    /** The maintenance record (if any) tied to a specific booking's return — used to size the calendar's red buffer. */
    public Optional<MaintenanceRecord> getByBookingId(long bookingId) throws Exception {
        return maintenanceDAO.findByBookingId(bookingId);
    }

    public List<MaintenanceRecord> getHistoryForVehicle(long vehicleId) throws Exception {
        return maintenanceDAO.findByVehicle(vehicleId);
    }
}
