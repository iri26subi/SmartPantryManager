package com.smartpantry.manager.matching;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities between measurement units of the same kind.
 *
 * Every supported unit belongs to one {@link Dimension} and has a factor that
 * converts it to that dimension's base unit (pieces, grams or millilitres).
 * Quantities are only comparable when their units share a dimension; there is
 * deliberately no mass-to-volume conversion because that depends on the
 * density of each ingredient and cannot be done reliably.
 */
public final class UnitConverter {

    /** The kind of quantity a unit measures. */
    public enum Dimension {
        COUNT,  // base unit: pcs
        MASS,   // base unit: g
        VOLUME  // base unit: ml
    }

    public static final String PIECES = "pcs";
    public static final String GRAMS = "g";
    public static final String KILOGRAMS = "kg";
    public static final String MILLILITRES = "ml";
    public static final String LITRES = "L";
    public static final String TEASPOON = "tsp";
    public static final String TABLESPOON = "tbsp";
    public static final String CUP = "cup";

    /** Canonical units in the order they are offered to the user. */
    public static final List<String> SUPPORTED_UNITS = Collections.unmodifiableList(Arrays.asList(
            PIECES, GRAMS, KILOGRAMS, MILLILITRES, LITRES, TEASPOON, TABLESPOON, CUP));

    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();
    private static final Map<String, Double> TO_BASE = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        register(PIECES, Dimension.COUNT, 1, "pc", "piece", "pieces", "each", "whole", "item", "items", "unit", "units");
        register(GRAMS, Dimension.MASS, 1, "gram", "grams", "gr", "gm");
        register(KILOGRAMS, Dimension.MASS, 1000, "kilogram", "kilograms", "kilo", "kilos", "kgs");
        register(MILLILITRES, Dimension.VOLUME, 1, "millilitre", "millilitres", "milliliter", "milliliters", "mls");
        register(LITRES, Dimension.VOLUME, 1000, "l", "litre", "litres", "liter", "liters", "lt", "ltr");
        register(TEASPOON, Dimension.VOLUME, 5, "teaspoon", "teaspoons", "tsps");
        register(TABLESPOON, Dimension.VOLUME, 15, "tablespoon", "tablespoons", "tbsps", "tbs");
        // Metric cup (250 ml), as used in South African recipes.
        register(CUP, Dimension.VOLUME, 250, "cups", "c");
    }

    private UnitConverter() {
        // utility class
    }

    private static void register(String unit, Dimension dimension, double toBase, String... aliases) {
        DIMENSIONS.put(unit, dimension);
        TO_BASE.put(unit, toBase);
        ALIASES.put(unit.toLowerCase(Locale.ROOT), unit);
        for (String alias : aliases) {
            ALIASES.put(alias, unit);
        }
    }

    /**
     * Maps user or seed input such as "Grams", "litre" or "tbsp" to the
     * canonical unit code, or returns null if the unit is not supported.
     */
    public static String normalizeUnit(String unit) {
        if (unit == null) {
            return null;
        }
        String key = unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
        return ALIASES.get(key);
    }

    public static boolean isSupported(String unit) {
        return normalizeUnit(unit) != null;
    }

    /** Returns the dimension of a unit, or null if the unit is unknown. */
    public static Dimension getDimension(String unit) {
        String canonical = normalizeUnit(unit);
        return canonical == null ? null : DIMENSIONS.get(canonical);
    }

    /** True when both units are known and measure the same kind of quantity. */
    public static boolean areCompatible(String unitA, String unitB) {
        Dimension a = getDimension(unitA);
        return a != null && a == getDimension(unitB);
    }

    /**
     * Converts a quantity to its dimension's base unit (pcs, g or ml).
     *
     * @throws IllegalArgumentException if the unit is not supported
     */
    public static double toBase(double quantity, String unit) {
        String canonical = normalizeUnit(unit);
        if (canonical == null) {
            throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
        return quantity * TO_BASE.get(canonical);
    }

    /**
     * Converts a quantity expressed in the base unit back to the given unit.
     *
     * @throws IllegalArgumentException if the unit is not supported
     */
    public static double fromBase(double baseQuantity, String unit) {
        String canonical = normalizeUnit(unit);
        if (canonical == null) {
            throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
        return baseQuantity / TO_BASE.get(canonical);
    }

    /**
     * Converts between two compatible units.
     *
     * @throws IllegalArgumentException if either unit is unknown or the units are incompatible
     */
    public static double convert(double quantity, String fromUnit, String toUnit) {
        if (!areCompatible(fromUnit, toUnit)) {
            throw new IllegalArgumentException("Cannot convert " + fromUnit + " to " + toUnit);
        }
        return fromBase(toBase(quantity, fromUnit), toUnit);
    }
}
