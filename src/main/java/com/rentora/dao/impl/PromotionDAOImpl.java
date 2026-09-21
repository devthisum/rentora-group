package com.rentora.dao.impl;

import com.rentora.dao.interfaces.PromotionDAO;
import com.rentora.model.Promotion;
import com.rentora.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromotionDAOImpl implements PromotionDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String BASE_SELECT =
            "SELECT p.*, v.brand AS vehicle_brand, v.model AS vehicle_model, v.vehicle_number, " +
            "v.price_per_day AS vehicle_price_per_day, u.full_name AS created_by_name " +
            "FROM promotions p " +
            "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
            "JOIN users u ON p.created_by = u.user_id ";

    @Override
    public long create(Promotion promotion) throws Exception {
        String sql = "INSERT INTO promotions (vehicle_id, title, description, discount_type, discount_value, " +
                     "start_date, end_date, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, promotion.getVehicleId());
            ps.setString(2, promotion.getTitle());
            ps.setString(3, promotion.getDescription());
            ps.setString(4, promotion.getDiscountType());
            ps.setBigDecimal(5, promotion.getDiscountValue());
            ps.setDate(6, Date.valueOf(promotion.getStartDate()));
            ps.setDate(7, Date.valueOf(promotion.getEndDate()));
            ps.setString(8, promotion.getStatus());
            ps.setLong(9, promotion.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public boolean update(Promotion promotion) throws Exception {
        String sql = "UPDATE promotions SET title=?, description=?, discount_type=?, discount_value=?, " +
                     "start_date=?, end_date=?, status=? WHERE promotion_id=?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, promotion.getTitle());
            ps.setString(2, promotion.getDescription());
            ps.setString(3, promotion.getDiscountType());
            ps.setBigDecimal(4, promotion.getDiscountValue());
            ps.setDate(5, Date.valueOf(promotion.getStartDate()));
            ps.setDate(6, Date.valueOf(promotion.getEndDate()));
            ps.setString(7, promotion.getStatus());
            ps.setLong(8, promotion.getPromotionId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long promotionId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM promotions WHERE promotion_id = ?")) {
            ps.setLong(1, promotionId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Promotion> findById(long promotionId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE p.promotion_id = ?")) {
            ps.setLong(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Promotion> findAll() throws Exception {
        List<Promotion> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " ORDER BY p.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Promotion> findByVehicle(long vehicleId) throws Exception {
        List<Promotion> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE p.vehicle_id = ? ORDER BY p.created_at DESC")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Optional<Promotion> findActiveForVehicle(long vehicleId) throws Exception {
        String sql = BASE_SELECT + " WHERE p.vehicle_id = ? AND p.status = 'ACTIVE' " +
                     "AND CURDATE() BETWEEN p.start_date AND p.end_date " +
                     "ORDER BY p.created_at DESC LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Promotion> findAllActive() throws Exception {
        String sql = BASE_SELECT + " WHERE p.status = 'ACTIVE' AND CURDATE() BETWEEN p.start_date AND p.end_date " +
                     "ORDER BY p.created_at DESC";
        List<Promotion> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setPromotionId(rs.getLong("promotion_id"));
        p.setVehicleId(rs.getLong("vehicle_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setDiscountType(rs.getString("discount_type"));
        p.setDiscountValue(rs.getBigDecimal("discount_value"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        p.setEndDate(rs.getDate("end_date").toLocalDate());
        p.setStatus(rs.getString("status"));
        p.setCreatedBy(rs.getLong("created_by"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) p.setCreatedAt(LocalDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault()));
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) p.setUpdatedAt(LocalDateTime.ofInstant(updatedTs.toInstant(), ZoneId.systemDefault()));
        p.setVehicleBrand(rs.getString("vehicle_brand"));
        p.setVehicleModel(rs.getString("vehicle_model"));
        p.setVehicleNumber(rs.getString("vehicle_number"));
        BigDecimal price = rs.getBigDecimal("vehicle_price_per_day");
        p.setVehiclePricePerDay(price != null ? price : BigDecimal.ZERO);
        p.setCreatedByName(rs.getString("created_by_name"));
        return p;
    }
}
