package com.rentora.dao.impl;

import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.model.Vehicle;
import com.rentora.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

/** JDBC implementation of VehicleDAO with dynamic filter-based search. */
public class VehicleDAOImpl implements VehicleDAO {

    private final DBConnectionManager connectionManager = DBConnectionManager.getInstance();

    private static final String INSERT_VEHICLE =
            "INSERT INTO vehicles (category_id, added_by, vehicle_number, brand, model, year, seats, " +
            "transmission, fuel_type, price_per_day, description, image_url, status, doors, air_conditioner, mileage, features) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String BASE_SELECT =
            "SELECT v.*, c.category_name, " +
            // Only ONGOING (customer has physically picked the car up, confirmed by
            // Booking Staff) counts as "booked" for the shop-floor status shown here.
            // CONFIRMED (paid but not yet picked up) deliberately does NOT count —
            // otherwise a no-show customer would leave the vehicle stuck showing
            // "Booked" all day even though it's sitting unused in the shop. Date-range
            // conflict checks elsewhere (isAvailable/hasDateConflict) still correctly
            // treat CONFIRMED as reserved for that purpose.
            "EXISTS(SELECT 1 FROM bookings bk WHERE bk.vehicle_id = v.vehicle_id " +
            "       AND bk.status = 'ONGOING' " +
            "       AND CURDATE() BETWEEN bk.start_date AND bk.end_date) AS booked_today " +
            "FROM vehicles v " +
            "JOIN vehicle_categories c ON v.category_id = c.category_id ";

    @Override
    public long create(Vehicle vehicle) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_VEHICLE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehicle.getCategoryId());
            if (vehicle.getAddedBy() > 0) ps.setLong(2, vehicle.getAddedBy());
            else ps.setNull(2, Types.BIGINT);
            ps.setString(3, vehicle.getVehicleNumber());
            ps.setString(4, vehicle.getBrand());
            ps.setString(5, vehicle.getModel());
            ps.setInt(6, vehicle.getYear());
            ps.setInt(7, vehicle.getSeats());
            ps.setString(8, vehicle.getTransmission());
            ps.setString(9, vehicle.getFuelType());
            ps.setBigDecimal(10, vehicle.getPricePerDay());
            ps.setString(11, vehicle.getDescription());
            ps.setString(12, vehicle.getImageUrl());
            ps.setString(13, "AVAILABLE");
            if (vehicle.getDoors() != null) ps.setInt(14, vehicle.getDoors());
            else ps.setNull(14, Types.TINYINT);
            ps.setString(15, vehicle.getAirConditioner() != null ? vehicle.getAirConditioner() : "YES");
            if (vehicle.getMileage() != null) ps.setInt(16, vehicle.getMileage());
            else ps.setNull(16, Types.INTEGER);
            ps.setString(17, vehicle.getFeatures());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1;
        }
    }

    @Override
    public Optional<Vehicle> findById(long vehicleId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " WHERE v.vehicle_id = ?")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Dynamic search supporting: category, minPrice, maxPrice, seats, transmission, fuelType, q (brand/model keyword).
     * Only AVAILABLE vehicles are ever returned to public search.
     */
    @Override
    public List<Vehicle> search(Map<String, String> filters) throws Exception {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filters.containsKey("category")) {
            sql.append(" AND c.category_name = ? ");
            params.add(filters.get("category"));
        }
        if (filters.containsKey("minPrice")) {
            sql.append(" AND v.price_per_day >= ? ");
            params.add(new BigDecimal(filters.get("minPrice")));
        }
        if (filters.containsKey("maxPrice")) {
            sql.append(" AND v.price_per_day <= ? ");
            params.add(new BigDecimal(filters.get("maxPrice")));
        }
        if (filters.containsKey("transmission")) {
            sql.append(" AND v.transmission = ? ");
            params.add(filters.get("transmission"));
        }
        if (filters.containsKey("fuelType")) {
            sql.append(" AND v.fuel_type = ? ");
            params.add(filters.get("fuelType"));
        }
        if (filters.containsKey("q")) {
            sql.append(" AND (v.brand LIKE ? OR v.model LIKE ?) ");
            String like = "%" + filters.get("q") + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY v.average_rating DESC, v.created_at DESC ");

        List<Vehicle> results = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    /** All vehicles in the shop's stock, regardless of status — used by the admin vehicle-management screens. */
    @Override
    public List<Vehicle> findAll() throws Exception {
        List<Vehicle> results = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + " ORDER BY v.created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    @Override
    public boolean updateStatus(long vehicleId, String status) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE vehicles SET status = ? WHERE vehicle_id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Vehicle vehicle) throws Exception {
        String sql = "UPDATE vehicles SET brand=?, model=?, year=?, seats=?, transmission=?, fuel_type=?, " +
                "price_per_day=?, description=?, image_url=?, doors=?, air_conditioner=?, mileage=?, features=? WHERE vehicle_id=?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicle.getBrand());
            ps.setString(2, vehicle.getModel());
            ps.setInt(3, vehicle.getYear());
            ps.setInt(4, vehicle.getSeats());
            ps.setString(5, vehicle.getTransmission());
            ps.setString(6, vehicle.getFuelType());
            ps.setBigDecimal(7, vehicle.getPricePerDay());
            ps.setString(8, vehicle.getDescription());
            ps.setString(9, vehicle.getImageUrl());
            if (vehicle.getDoors() != null) ps.setInt(10, vehicle.getDoors());
            else ps.setNull(10, Types.TINYINT);
            ps.setString(11, vehicle.getAirConditioner() != null ? vehicle.getAirConditioner() : "YES");
            if (vehicle.getMileage() != null) ps.setInt(12, vehicle.getMileage());
            else ps.setNull(12, Types.INTEGER);
            ps.setString(13, vehicle.getFeatures());
            ps.setLong(14, vehicle.getVehicleId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long vehicleId) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM vehicles WHERE vehicle_id = ?")) {
            ps.setLong(1, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsByVehicleNumber(String vehicleNumber) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM vehicles WHERE vehicle_number = ?")) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean isAvailable(long vehicleId, LocalDate start, LocalDate end) throws Exception {
        String sql = "SELECT 1 FROM bookings WHERE vehicle_id = ? AND status IN ('AWAITING_PAYMENT','CONFIRMED','ONGOING') " +
                "AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // available if NO conflicting row found
            }
        }
    }

    @Override
    public boolean updateAverageRating(long vehicleId, double averageRating) throws Exception {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE vehicles SET average_rating = ? WHERE vehicle_id = ?")) {
            ps.setDouble(1, averageRating);
            ps.setLong(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getLong("vehicle_id"));
        v.setCategoryId(rs.getInt("category_id"));
        v.setCategoryName(rs.getString("category_name"));
        long addedBy = rs.getLong("added_by");
        if (!rs.wasNull()) v.setAddedBy(addedBy);
        v.setVehicleNumber(rs.getString("vehicle_number"));
        v.setBrand(rs.getString("brand"));
        v.setModel(rs.getString("model"));
        v.setYear(rs.getInt("year"));
        v.setSeats(rs.getInt("seats"));
        v.setTransmission(rs.getString("transmission"));
        v.setFuelType(rs.getString("fuel_type"));
        v.setPricePerDay(rs.getBigDecimal("price_per_day"));
        v.setDescription(rs.getString("description"));
        v.setImageUrl(rs.getString("image_url"));
        v.setStatus(rs.getString("status"));
        v.setAverageRating(rs.getDouble("average_rating"));
        try {
            int doors = rs.getInt("doors");
            if (!rs.wasNull()) v.setDoors(doors);
            v.setAirConditioner(rs.getString("air_conditioner"));
            int mileage = rs.getInt("mileage");
            if (!rs.wasNull()) v.setMileage(mileage);
            v.setFeatures(rs.getString("features"));
        } catch (SQLException ignored) {
            // Columns not present yet — run database/migration_vehicle_specs.sql. Page still works without them.
        }
        try {
            v.setBookedToday(rs.getBoolean("booked_today"));
        } catch (SQLException ignored) {
            // Only present on queries built from BASE_SELECT; harmless if absent elsewhere.
        }
        return v;
    }
}
