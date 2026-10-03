package com.rentora.dao.impl;

import com.rentora.dao.interfaces.VehicleImageDAO;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleImageDAOImpl implements VehicleImageDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    @Override
    public long addImage(long vehicleId, String imageUrl, boolean isPrimary) throws Exception {
        String sql = "INSERT INTO vehicle_images (vehicle_id, image_url, is_primary) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, vehicleId);
            ps.setString(2, imageUrl);
            ps.setBoolean(3, isPrimary);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public List<String> findByVehicle(long vehicleId) throws Exception {
        List<String> urls = new ArrayList<>();
        String sql = "SELECT image_url FROM vehicle_images WHERE vehicle_id = ? ORDER BY is_primary DESC, image_id ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) urls.add(rs.getString("image_url"));
            }
        }
        return urls;
    }

    @Override
    public List<com.rentora.model.VehicleImage> findRowsByVehicle(long vehicleId) throws Exception {
        List<com.rentora.model.VehicleImage> rows = new ArrayList<>();
        String sql = "SELECT image_id, vehicle_id, image_url, is_primary FROM vehicle_images WHERE vehicle_id = ? ORDER BY is_primary DESC, image_id ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.rentora.model.VehicleImage img = new com.rentora.model.VehicleImage();
                    img.setImageId(rs.getLong("image_id"));
                    img.setVehicleId(rs.getLong("vehicle_id"));
                    img.setImageUrl(rs.getString("image_url"));
                    img.setPrimary(rs.getBoolean("is_primary"));
                    rows.add(img);
                }
            }
        }
        return rows;
    }

    @Override
    public boolean deleteImage(long imageId, long vehicleId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM vehicle_images WHERE image_id = ? AND vehicle_id = ? AND is_primary = FALSE")) {
            ps.setLong(1, imageId);
            ps.setLong(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void deletePrimary(long vehicleId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM vehicle_images WHERE vehicle_id = ? AND is_primary = TRUE")) {
            ps.setLong(1, vehicleId);
            ps.executeUpdate();
        }
    }
}
