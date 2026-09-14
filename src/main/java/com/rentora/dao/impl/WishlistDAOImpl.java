package com.rentora.dao.impl;

import com.rentora.dao.interfaces.WishlistDAO;
import com.rentora.model.Vehicle;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAOImpl implements WishlistDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    @Override
    public boolean add(long renterId, long vehicleId) throws Exception {
        // INSERT IGNORE respects the (renter_id, vehicle_id) unique key —
        // adding an already-favorited vehicle again is a harmless no-op.
        String sql = "INSERT IGNORE INTO wishlist (renter_id, vehicle_id) VALUES (?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, renterId);
            ps.setLong(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean remove(long renterId, long vehicleId) throws Exception {
        String sql = "DELETE FROM wishlist WHERE renter_id = ? AND vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, renterId);
            ps.setLong(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean isFavorited(long renterId, long vehicleId) throws Exception {
        String sql = "SELECT 1 FROM wishlist WHERE renter_id = ? AND vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, renterId);
            ps.setLong(2, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** sortBy: "price_asc", "price_desc", "rating", or defaults to most-recently-added first. */
    @Override
    public List<Vehicle> findByRenter(long renterId, String sortBy) throws Exception {
        String orderClause = switch (sortBy == null ? "" : sortBy) {
            case "price_asc" -> "v.price_per_day ASC";
            case "price_desc" -> "v.price_per_day DESC";
            case "rating" -> "v.average_rating DESC";
            default -> "w.added_at DESC";
        };

        String sql = "SELECT v.*, c.category_name, " +
                "EXISTS(SELECT 1 FROM bookings bk WHERE bk.vehicle_id = v.vehicle_id " +
                "       AND bk.status IN ('CONFIRMED','ONGOING') " +
                "       AND CURDATE() BETWEEN bk.start_date AND bk.end_date) AS booked_today " +
                "FROM wishlist w " +
                "JOIN vehicles v ON w.vehicle_id = v.vehicle_id " +
                "JOIN vehicle_categories c ON v.category_id = c.category_id " +
                "WHERE w.renter_id = ? ORDER BY " + orderClause;

        List<Vehicle> results = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, renterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setVehicleId(rs.getLong("vehicle_id"));
                    v.setCategoryName(rs.getString("category_name"));
                    v.setVehicleNumber(rs.getString("vehicle_number"));
                    v.setBrand(rs.getString("brand"));
                    v.setModel(rs.getString("model"));
                    v.setYear(rs.getInt("year"));
                    v.setSeats(rs.getInt("seats"));
                    v.setTransmission(rs.getString("transmission"));
                    v.setFuelType(rs.getString("fuel_type"));
                    v.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    v.setDescription(rs.getString("description"));
                    v.setImageUrl(rs.getString("image_url"));
                    v.setStatus(rs.getString("status"));
                    v.setAverageRating(rs.getDouble("average_rating"));
                    v.setBookedToday(rs.getBoolean("booked_today"));
                    results.add(v);
                }
            }
        }
        return results;
    }

    @Override
    public java.util.Set<Long> findFavoritedVehicleIds(long renterId) throws Exception {
        java.util.Set<Long> ids = new java.util.HashSet<>();
        String sql = "SELECT vehicle_id FROM wishlist WHERE renter_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, renterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("vehicle_id"));
            }
        }
        return ids;
    }
}
