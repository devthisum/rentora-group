package com.rentora.dao.interfaces;

import java.util.List;

public interface VehicleImageDAO {
    long addImage(long vehicleId, String imageUrl, boolean isPrimary) throws Exception;
    List<String> findByVehicle(long vehicleId) throws Exception;
    /** Full rows (id + url + primary flag), primary first — used by the admin edit screen. */
    List<com.rentora.model.VehicleImage> findRowsByVehicle(long vehicleId) throws Exception;
    /** Deletes one photo, scoped to its vehicle so an id from another vehicle can't be removed. */
    boolean deleteImage(long imageId, long vehicleId) throws Exception;
    /** Removes the primary (cover) row(s) so the cover can be re-synced after it changes. */
    void deletePrimary(long vehicleId) throws Exception;
}
