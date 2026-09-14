package com.rentora.dao.impl;

import com.rentora.dao.interfaces.SavedPaymentMethodDAO;
import com.rentora.model.SavedPaymentMethod;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavedPaymentMethodDAOImpl implements SavedPaymentMethodDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String BASE_SELECT = "SELECT * FROM saved_payment_methods ";

    @Override
    public long create(SavedPaymentMethod method) throws Exception {
        String sql = "INSERT INTO saved_payment_methods (user_id, type, label, masked_number, expiry, is_default) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, method.getUserId());
            ps.setString(2, method.getType());
            ps.setString(3, method.getLabel());
            ps.setString(4, method.getMaskedNumber());
            ps.setString(5, method.getExpiry());
            ps.setBoolean(6, method.isDefault());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<SavedPaymentMethod> findById(long id) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE payment_method_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<SavedPaymentMethod> findByUser(long userId) throws Exception {
        List<SavedPaymentMethod> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE user_id = ? ORDER BY is_default DESC, created_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(SavedPaymentMethod method) throws Exception {
        String sql = "UPDATE saved_payment_methods SET label = ?, masked_number = ?, expiry = ? WHERE payment_method_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, method.getLabel());
            ps.setString(2, method.getMaskedNumber());
            ps.setString(3, method.getExpiry());
            ps.setLong(4, method.getPaymentMethodId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long id) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM saved_payment_methods WHERE payment_method_id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clearDefault(long userId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE saved_payment_methods SET is_default = FALSE WHERE user_id = ?")) {
            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        }
    }

    @Override
    public boolean setDefault(long id) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE saved_payment_methods SET is_default = TRUE WHERE payment_method_id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private SavedPaymentMethod mapRow(ResultSet rs) throws SQLException {
        SavedPaymentMethod m = new SavedPaymentMethod();
        m.setPaymentMethodId(rs.getLong("payment_method_id"));
        m.setUserId(rs.getLong("user_id"));
        m.setType(rs.getString("type"));
        m.setLabel(rs.getString("label"));
        m.setMaskedNumber(rs.getString("masked_number"));
        m.setExpiry(rs.getString("expiry"));
        m.setDefault(rs.getBoolean("is_default"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()));
        return m;
    }
}
