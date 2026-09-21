package com.rentora.dao.impl;

import com.rentora.dao.interfaces.PasswordResetOtpDAO;
import com.rentora.model.PasswordResetOtp;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class PasswordResetOtpDAOImpl implements PasswordResetOtpDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    @Override
    public long create(PasswordResetOtp otp) throws Exception {
        String sql = "INSERT INTO password_reset_otps (user_id, otp_code, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, otp.getUserId());
            ps.setString(2, otp.getOtpCode());
            ps.setTimestamp(3, Timestamp.valueOf(otp.getExpiresAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<PasswordResetOtp> findLatestByUserAndCode(long userId, String otpCode) throws Exception {
        String sql = "SELECT * FROM password_reset_otps WHERE user_id = ? AND otp_code = ? " +
                     "ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, otpCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                PasswordResetOtp otp = new PasswordResetOtp();
                otp.setOtpId(rs.getLong("otp_id"));
                otp.setUserId(rs.getLong("user_id"));
                otp.setOtpCode(rs.getString("otp_code"));
                otp.setExpiresAt(LocalDateTime.ofInstant(rs.getTimestamp("expires_at").toInstant(), ZoneId.systemDefault()));
                otp.setUsed(rs.getBoolean("used"));
                Timestamp createdTs = rs.getTimestamp("created_at");
                if (createdTs != null) otp.setCreatedAt(LocalDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault()));
                return Optional.of(otp);
            }
        }
    }

    @Override
    public boolean markUsed(long otpId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE password_reset_otps SET used = TRUE WHERE otp_id = ?")) {
            ps.setLong(1, otpId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void invalidatePreviousForUser(long userId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE password_reset_otps SET used = TRUE WHERE user_id = ? AND used = FALSE")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
}
