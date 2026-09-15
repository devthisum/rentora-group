package com.rentora.dao.impl;

import com.rentora.dao.interfaces.ReviewDAO;
import com.rentora.model.Review;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDAOImpl implements ReviewDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String BASE_SELECT =
            "SELECT r.*, u.full_name AS renter_name FROM reviews r JOIN users u ON r.renter_id = u.user_id ";

    @Override
    public long create(Review review) throws Exception {
        String sql = "INSERT INTO reviews (booking_id, renter_id, vehicle_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getBookingId());
            ps.setLong(2, review.getRenterId());
            ps.setLong(3, review.getVehicleId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public boolean update(long reviewId, int rating, String comment) throws Exception {
        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE review_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setLong(3, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long reviewId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM reviews WHERE review_id = ?")) {
            ps.setLong(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Review> findByVehicle(long vehicleId) throws Exception {
        List<Review> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE r.vehicle_id = ? ORDER BY r.created_at DESC")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Optional<Review> findByBooking(long bookingId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE r.booking_id = ?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Review> findById(long reviewId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE r.review_id = ?")) {
            ps.setLong(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public double getAverageRatingForVehicle(long vehicleId) throws Exception {
        String sql = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE vehicle_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avg;
                }
                return 0.0;
            }
        }
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getLong("review_id"));
        r.setBookingId(rs.getLong("booking_id"));
        r.setRenterId(rs.getLong("renter_id"));
        r.setVehicleId(rs.getLong("vehicle_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        r.setRenterName(rs.getString("renter_name"));
        return r;
    }
}
