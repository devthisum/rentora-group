package com.rentora.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern.
 * Encapsulates a fare/payment calculation algorithm so new payment
 * methods or discount rules can be added without modifying booking logic.
 */
public interface PaymentStrategy {
    BigDecimal calculateTotal(BigDecimal pricePerDay, long numberOfDays);
    String getMethodName();
}
