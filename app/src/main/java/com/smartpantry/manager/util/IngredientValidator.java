package com.smartpantry.manager.util;

/**
 * Validation rules for the Add/Edit Ingredient form.
 *
 * Kept free of Android classes so the rules can be unit-tested on the JVM.
 * The Activity maps each error to a user-friendly string resource.
 */
public final class IngredientValidator {

    public static final int MAX_NAME_LENGTH = 50;
    public static final double MAX_QUANTITY = 100_000;

    public enum NameError { NONE, REQUIRED, TOO_LONG, INVALID }

    public enum QuantityError { NONE, REQUIRED, NOT_A_NUMBER, NOT_POSITIVE, TOO_LARGE }

    /**
     * Letters (any language), digits, spaces, hyphens and apostrophes; must
     * start with a letter or digit and contain at least one letter.
     */
    private static final String NAME_PATTERN = "[\\p{L}0-9][\\p{L}0-9 '\\-]*";
    private static final String HAS_LETTER_PATTERN = ".*\\p{L}.*";

    private IngredientValidator() {
        // utility class
    }

    /** Trims the name and collapses repeated spaces ("  red   onion " -> "red onion"). */
    public static String cleanName(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    public static NameError validateName(String raw) {
        String name = cleanName(raw);
        if (name.isEmpty()) {
            return NameError.REQUIRED;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return NameError.TOO_LONG;
        }
        if (!name.matches(NAME_PATTERN) || !name.matches(HAS_LETTER_PATTERN)) {
            return NameError.INVALID;
        }
        return NameError.NONE;
    }

    public static QuantityError validateQuantity(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return QuantityError.REQUIRED;
        }
        Double value = parseQuantity(raw);
        if (value == null) {
            return QuantityError.NOT_A_NUMBER;
        }
        if (value <= 0) {
            return QuantityError.NOT_POSITIVE;
        }
        if (value > MAX_QUANTITY) {
            return QuantityError.TOO_LARGE;
        }
        return QuantityError.NONE;
    }

    /**
     * Parses a quantity, accepting a comma as the decimal separator
     * (common on South African keyboards). Returns null for anything that is
     * not a plain finite decimal number, e.g. "abc", "1e5", "NaN" or "2.5.1".
     */
    public static Double parseQuantity(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim().replace(',', '.');
        if (!text.matches("-?\\d*\\.?\\d+|-?\\d+\\.")) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
