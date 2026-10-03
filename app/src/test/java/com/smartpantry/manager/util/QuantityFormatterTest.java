package com.smartpantry.manager.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QuantityFormatterTest {

    @Test
    public void wholeNumbersHaveNoDecimals() {
        assertEquals("3", QuantityFormatter.format(3.0));
        assertEquals("250", QuantityFormatter.format(250));
        assertEquals("1000", QuantityFormatter.format(1000));
    }

    @Test
    public void fractionsKeepUpToTwoDecimals() {
        assertEquals("1.5", QuantityFormatter.format(1.5));
        assertEquals("0.25", QuantityFormatter.format(0.25));
        assertEquals("0.33", QuantityFormatter.format(1.0 / 3));
        assertEquals("0.67", QuantityFormatter.format(2.0 / 3));
    }

    @Test
    public void appendsTheUnit() {
        assertEquals("6 pcs", QuantityFormatter.format(6, "pcs"));
        assertEquals("0.5 L", QuantityFormatter.format(0.5, "L"));
    }
}
