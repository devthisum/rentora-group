package com.rentora.dao.impl;

import com.rentora.dao.interfaces.UserDAO;
import com.rentora.model.User;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC implementation of UserDAO. Uses PreparedStatements exclusively (SQL-injection safe). */
public class UserDAOImpl implements UserDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String INSERT_USER =
            "INSERT INTO users (role_id, full_name, email, phone, password_hash, nic_number, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.user_id = ?";

    private static final String SELECT_BY_EMAIL =
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.email = ?";

    private static final String SELECT_ALL_BY_ROLE =
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE r.role_name = ? " +
            "ORDER BY u.created_at DESC";

    private static final String UPDATE_STATUS =
            "UPDATE users SET status = ? WHERE user_id = ?";

    private static final String UPDATE_USER =
            "UPDATE users SET full_name = ?, phone = ?, profile_image = ? WHERE user_id = ?";

    @Override
    public long create(User user) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user.getRoleId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getNicNumber());
            ps.setString(7, user.getStatus() == null ? "ACTIVE" : user.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<User> findById(long userId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws Exception {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<User> findAllByRole(String roleName) throws Exception {
        List<User> users = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_BY_ROLE)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapRow(rs));
            }
        }
        return users;
    }

    @Override
    public boolean updateStatus(long userId, String status) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(User user) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getProfileImage());
            ps.setLong(4, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateCheckoutInfo(long userId, String addressStreet, String addressCity, String addressPostalCode, String drivingLicenseNumber) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET address_street = ?, address_city = ?, address_postal_code = ?, driving_license_number = ? WHERE user_id = ?")) {
            ps.setString(1, addressStreet);
            ps.setString(2, addressCity);
            ps.setString(3, addressPostalCode);
            ps.setString(4, drivingLicenseNumber);
            ps.setLong(5, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(long userId, String newPasswordHash) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long userId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int findRoleIdByName(String roleName) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT role_id FROM roles WHERE role_name = ?")) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("role_id") : -1;
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setRoleId(rs.getInt("role_id"));
        user.setRoleName(rs.getString("role_name"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setNicNumber(rs.getString("nic_number"));
        user.setAddressStreet(rs.getString("address_street"));
        user.setAddressCity(rs.getString("address_city"));
        user.setAddressPostalCode(rs.getString("address_postal_code"));
        user.setDrivingLicenseNumber(rs.getString("driving_license_number"));
        user.setProfileImage(rs.getString("profile_image"));
        user.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        return user;
    }
}
