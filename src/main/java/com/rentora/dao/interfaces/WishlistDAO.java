package com.rentora.dao.interfaces;

import com.rentora.model.Vehicle;
import java.util.List;

public interface WishlistDAO {
    boolean add(long renterId, long vehicleId) throws Exception;
    boolean remove(long renterId, long vehicleId) throws Exception;
    boolean isFavorited(long renterId, long vehicleId) throws Exception;
    List<Vehicle> findByRenter(long renterId, String sortBy) throws Exception;
    java.util.Set<Long> findFavoritedVehicleIds(long renterId) throws Exception;
}
