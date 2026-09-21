package com.rentora.model;

import java.time.LocalDateTime;

/** A single one-time-password sent to a user for the forgot-password flow. */
public class PasswordResetOtp {

    private long otpId;
    private long userId;
    private String otpCode;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime createdAt;

    public long getOtpId() { return otpId; }
    public void setOtpId(long otpId) { this.otpId = otpId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() { return expiresAt != null && LocalDateTime.now().isAfter(expiresAt); }
    public boolean isValid() { return !used && !isExpired(); }
}
