package com.rentora.service;

import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.factory.VehicleFactory;
import com.rentora.model.Vehicle;
import com.rentora.util.ValidationUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business logic for vehicle stock management. Admin/staff add and edit
 * vehicles directly — there is no separate owner account and no approval
 * workflow, since the shop owns everything in its catalog.
 */
public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    /**
     * Adds a new vehicle to the shop's stock. The Factory Pattern (VehicleFactory)
     * supplies sensible category-based defaults for seats/transmission/fuel type,
     * but any value the admin explicitly provided on the form takes precedence.
     * New vehicles are AVAILABLE immediately.
     */
    public long addVehicle(Vehicle input, long addedByAdminId) throws Exception {

        if (!ValidationUtil.isValidVehicleNumber(input.getVehicleNumber())) {
            throw new ValidationException("Invalid vehicle number format. Expected e.g. ABC-1234.");
        }
        if (!ValidationUtil.isValidPrice(input.getPricePerDay().doubleValue())) {
            throw new ValidationException("Price per day must be a positive, realistic amount.");
        }
        if (vehicleDAO.existsByVehicleNumber(input.getVehicleNumber())) {
            throw new ValidationException("A vehicle with this number is already registered.");
        }

        // Factory Pattern: category-aware defaults (seats, transmission, fuel type)
        Vehicle vehicle = VehicleFactory.createVehicle(input.getCategoryName());
        vehicle.setAddedBy(addedByAdminId);
        vehicle.setCategoryId(input.getCategoryId());
        vehicle.setVehicleNumber(input.getVehicleNumber());
        vehicle.setBrand(input.getBrand());
        vehicle.setModel(input.getModel());
        vehicle.setYear(input.getYear());
        vehicle.setPricePerDay(input.getPricePerDay());
        vehicle.setDescription(input.getDescription());
        vehicle.setImageUrl(input.getImageUrl());

        // Admin-specified values override the factory defaults when provided
        if (input.getSeats() > 0) vehicle.setSeats(input.getSeats());
        if (ValidationUtil.isNotBlank(input.getTransmission())) vehicle.setTransmission(input.getTransmission());
        if (ValidationUtil.isNotBlank(input.getFuelType())) vehicle.setFuelType(input.getFuelType());

        return vehicleDAO.create(vehicle);
    }

    public List<Vehicle> search(Map<String, String> filters) throws Exception {
        return vehicleDAO.search(filters);
    }

    public Optional<Vehicle> getById(long vehicleId) throws Exception {
        return vehicleDAO.findById(vehicleId);
    }

    /** All vehicles in the shop's stock, for the admin management screens. */
    public List<Vehicle> getAll() throws Exception {
        return vehicleDAO.findAll();
    }

    /** Every vehicle that is NOT archived — for staff screens that should ignore retired stock. */
    public List<Vehicle> getAllActive() throws Exception {
        return vehicleDAO.findAll().stream().filter(v -> !v.isArchived()).toList();
    }

    public boolean isAvailable(long vehicleId, java.time.LocalDate start, java.time.LocalDate end) throws Exception {
        if (!ValidationUtil.isValidDateRange(start, end)) {
            throw new ValidationException("Invalid booking date range.");
        }
        Optional<Vehicle> vehicle = vehicleDAO.findById(vehicleId);
        if (vehicle.isEmpty()) {
            return false;
        }
        if (vehicle.get().isArchived()) {
            return false; // retired from the shop — can't be booked
        }
        String status = vehicle.get().getStatus();
        // Being booked for OTHER dates doesn't make a vehicle unbookable — the
        // calendar/date-conflict check below handles that. Only a vehicle
        // that's physically out of service right now is a hard no.
        if ("CHECKING".equals(status) || "MAINTENANCE".equals(status)) {
            return false;
        }
        return vehicleDAO.isAvailable(vehicleId, start, end);
    }

    /** Admin edits a vehicle's details directly — no approval workflow. */
    public void updateVehicle(Vehicle changes) throws Exception {
        Optional<Vehicle> maybeExisting = vehicleDAO.findById(changes.getVehicleId());
        if (maybeExisting.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        if (!ValidationUtil.isValidPrice(changes.getPricePerDay().doubleValue())) {
            throw new ValidationException("Price per day must be a positive, realistic amount.");
        }
        vehicleDAO.update(changes);
    }

    private static final java.util.Set<String> VALID_STATUSES =
            java.util.Set.of("AVAILABLE", "CHECKING", "MAINTENANCE");

    /**
     * Manually sets a vehicle's status (Available / Checking / Maintenance).
     * Used by admin for direct corrections outside the normal
     * return-&-inspect workflow that MaintenanceService drives.
     * Blocked if the vehicle is out on an active booking today, since that
     * would let a car be marked "Available" while a renter still has it.
     */
    public void updateStatus(long vehicleId, String status) throws Exception {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new ValidationException("Invalid vehicle status.");
        }
        Optional<Vehicle> existing = vehicleDAO.findById(vehicleId);
        if (existing.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        if (existing.get().isBookedToday() && "AVAILABLE".equalsIgnoreCase(status)) {
            throw new ValidationException("This vehicle is out on an active booking today — it can't be marked Available manually.");
        }
        vehicleDAO.updateStatus(vehicleId, status.toUpperCase());
    }

    /** Admin removes a vehicle from the shop's stock. Blocked while it's out on rent or being checked/repaired. */
    public void deleteVehicle(long vehicleId) throws Exception {
        Optional<Vehicle> maybeExisting = vehicleDAO.findById(vehicleId);
        if (maybeExisting.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        String status = maybeExisting.get().getStatus();
        if ("BOOKED".equals(status) || "CHECKING".equals(status) || "MAINTENANCE".equals(status)) {
            throw new ValidationException("This vehicle can't be removed while it's booked or in maintenance.");
        }
        // A vehicle with ANY booking history is referenced by those records, so deleting it would
        // either fail or wipe history. Archiving keeps the records and hides it from customers.
        if (vehicleDAO.hasAnyBookings(vehicleId)) {
            throw new ValidationException("This vehicle has booking history, so it can't be deleted. "
                    + "Archive it instead — customers won't see it, but all records are kept.");
        }
        vehicleDAO.delete(vehicleId);
    }

    /**
     * Archives a vehicle: hidden from customers, history kept. Blocked while the vehicle is
     * tied up in a live booking or in the inspection/repair workflow, so nothing is orphaned.
     */
    public void archiveVehicle(long vehicleId) throws Exception {
        Optional<Vehicle> maybeExisting = vehicleDAO.findById(vehicleId);
        if (maybeExisting.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        Vehicle v = maybeExisting.get();
        if (v.isArchived()) {
            throw new ValidationException("This vehicle is already archived.");
        }
        if (vehicleDAO.hasActiveBookings(vehicleId)) {
            throw new ValidationException("This vehicle has unpaid, upcoming or ongoing bookings. "
                    + "Let those finish (or cancel them) before archiving it.");
        }
        if ("CHECKING".equals(v.getStatus()) || "MAINTENANCE".equals(v.getStatus())) {
            throw new ValidationException("Finish the inspection/repair first, then archive this vehicle.");
        }
        vehicleDAO.setArchived(vehicleId, true);
    }

    /** Brings an archived vehicle back into the shop's visible stock. */
    public void restoreVehicle(long vehicleId) throws Exception {
        Optional<Vehicle> maybeExisting = vehicleDAO.findById(vehicleId);
        if (maybeExisting.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        if (!maybeExisting.get().isArchived()) {
            throw new ValidationException("This vehicle isn't archived.");
        }
        vehicleDAO.setArchived(vehicleId, false);
    }
}
