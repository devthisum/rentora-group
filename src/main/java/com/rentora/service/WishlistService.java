package com.rentora.service;

import com.rentora.dao.impl.WishlistDAOImpl;
import com.rentora.dao.interfaces.WishlistDAO;
import com.rentora.model.Vehicle;

import java.util.List;

public class WishlistService {

    private final WishlistDAO wishlistDAO = new WishlistDAOImpl();

    public void add(long renterId, long vehicleId) throws Exception {
        wishlistDAO.add(renterId, vehicleId);
    }

    public void remove(long renterId, long vehicleId) throws Exception {
        wishlistDAO.remove(renterId, vehicleId);
    }

    /** Adds if not already saved, removes if it is — used by the heart-icon toggle button. */
    public boolean toggle(long renterId, long vehicleId) throws Exception {
        if (wishlistDAO.isFavorited(renterId, vehicleId)) {
            wishlistDAO.remove(renterId, vehicleId);
            return false; // now NOT favorited
        } else {
            wishlistDAO.add(renterId, vehicleId);
            return true; // now favorited
        }
    }

    public boolean isFavorited(long renterId, long vehicleId) throws Exception {
        return wishlistDAO.isFavorited(renterId, vehicleId);
    }

    public java.util.Set<Long> getFavoritedVehicleIds(long renterId) throws Exception {
        return wishlistDAO.findFavoritedVehicleIds(renterId);
    }

    /** sortBy: "price_asc", "price_desc", "rating", or null for most-recent-first. Optional keyword filters by brand/model. */
    public List<Vehicle> getSavedVehicles(long renterId, String sortBy, String keyword) throws Exception {
        List<Vehicle> vehicles = wishlistDAO.findByRenter(renterId, sortBy);
        if (keyword == null || keyword.isBlank()) return vehicles;

        String lowerKeyword = keyword.toLowerCase();
        return vehicles.stream()
                .filter(v -> (v.getBrand() + " " + v.getModel()).toLowerCase().contains(lowerKeyword))
                .toList();
    }
}
