package com.rentora.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Wallet payment: base fare, no service fee, small loyalty incentive (2% off). */
public class WalletPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal WALLET_DISCOUNT_RATE = new BigDecimal("0.02");

    @Override
    public BigDecimal calculateTotal(BigDecimal pricePerDay, long numberOfDays) {
        BigDecimal base = pricePerDay.multiply(BigDecimal.valueOf(numberOfDays));
        BigDecimal discount = base.multiply(WALLET_DISCOUNT_RATE);
        return base.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getMethodName() { return "WALLET"; }
}
