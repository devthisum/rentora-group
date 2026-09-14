package com.rentora.model;

import java.time.LocalDateTime;

/** A conversation between a renter and the shop (any admin/staff can reply) about a specific vehicle. */
public class InquiryThread {
    private long threadId;
    private long vehicleId;
    private long renterId;
    private String status; // OPEN, CLOSED
    private LocalDateTime createdAt;

    // Joined display fields
    private String vehicleBrand;
    private String vehicleModel;
    private String renterName;
    private String lastMessagePreview;
    private boolean hasUnread;

    public long getThreadId() { return threadId; }
    public void setThreadId(long threadId) { this.threadId = threadId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public long getRenterId() { return renterId; }
    public void setRenterId(long renterId) { this.renterId = renterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }

    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }

    public boolean isHasUnread() { return hasUnread; }
    public void setHasUnread(boolean hasUnread) { this.hasUnread = hasUnread; }
}
