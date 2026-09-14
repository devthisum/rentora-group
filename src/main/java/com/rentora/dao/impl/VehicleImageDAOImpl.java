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
}
