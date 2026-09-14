package com.rentora.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Standard card payment: base fare + a flat platform service fee (5%). */
public class CardPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    @Override
    public BigDecimal calculateTotal(BigDecimal pricePerDay, long numberOfDays) {
        BigDecimal base = pricePerDay.multiply(BigDecimal.valueOf(numberOfDays));
        BigDecimal fee = base.multiply(SERVICE_FEE_RATE);
        return base.add(fee).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getMethodName() { return "CARD"; }
}
