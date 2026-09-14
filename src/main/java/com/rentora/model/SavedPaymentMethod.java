package com.rentora.model;

import java.time.LocalDateTime;

/**
 * A renter's saved payment method for faster checkout. Since Rentora's
 * payment flow is fully simulated (no real processor), only a masked card
 * number (last 4 digits) and expiry are ever stored — never a full card
 * number or CVV, exactly like the one-off checkout on the payment page.
 */
public class SavedPaymentMethod {
    private long paymentMethodId;
    private long userId;
    private String type; // CARD, WALLET
    private String label;
    private String maskedNumber;
    private String expiry;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public long getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(long paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getMaskedNumber() { return maskedNumber; }
    public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
