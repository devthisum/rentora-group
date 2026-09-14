package com.rentora.dao.impl;

import com.rentora.dao.interfaces.BookingDAO;
import com.rentora.model.Booking;
import com.rentora.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAOImpl implements BookingDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String INSERT_BOOKING =
            "INSERT INTO bookings (renter_id, vehicle_id, start_date, end_date, total_amount, coupon_id, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, 'AWAITING_PAYMENT')";

    private static final String BASE_SELECT =
            "SELECT b.*, v.brand AS vehicle_brand, v.model AS vehicle_model, v.vehicle_number AS vehicle_number, " +
            "v.price_per_day AS vehicle_price_per_day, " +
            "u.full_name AS renter_name " +
            "FROM bookings b " +
            "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
            "JOIN users u ON b.renter_id = u.user_id ";

    @Override
    public long create(Booking booking) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_BOOKING, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getRenterId());
            ps.setLong(2, booking.getVehicleId());
            ps.setDate(3, Date.valueOf(booking.getStartDate()));
            ps.setDate(4, Date.valueOf(booking.getEndDate()));
            ps.setBigDecimal(5, booking.getTotalAmount());
            if (booking.getCouponId() != null) ps.setLong(6, booking.getCouponId());
            else ps.setNull(6, Types.BIGINT);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<Booking> findById(long bookingId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE b.booking_id = ?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Booking> findByRenter(long renterId) throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE b.renter_id = ? ORDER BY b.created_at DESC")) {
            ps.setLong(1, renterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Booking> findAll() throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " ORDER BY b.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Booking> findActiveOrderedByReturn() throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE b.status IN ('CONFIRMED','ONGOING') ORDER BY b.end_date ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Booking> findPickupsDue() throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE b.status = 'CONFIRMED' AND b.start_date <= CURDATE() ORDER BY b.start_date ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Booking> findActiveByVehicle(long vehicleId) throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE b.vehicle_id = ? AND b.status IN ('AWAITING_PAYMENT','CONFIRMED','ONGOING','RETURNED') " +
                     "ORDER BY b.start_date ASC")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Booking> findExpiredAwaitingPayment(LocalDateTime cutoff) throws Exception {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE b.status = 'AWAITING_PAYMENT' AND b.created_at < ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(long bookingId, String status) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE bookings SET status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Marks the booking RETURNED and stamps returned_at — the trigger point for the maintenance check. */
    @Override
    public boolean markReturned(long bookingId, BigDecimal lateFee) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE bookings SET status = 'RETURNED', returned_at = ?, late_fee = ? WHERE booking_id = ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBigDecimal(2, lateFee != null ? lateFee : BigDecimal.ZERO);
            ps.setLong(3, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateDates(long bookingId, LocalDate start, LocalDate end, java.math.BigDecimal totalAmount) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE bookings SET start_date = ?, end_date = ?, total_amount = ? WHERE booking_id = ?")) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ps.setBigDecimal(3, totalAmount);
            ps.setLong(4, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Checks for overlapping active bookings on the same vehicle (used pre-booking). */
    @Override
    public boolean hasDateConflict(long vehicleId, LocalDate start, LocalDate end) throws Exception {
        String sql = "SELECT 1 FROM bookings WHERE vehicle_id = ? AND status IN ('AWAITING_PAYMENT','CONFIRMED','ONGOING') " +
                "AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Same conflict check, but ignores a specific booking — used when a renter edits their own booking's dates. */
    @Override
    public boolean hasDateConflict(long vehicleId, LocalDate start, LocalDate end, long excludeBookingId) throws Exception {
        String sql = "SELECT 1 FROM bookings WHERE vehicle_id = ? AND booking_id != ? " +
                "AND status IN ('AWAITING_PAYMENT','CONFIRMED','ONGOING') " +
                "AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setLong(2, excludeBookingId);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getLong("booking_id"));
        b.setRenterId(rs.getLong("renter_id"));
        b.setVehicleId(rs.getLong("vehicle_id"));
        b.setStartDate(rs.getDate("start_date").toLocalDate());
        b.setEndDate(rs.getDate("end_date").toLocalDate());
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        BigDecimal lateFee = rs.getBigDecimal("late_fee");
        b.setLateFee(lateFee != null ? lateFee : BigDecimal.ZERO);
        long couponId = rs.getLong("coupon_id");
        if (!rs.wasNull()) b.setCouponId(couponId);
        b.setStatus(rs.getString("status"));
        Timestamp returnedTs = rs.getTimestamp("returned_at");
        if (returnedTs != null) b.setReturnedAt(LocalDateTime.ofInstant(returnedTs.toInstant(), java.time.ZoneId.systemDefault()));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        b.setVehicleBrand(rs.getString("vehicle_brand"));
        b.setVehicleModel(rs.getString("vehicle_model"));
        b.setVehicleNumber(rs.getString("vehicle_number"));
        BigDecimal pricePerDay = rs.getBigDecimal("vehicle_price_per_day");
        b.setVehiclePricePerDay(pricePerDay != null ? pricePerDay : BigDecimal.ZERO);
        b.setRenterName(rs.getString("renter_name"));
        return b;
    }
}
