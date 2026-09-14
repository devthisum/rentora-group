package com.rentora.dao.impl;

import com.rentora.dao.interfaces.AnnouncementDAO;
import com.rentora.model.Announcement;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnouncementDAOImpl implements AnnouncementDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String BASE_SELECT =
            "SELECT a.*, u.full_name AS posted_by_name FROM announcements a " +
            "JOIN users u ON a.posted_by = u.user_id ";

    @Override
    public long create(Announcement a) throws Exception {
        String sql = "INSERT INTO announcements (title, message, priority, category, posted_by, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getMessage());
            ps.setString(3, a.getPriority());
            ps.setString(4, a.getCategory());
            ps.setLong(5, a.getPostedBy());
            if (a.getExpiryDate() != null) ps.setDate(6, Date.valueOf(a.getExpiryDate()));
            else ps.setNull(6, Types.DATE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<Announcement> findById(long id) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE a.announcement_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Announcement> findAllActive() throws Exception {
        List<Announcement> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE a.expiry_date IS NULL OR a.expiry_date >= CURDATE() " +
                "ORDER BY FIELD(a.priority,'URGENT','HIGH','NORMAL','LOW'), a.created_at DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Announcement> findAllForAdmin() throws Exception {
        List<Announcement> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " ORDER BY a.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Announcement a) throws Exception {
        String sql = "UPDATE announcements SET title=?, message=?, priority=?, category=?, expiry_date=? WHERE announcement_id=?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getMessage());
            ps.setString(3, a.getPriority());
            ps.setString(4, a.getCategory());
            if (a.getExpiryDate() != null) ps.setDate(5, Date.valueOf(a.getExpiryDate()));
            else ps.setNull(5, Types.DATE);
            ps.setLong(6, a.getAnnouncementId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long id) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM announcements WHERE announcement_id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markRead(long userId, long announcementId) throws Exception {
        String sql = "INSERT IGNORE INTO announcement_reads (user_id, announcement_id) VALUES (?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, announcementId);
            return ps.executeUpdate() >= 0;
        }
    }

    @Override
    public boolean isReadByUser(long userId, long announcementId) throws Exception {
        String sql = "SELECT 1 FROM announcement_reads WHERE user_id = ? AND announcement_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, announcementId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int countUnreadForUser(long userId) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM announcements a " +
                "WHERE (a.expiry_date IS NULL OR a.expiry_date >= CURDATE()) " +
                "AND NOT EXISTS (SELECT 1 FROM announcement_reads r WHERE r.announcement_id = a.announcement_id AND r.user_id = ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    private Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setAnnouncementId(rs.getLong("announcement_id"));
        a.setTitle(rs.getString("title"));
        a.setMessage(rs.getString("message"));
        a.setPriority(rs.getString("priority"));
        a.setCategory(rs.getString("category"));
        a.setPostedBy(rs.getLong("posted_by"));
        Date expiry = rs.getDate("expiry_date");
        if (expiry != null) a.setExpiryDate(expiry.toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        a.setPostedByName(rs.getString("posted_by_name"));
        return a;
    }
}
