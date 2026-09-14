package com.rentora.dao.impl;

import com.rentora.dao.interfaces.MaintenanceDAO;
import com.rentora.model.MaintenanceRecord;
import com.rentora.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceDAOImpl implements MaintenanceDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String BASE_SELECT =
            "SELECT m.*, v.brand AS vehicle_brand, v.model AS vehicle_model, v.vehicle_number, " +
            "u.full_name AS renter_name, u.phone AS renter_phone, b.renter_id AS renter_id " +
            "FROM maintenance m " +
            "JOIN vehicles v ON m.vehicle_id = v.vehicle_id " +
            "LEFT JOIN bookings b ON m.booking_id = b.booking_id " +
            "LEFT JOIN users u ON b.renter_id = u.user_id ";

    @Override
    public long create(MaintenanceRecord record) throws Exception {
        String sql = "INSERT INTO maintenance (vehicle_id, booking_id, stage, problem_found, notes, " +
                "estimated_days, repair_cost, customer_at_fault, extra_charge_pct, extra_charge_amount, " +
                "total_charge, payment_due_at, checked_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, record);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public boolean update(MaintenanceRecord record) throws Exception {
        String sql = "UPDATE maintenance SET vehicle_id=?, booking_id=?, stage=?, problem_found=?, notes=?, " +
                "estimated_days=?, repair_cost=?, customer_at_fault=?, extra_charge_pct=?, extra_charge_amount=?, " +
                "total_charge=?, payment_due_at=?, checked_by=?, charge_paid=? WHERE maintenance_id=?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bind(ps, record);
            ps.setBoolean(i++, record.isChargePaid());
            ps.setLong(i, record.getMaintenanceId());
            return ps.executeUpdate() > 0;
        }
    }

    private int bind(PreparedStatement ps, MaintenanceRecord record) throws SQLException {
        int i = 1;
        ps.setLong(i++, record.getVehicleId());
        if (record.getBookingId() != null) ps.setLong(i++, record.getBookingId()); else ps.setNull(i++, Types.BIGINT);
        ps.setString(i++, record.getStage());
        if (record.getProblemFound() != null) ps.setBoolean(i++, record.getProblemFound()); else ps.setNull(i++, Types.BOOLEAN);
        ps.setString(i++, record.getNotes());
        if (record.getEstimatedDays() != null) ps.setInt(i++, record.getEstimatedDays()); else ps.setNull(i++, Types.INTEGER);
        if (record.getRepairCost() != null) ps.setBigDecimal(i++, record.getRepairCost()); else ps.setNull(i++, Types.DECIMAL);
        ps.setBoolean(i++, record.isCustomerAtFault());
        if (record.getExtraChargePct() != null) ps.setBigDecimal(i++, record.getExtraChargePct()); else ps.setNull(i++, Types.DECIMAL);
        if (record.getExtraChargeAmount() != null) ps.setBigDecimal(i++, record.getExtraChargeAmount()); else ps.setNull(i++, Types.DECIMAL);
        if (record.getTotalCharge() != null) ps.setBigDecimal(i++, record.getTotalCharge()); else ps.setNull(i++, Types.DECIMAL);
        if (record.getPaymentDueAt() != null) ps.setTimestamp(i++, Timestamp.valueOf(record.getPaymentDueAt())); else ps.setNull(i++, Types.TIMESTAMP);
        if (record.getCheckedBy() != null) ps.setLong(i++, record.getCheckedBy()); else ps.setNull(i++, Types.BIGINT);
        return i;
    }

    @Override
    public Optional<MaintenanceRecord> findById(long maintenanceId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE m.maintenance_id = ?")) {
            ps.setLong(1, maintenanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<MaintenanceRecord> findByVehicleAndStage(long vehicleId, String stage) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE m.vehicle_id = ? AND m.stage = ? ORDER BY m.created_at DESC LIMIT 1")) {
            ps.setLong(1, vehicleId);
            ps.setString(2, stage);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<MaintenanceRecord> findByBookingId(long bookingId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE m.booking_id = ? ORDER BY m.created_at DESC LIMIT 1")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<MaintenanceRecord> findActive() throws Exception {
        List<MaintenanceRecord> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE m.stage IN ('CHECKING','UNDER_MAINTENANCE','AWAITING_CUSTOMER_PAYMENT') " +
                     "ORDER BY FIELD(m.stage,'CHECKING','AWAITING_CUSTOMER_PAYMENT','UNDER_MAINTENANCE'), m.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<MaintenanceRecord> findByVehicle(long vehicleId) throws Exception {
        List<MaintenanceRecord> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     BASE_SELECT + " WHERE m.vehicle_id = ? ORDER BY m.created_at DESC")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private MaintenanceRecord mapRow(ResultSet rs) throws SQLException {
        MaintenanceRecord m = new MaintenanceRecord();
        m.setMaintenanceId(rs.getLong("maintenance_id"));
        m.setVehicleId(rs.getLong("vehicle_id"));
        long bookingId = rs.getLong("booking_id");
        if (!rs.wasNull()) m.setBookingId(bookingId);
        m.setStage(rs.getString("stage"));
        boolean problemFound = rs.getBoolean("problem_found");
        if (!rs.wasNull()) m.setProblemFound(problemFound);
        m.setNotes(rs.getString("notes"));
        int estDays = rs.getInt("estimated_days");
        if (!rs.wasNull()) m.setEstimatedDays(estDays);
        m.setRepairCost(rs.getBigDecimal("repair_cost"));
        m.setCustomerAtFault(rs.getBoolean("customer_at_fault"));
        m.setExtraChargePct(rs.getBigDecimal("extra_charge_pct"));
        m.setExtraChargeAmount(rs.getBigDecimal("extra_charge_amount"));
        m.setTotalCharge(rs.getBigDecimal("total_charge"));
        m.setChargePaid(rs.getBoolean("charge_paid"));
        Timestamp dueTs = rs.getTimestamp("payment_due_at");
        if (dueTs != null) m.setPaymentDueAt(LocalDateTime.ofInstant(dueTs.toInstant(), ZoneId.systemDefault()));
        long checkedBy = rs.getLong("checked_by");
        if (!rs.wasNull()) m.setCheckedBy(checkedBy);
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) m.setCreatedAt(LocalDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault()));
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) m.setUpdatedAt(LocalDateTime.ofInstant(updatedTs.toInstant(), ZoneId.systemDefault()));
        m.setVehicleBrand(rs.getString("vehicle_brand"));
        m.setVehicleModel(rs.getString("vehicle_model"));
        m.setVehicleNumber(rs.getString("vehicle_number"));
        m.setRenterName(rs.getString("renter_name"));
        m.setRenterPhone(rs.getString("renter_phone"));
        long renterId = rs.getLong("renter_id");
        if (!rs.wasNull()) m.setRenterId(renterId);
        return m;
    }
}
