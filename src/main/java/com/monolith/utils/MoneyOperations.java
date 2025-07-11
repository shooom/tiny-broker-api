package com.monolith.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyOperations {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Standardizes BigDecimal operations by applying consistent scale and rounding mode.
     *
     * @param amount the amount to standardize
     * @return the standardized amount
     */
    public static BigDecimal standardize(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, ROUNDING_MODE);
    }
}
