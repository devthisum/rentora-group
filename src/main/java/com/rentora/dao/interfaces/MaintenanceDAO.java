package com.rentora.dao.interfaces;

import com.rentora.model.MaintenanceRecord;
import java.util.List;
import java.util.Optional;

public interface MaintenanceDAO {
    long create(MaintenanceRecord record) throws Exception;
    Optional<MaintenanceRecord> findById(long maintenanceId) throws Exception;
    Optional<MaintenanceRecord> findByVehicleAndStage(long vehicleId, String stage) throws Exception;
    /** The maintenance record (if any) opened for a specific booking's return — used to size the calendar's red buffer. */
    Optional<MaintenanceRecord> findByBookingId(long bookingId) throws Exception;
    /** Everything currently open: CHECKING, UNDER_MAINTENANCE, AWAITING_CUSTOMER_PAYMENT — the admin maintenance board. */
    List<MaintenanceRecord> findActive() throws Exception;
    List<MaintenanceRecord> findByVehicle(long vehicleId) throws Exception;
    boolean update(MaintenanceRecord record) throws Exception;
}
