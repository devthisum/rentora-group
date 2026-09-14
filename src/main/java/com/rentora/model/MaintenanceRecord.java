package com.rentora.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks a vehicle from "just returned" through inspection to resolution.
 * See database/schema.sql (maintenance table) for the full stage lifecycle.
 */
public class MaintenanceRecord {
    private long maintenanceId;
    private long vehicleId;
    private Long bookingId;
    private String stage; // CHECKING, OK, UNDER_MAINTENANCE, AWAITING_CUSTOMER_PAYMENT, RESOLVED
    private Boolean problemFound;
    private String notes;
    private Integer estimatedDays;
    private BigDecimal repairCost;
    private boolean customerAtFault;
    private BigDecimal extraChargePct;
    private BigDecimal extraChargeAmount;
    private BigDecimal totalCharge;
    private boolean chargePaid;
    private LocalDateTime paymentDueAt;
    private Long checkedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined display fields
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleNumber;
    private String renterName;
    private String renterPhone;
    private Long renterId;

    public long getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(long maintenanceId) { this.maintenanceId = maintenanceId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public Boolean getProblemFound() { return problemFound; }
    public void setProblemFound(Boolean problemFound) { this.problemFound = problemFound; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public BigDecimal getRepairCost() { return repairCost; }
    public void setRepairCost(BigDecimal repairCost) { this.repairCost = repairCost; }

    public boolean isCustomerAtFault() { return customerAtFault; }
    public void setCustomerAtFault(boolean customerAtFault) { this.customerAtFault = customerAtFault; }

    public BigDecimal getExtraChargePct() { return extraChargePct; }
    public void setExtraChargePct(BigDecimal extraChargePct) { this.extraChargePct = extraChargePct; }

    public BigDecimal getExtraChargeAmount() { return extraChargeAmount; }
    public void setExtraChargeAmount(BigDecimal extraChargeAmount) { this.extraChargeAmount = extraChargeAmount; }

    public BigDecimal getTotalCharge() { return totalCharge; }
    public void setTotalCharge(BigDecimal totalCharge) { this.totalCharge = totalCharge; }

    public boolean isChargePaid() { return chargePaid; }
    public void setChargePaid(boolean chargePaid) { this.chargePaid = chargePaid; }

    public LocalDateTime getPaymentDueAt() { return paymentDueAt; }
    public void setPaymentDueAt(LocalDateTime paymentDueAt) { this.paymentDueAt = paymentDueAt; }

    public Long getCheckedBy() { return checkedBy; }
    public void setCheckedBy(Long checkedBy) { this.checkedBy = checkedBy; }

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

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }

    public String getRenterPhone() { return renterPhone; }
    public void setRenterPhone(String renterPhone) { this.renterPhone = renterPhone; }

    public Long getRenterId() { return renterId; }
    public void setRenterId(Long renterId) { this.renterId = renterId; }

    public boolean isPaymentOverdue() {
        return paymentDueAt != null && !chargePaid && LocalDateTime.now().isAfter(paymentDueAt);
    }
}
