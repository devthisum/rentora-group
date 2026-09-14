package com.rentora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private long bookingId;
    private long renterId;
    private long vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal lateFee;
    private Long couponId;
    private String status; // AWAITING_PAYMENT, CONFIRMED, ONGOING, RETURNED, COMPLETED, CANCELLED
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;

    // Convenience fields for dashboard display (joined data)
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleNumber;
    private BigDecimal vehiclePricePerDay;
    private String renterName;

    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }

    public long getRenterId() { return renterId; }
    public void setRenterId(long renterId) { this.renterId = renterId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public BigDecimal getVehiclePricePerDay() { return vehiclePricePerDay; }
    public void setVehiclePricePerDay(BigDecimal vehiclePricePerDay) { this.vehiclePricePerDay = vehiclePricePerDay; }

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }

    /** Days remaining until the vehicle is due back, based on endDate (today if already overdue). */
    public long getDaysUntilReturn() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return Math.max(days, 0);
    }

    /** Days overdue right now, based on endDate vs today — 0 if not yet due back. Used to preview a likely late fee before a return is actually confirmed. */
    public long getDaysLateSoFar() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(endDate, LocalDate.now());
        return Math.max(days, 0);
    }

    /** Live preview of what the late fee would be if returned right now — 30% of the daily price per day overdue. The real fee is (re-)calculated server-side at the moment a return is actually confirmed. */
    public BigDecimal getProjectedLateFee() {
        long lateDays = getDaysLateSoFar();
        if (lateDays <= 0 || vehiclePricePerDay == null) return BigDecimal.ZERO;
        return vehiclePricePerDay.multiply(new BigDecimal("0.30"))
                .multiply(BigDecimal.valueOf(lateDays))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
