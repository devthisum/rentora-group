package com.rentora.dao.impl;

import com.rentora.dao.interfaces.InquiryDAO;
import com.rentora.model.InquiryMessage;
import com.rentora.model.InquiryThread;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InquiryDAOImpl implements InquiryDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String THREAD_BASE_SELECT =
            "SELECT t.*, v.brand AS vehicle_brand, v.model AS vehicle_model, " +
            "ur.full_name AS renter_name " +
            "FROM inquiry_threads t " +
            "JOIN vehicles v ON t.vehicle_id = v.vehicle_id " +
            "JOIN users ur ON t.renter_id = ur.user_id ";

    @Override
    public long createThread(InquiryThread thread) throws Exception {
        String sql = "INSERT INTO inquiry_threads (vehicle_id, renter_id, status) VALUES (?, ?, 'OPEN')";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, thread.getVehicleId());
            ps.setLong(2, thread.getRenterId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<InquiryThread> findExistingThread(long vehicleId, long renterId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     THREAD_BASE_SELECT + " WHERE t.vehicle_id = ? AND t.renter_id = ?")) {
            ps.setLong(1, vehicleId);
            ps.setLong(2, renterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapThread(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<InquiryThread> findThreadById(long threadId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(THREAD_BASE_SELECT + " WHERE t.thread_id = ?")) {
            ps.setLong(1, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapThread(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<InquiryThread> findThreadsByRenter(long renterId) throws Exception {
        List<InquiryThread> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     THREAD_BASE_SELECT + " WHERE t.renter_id = ? ORDER BY t.created_at DESC")) {
            ps.setLong(1, renterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapThread(rs));
            }
        }
        return list;
    }

    @Override
    public List<InquiryThread> findAllThreads() throws Exception {
        List<InquiryThread> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(THREAD_BASE_SELECT + " ORDER BY t.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapThread(rs));
        }
        return list;
    }

    @Override
    public long addMessage(InquiryMessage message) throws Exception {
        String sql = "INSERT INTO inquiry_messages (thread_id, sender_id, message) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, message.getThreadId());
            ps.setLong(2, message.getSenderId());
            ps.setString(3, message.getMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public List<InquiryMessage> findMessagesByThread(long threadId) throws Exception {
        List<InquiryMessage> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name AS sender_name FROM inquiry_messages m " +
                "JOIN users u ON m.sender_id = u.user_id WHERE m.thread_id = ? ORDER BY m.created_at ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InquiryMessage m = new InquiryMessage();
                    m.setMessageId(rs.getLong("message_id"));
                    m.setThreadId(rs.getLong("thread_id"));
                    m.setSenderId(rs.getLong("sender_id"));
                    m.setMessage(rs.getString("message"));
                    m.setRead(rs.getBoolean("is_read"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) m.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
                    m.setSenderName(rs.getString("sender_name"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    /** Marks every message in the thread NOT sent by the reader as read (i.e. "I've read the other person's messages"). */
    @Override
    public boolean markMessagesRead(long threadId, long readerId) throws Exception {
        String sql = "UPDATE inquiry_messages SET is_read = TRUE WHERE thread_id = ? AND sender_id != ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, threadId);
            ps.setLong(2, readerId);
            return ps.executeUpdate() >= 0;
        }
    }

    @Override
    public boolean hasUnreadMessages(long threadId, long forUserId) throws Exception {
        String sql = "SELECT 1 FROM inquiry_messages WHERE thread_id = ? AND sender_id != ? AND is_read = FALSE LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, threadId);
            ps.setLong(2, forUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private InquiryThread mapThread(ResultSet rs) throws SQLException {
        InquiryThread t = new InquiryThread();
        t.setThreadId(rs.getLong("thread_id"));
        t.setVehicleId(rs.getLong("vehicle_id"));
        t.setRenterId(rs.getLong("renter_id"));
        t.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        t.setVehicleBrand(rs.getString("vehicle_brand"));
        t.setVehicleModel(rs.getString("vehicle_model"));
        t.setRenterName(rs.getString("renter_name"));
        return t;
    }
}
