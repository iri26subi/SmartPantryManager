package com.smartpantry.manager.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formats quantities for display without trailing zeros (3 instead of 3.0). */
public final class QuantityFormatter {

    private QuantityFormatter() {
        // utility class
    }

    public static String format(double quantity) {
        BigDecimal value = BigDecimal.valueOf(quantity)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return value.toPlainString();
    }

    public static String format(double quantity, String unit) {
        return format(quantity) + " " + unit;
    }
}
