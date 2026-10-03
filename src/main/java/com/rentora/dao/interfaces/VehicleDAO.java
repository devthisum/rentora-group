package com.rentora.dao.interfaces;

import com.rentora.model.Vehicle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VehicleDAO {
    long create(Vehicle vehicle) throws Exception;
    Optional<Vehicle> findById(long vehicleId) throws Exception;
    List<Vehicle> search(Map<String, String> filters) throws Exception;
    List<Vehicle> findAll() throws Exception;
    boolean updateStatus(long vehicleId, String status) throws Exception;
    boolean update(Vehicle vehicle) throws Exception;
    boolean delete(long vehicleId) throws Exception;
    boolean existsByVehicleNumber(String vehicleNumber) throws Exception;
    boolean isAvailable(long vehicleId, java.time.LocalDate start, java.time.LocalDate end) throws Exception;
    boolean updateAverageRating(long vehicleId, double averageRating) throws Exception;
    /** Hides (true) or restores (false) a vehicle for customers without touching any of its records. */
    boolean setArchived(long vehicleId, boolean archived) throws Exception;
    /** True if the vehicle has a booking that is unpaid, paid-but-not-picked-up, or currently out. */
    boolean hasActiveBookings(long vehicleId) throws Exception;
    /** True if the vehicle has ANY booking, past or present — such a vehicle can be archived but never deleted. */
    boolean hasAnyBookings(long vehicleId) throws Exception;
}
