package com.rentora.service;

import com.rentora.dao.impl.BookingDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.BookingDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.BookingConflictException;
import com.rentora.exception.ValidationException;
import com.rentora.model.Booking;
import com.rentora.model.Vehicle;
import com.rentora.observer.NotificationSubject;
import com.rentora.strategy.PaymentStrategy;
import com.rentora.util.ValidationUtil;
import com.rentora.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Core booking orchestration. Combines the Strategy pattern (fare
 * calculation) and Observer pattern (notifications) around the DAO layer.
 * Single-shop system — no pickup/return locations to track.
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private final PromotionService promotionService = new PromotionService();
    private final NotificationSubject notificationSubject;

    public BookingService(NotificationSubject notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    public long createBooking(long renterId, long vehicleId, LocalDate startDate, LocalDate endDate,
                               PaymentStrategy paymentStrategy) throws Exception {

        if (!ValidationUtil.isValidDateRange(startDate, endDate)) {
            throw new ValidationException("Invalid booking dates.");
        }

        Optional<Vehicle> maybeVehicle = vehicleDAO.findById(vehicleId);
        if (maybeVehicle.isEmpty()) {
            throw new ValidationException("This vehicle is not available for booking.");
        }
        Vehicle vehicle = maybeVehicle.get();
        // A vehicle already BOOKED for other dates can still be booked for its free
        // dates — the conflict check below is what actually protects overlapping
        // dates. Only a vehicle that's physically out of service is a hard no.
        if ("CHECKING".equals(vehicle.getStatus()) || "MAINTENANCE".equals(vehicle.getStatus())) {
            throw new ValidationException("This vehicle is currently in for maintenance and can't be booked.");
        }

        // Prevent double-booking / overlapping date ranges
        if (bookingDAO.hasDateConflict(vehicleId, startDate, endDate)) {
            throw new BookingConflictException("This vehicle is already booked for the selected dates.");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        // Uses the discounted price if an active staff promotion applies to this
        // vehicle right now — the discount is real money off, not just a UI badge.
        BigDecimal effectivePricePerDay = promotionService.getEffectivePricePerDay(vehicle);
        BigDecimal total = paymentStrategy.calculateTotal(effectivePricePerDay, days);

        Booking booking = new Booking();
        booking.setRenterId(renterId);
        booking.setVehicleId(vehicleId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalAmount(total);

        // Notifications to admin are sent once payment actually succeeds (see PaymentService).
        // A booking that's created but never paid for shouldn't alert anyone.

        return bookingDAO.create(booking);
    }

    /** A renter cancels their own still-unpaid booking, freeing the vehicle back up. */
    public void cancelBooking(long bookingId, long renterId) throws Exception {
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renterId) {
            throw new ValidationException("You don't have permission to cancel this booking.");
        }
        if (!"AWAITING_PAYMENT".equals(maybeBooking.get().getStatus())) {
            throw new ValidationException("Only unpaid bookings can be cancelled this way.");
        }
        bookingDAO.updateStatus(bookingId, "CANCELLED");
    }

    /**
     * Lets a renter change their still-unpaid booking's dates before paying —
     * re-validates the new range is conflict-free and recalculates the total
     * with the same fare strategy (card fee / wallet discount) as booking.
     */
    public void updateBookingDates(long bookingId, long renterId, LocalDate newStart, LocalDate newEnd,
                                    PaymentStrategy paymentStrategy) throws Exception {
        if (!ValidationUtil.isValidDateRange(newStart, newEnd)) {
            throw new ValidationException("Invalid booking dates.");
        }
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty() || maybeBooking.get().getRenterId() != renterId) {
            throw new ValidationException("You don't have permission to edit this booking.");
        }
        Booking booking = maybeBooking.get();
        if (!"AWAITING_PAYMENT".equals(booking.getStatus())) {
            throw new ValidationException("This booking can no longer be edited.");
        }
        if (bookingDAO.hasDateConflict(booking.getVehicleId(), newStart, newEnd, bookingId)) {
            throw new BookingConflictException("This vehicle is already booked for those dates.");
        }

        Optional<Vehicle> maybeVehicle = vehicleDAO.findById(booking.getVehicleId());
        if (maybeVehicle.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        long days = ChronoUnit.DAYS.between(newStart, newEnd) + 1;
        BigDecimal newTotal = paymentStrategy.calculateTotal(maybeVehicle.get().getPricePerDay(), days);

        bookingDAO.updateDates(bookingId, newStart, newEnd, newTotal);
    }

    /**
     * Auto-cancels any booking still AWAITING_PAYMENT more than 10 minutes after
     * it was created, freeing those dates back up. Called every minute by the
     * scheduled job set up in AppContextListener.
     */
    public void cancelExpiredBookings() throws Exception {
        List<Booking> expired = bookingDAO.findExpiredAwaitingPayment(java.time.LocalDateTime.now().minusMinutes(10));
        for (Booking b : expired) {
            bookingDAO.updateStatus(b.getBookingId(), "CANCELLED");
        }
    }

    /**
     * Bookings paid for (CONFIRMED) whose pickup date has arrived (or passed)
     * but the customer hasn't been checked in yet — Booking Staff's queue for
     * "confirm pickup" / "mark no-show".
     */
    public List<Booking> getTodaysPickups() throws Exception {
        return bookingDAO.findPickupsDue();
    }

    /**
     * Booking Staff confirms the customer has physically shown up and taken
     * the vehicle. This is the moment the vehicle actually becomes
     * unavailable on the shop floor — a paid-but-not-yet-picked-up (CONFIRMED)
     * booking deliberately does NOT block the vehicle's displayed status, so
     * a no-show doesn't leave it stuck looking "Booked" all day.
     */
    public void confirmPickup(long bookingId) throws Exception {
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty()) {
            throw new ValidationException("Booking not found.");
        }
        if (!"CONFIRMED".equals(maybeBooking.get().getStatus())) {
            throw new ValidationException("This booking isn't awaiting pickup.");
        }
        bookingDAO.updateStatus(bookingId, "ONGOING");
    }

    /**
     * Booking Staff records that a customer never showed up to collect their
     * paid booking — cancels it, freeing those dates for someone else too
     * (not just the shop-floor status, which is already unaffected by a
     * CONFIRMED-but-not-picked-up booking).
     */
    public void markNoShow(long bookingId) throws Exception {
        Optional<Booking> maybeBooking = bookingDAO.findById(bookingId);
        if (maybeBooking.isEmpty()) {
            throw new ValidationException("Booking not found.");
        }
        Booking booking = maybeBooking.get();
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ValidationException("This booking isn't awaiting pickup.");
        }
        if (booking.getStartDate().isAfter(LocalDate.now())) {
            throw new ValidationException("This booking's pickup date hasn't arrived yet.");
        }
        bookingDAO.updateStatus(bookingId, "CANCELLED");
    }

    public List<Booking> getByRenter(long renterId) throws Exception {
        return bookingDAO.findByRenter(renterId);
    }

    /** All not-yet-finished bookings for one vehicle — feeds the availability calendar. */
    public List<Booking> getActiveByVehicle(long vehicleId) throws Exception {
        return bookingDAO.findActiveByVehicle(vehicleId);
    }

    public List<Booking> getAll() throws Exception {
        return bookingDAO.findAll();
    }

    /** Currently-out rentals ordered by soonest due back — "return in ___ days" on the maintenance board. */
    public List<Booking> getActiveOrderedByReturn() throws Exception {
        return bookingDAO.findActiveOrderedByReturn();
    }

    /** Just the ONGOING ones (customer has actually picked the vehicle up) — Booking Staff's "confirm return" queue. */
    public List<Booking> getOngoingOrderedByReturn() throws Exception {
        return bookingDAO.findActiveOrderedByReturn().stream()
                .filter(b -> "ONGOING".equals(b.getStatus()))
                .toList();
    }

    public Optional<Booking> getById(long bookingId) throws Exception {
        return bookingDAO.findById(bookingId);
    }
}
