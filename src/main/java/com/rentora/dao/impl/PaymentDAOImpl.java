package com.rentora.dao.impl;

import com.rentora.dao.interfaces.PaymentDAO;
import com.rentora.model.Payment;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class PaymentDAOImpl implements PaymentDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    @Override
    public long create(Payment payment) throws Exception {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, payment_status, transaction_ref, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getBookingId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus());
            ps.setString(5, payment.getTransactionRef());
            if (payment.getPaidAt() != null) ps.setTimestamp(6, Timestamp.valueOf(payment.getPaidAt()));
            else ps.setNull(6, Types.TIMESTAMP);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<Payment> findByBooking(long bookingId) throws Exception {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Payment p = new Payment();
                p.setPaymentId(rs.getLong("payment_id"));
                p.setBookingId(rs.getLong("booking_id"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setPaymentMethod(rs.getString("payment_method"));
                p.setPaymentStatus(rs.getString("payment_status"));
                p.setTransactionRef(rs.getString("transaction_ref"));
                Timestamp ts = rs.getTimestamp("paid_at");
                if (ts != null) p.setPaidAt(LocalDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
                return Optional.of(p);
            }
        }
    }
}
