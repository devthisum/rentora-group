package com.rentora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A staff-created price-drop deal attached to one vehicle. */
public class Promotion {

    private long promotionId;
    private long vehicleId;
    private String title;
    private String description;
    private String discountType;   // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;         // ACTIVE or INACTIVE
    private long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined / computed display fields
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleNumber;
    private BigDecimal vehiclePricePerDay;
    private String createdByName;

    public long getPromotionId() { return promotionId; }
    public void setPromotionId(long promotionId) { this.promotionId = promotionId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public BigDecimal getVehiclePricePerDay() { return vehiclePricePerDay; }
    public void setVehiclePricePerDay(BigDecimal vehiclePricePerDay) { this.vehiclePricePerDay = vehiclePricePerDay; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    /** True if today falls within the promotion's date range and it hasn't been switched off. */
    public boolean isCurrentlyActive() {
        if (!"ACTIVE".equalsIgnoreCase(status)) return false;
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /** Applies this promotion's discount to a base price, floored at 0. */
    public BigDecimal applyTo(BigDecimal basePrice) {
        if (basePrice == null) return null;
        BigDecimal result;
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    discountValue.divide(new BigDecimal("100")));
            result = basePrice.multiply(factor);
        } else {
            result = basePrice.subtract(discountValue);
        }
        return result.max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
