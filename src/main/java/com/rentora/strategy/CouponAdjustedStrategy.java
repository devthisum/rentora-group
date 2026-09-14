package com.rentora.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorating strategy that wraps another PaymentStrategy and applies
 * a coupon discount percentage on top of its result.
 */
public class CouponAdjustedStrategy implements PaymentStrategy {

    private final PaymentStrategy delegate;
    private final BigDecimal discountPct;

    public CouponAdjustedStrategy(PaymentStrategy delegate, BigDecimal discountPct) {
        this.delegate = delegate;
        this.discountPct = discountPct;
    }

    @Override
    public BigDecimal calculateTotal(BigDecimal pricePerDay, long numberOfDays) {
        BigDecimal subtotal = delegate.calculateTotal(pricePerDay, numberOfDays);
        BigDecimal discount = subtotal.multiply(discountPct.divide(BigDecimal.valueOf(100)));
        return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getMethodName() { return delegate.getMethodName() + "_WITH_COUPON"; }
}
