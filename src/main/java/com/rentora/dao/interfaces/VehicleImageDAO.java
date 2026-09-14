package com.rentora.dao.interfaces;

import java.util.List;

public interface VehicleImageDAO {
    long addImage(long vehicleId, String imageUrl, boolean isPrimary) throws Exception;
    List<String> findByVehicle(long vehicleId) throws Exception;
}
