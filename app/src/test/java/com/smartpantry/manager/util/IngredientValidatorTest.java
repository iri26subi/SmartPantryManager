package com.smartpantry.manager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class IngredientValidatorTest {

    @Test
    public void validNamesPass() {
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("Eggs"));
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("  red   onion "));
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("Self-raising flour"));
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("Baker's yeast"));
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("7 up"));
        assertEquals(IngredientValidator.NameError.NONE, IngredientValidator.validateName("Crème fraîche"));
    }

    @Test
    public void blankNamesAreRejected() {
        assertEquals(IngredientValidator.NameError.REQUIRED, IngredientValidator.validateName(""));
        assertEquals(IngredientValidator.NameError.REQUIRED, IngredientValidator.validateName("    "));
        assertEquals(IngredientValidator.NameError.REQUIRED, IngredientValidator.validateName(null));
    }

    @Test
    public void invalidNamesAreRejected() {
        assertEquals(IngredientValidator.NameError.INVALID, IngredientValidator.validateName("123"));
        assertEquals(IngredientValidator.NameError.INVALID, IngredientValidator.validateName("-eggs"));
        assertEquals(IngredientValidator.NameError.INVALID, IngredientValidator.validateName("eggs; DROP TABLE"));
        assertEquals(IngredientValidator.NameError.INVALID, IngredientValidator.validateName("milk!"));
    }

    @Test
    public void longNamesAreRejected() {
        String name = "a".repeat(IngredientValidator.MAX_NAME_LENGTH + 1);
        assertEquals(IngredientValidator.NameError.TOO_LONG, IngredientValidator.validateName(name));
    }

    @Test
    public void cleanNameTrimsAndCollapsesSpaces() {
        assertEquals("red onion", IngredientValidator.cleanName("  red   onion "));
    }

    @Test
    public void validQuantitiesPass() {
        assertEquals(IngredientValidator.QuantityError.NONE, IngredientValidator.validateQuantity("3"));
        assertEquals(IngredientValidator.QuantityError.NONE, IngredientValidator.validateQuantity("1.5"));
        assertEquals(IngredientValidator.QuantityError.NONE, IngredientValidator.validateQuantity("0,25"));
        assertEquals(IngredientValidator.QuantityError.NONE, IngredientValidator.validateQuantity(".5"));
        assertEquals(0.25, IngredientValidator.parseQuantity("0,25"), 1e-9);
    }

    @Test
    public void zeroAndNegativeQuantitiesAreRejected() {
        assertEquals(IngredientValidator.QuantityError.NOT_POSITIVE, IngredientValidator.validateQuantity("0"));
        assertEquals(IngredientValidator.QuantityError.NOT_POSITIVE, IngredientValidator.validateQuantity("0.0"));
        assertEquals(IngredientValidator.QuantityError.NOT_POSITIVE, IngredientValidator.validateQuantity("-2"));
    }

    @Test
    public void nonNumericQuantitiesAreRejected() {
        assertEquals(IngredientValidator.QuantityError.REQUIRED, IngredientValidator.validateQuantity(""));
        assertEquals(IngredientValidator.QuantityError.REQUIRED, IngredientValidator.validateQuantity("  "));
        assertEquals(IngredientValidator.QuantityError.NOT_A_NUMBER, IngredientValidator.validateQuantity("abc"));
        assertEquals(IngredientValidator.QuantityError.NOT_A_NUMBER, IngredientValidator.validateQuantity("2.5.1"));
        assertEquals(IngredientValidator.QuantityError.NOT_A_NUMBER, IngredientValidator.validateQuantity("NaN"));
        assertEquals(IngredientValidator.QuantityError.NOT_A_NUMBER, IngredientValidator.validateQuantity("1e5"));
        assertEquals(IngredientValidator.QuantityError.NOT_A_NUMBER, IngredientValidator.validateQuantity("Infinity"));
        assertNull(IngredientValidator.parseQuantity("."));
    }

    @Test
    public void hugeQuantitiesAreRejected() {
        assertEquals(IngredientValidator.QuantityError.TOO_LARGE, IngredientValidator.validateQuantity("100001"));
    }
}
