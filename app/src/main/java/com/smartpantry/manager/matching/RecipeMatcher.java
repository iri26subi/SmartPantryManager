package com.smartpantry.manager.matching;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.util.ExpiryUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the strict-matching rule of the Smart Pantry Manager.
 *
 * A recipe is suggested only if EVERY ingredient it requires is present in the
 * pantry in at least the required quantity. The algorithm:
 *
 *  1. Builds a pantry index: normalised ingredient name -> unit dimension ->
 *     total quantity in base units (pcs, g or ml). Duplicate pantry rows for
 *     the same ingredient (e.g. two egg boxes with different expiry dates) are
 *     added together, but only when their units are compatible.
 *  2. For each recipe ingredient, converts the required quantity to base units
 *     and compares it with the indexed total for the same name and dimension.
 *  3. The recipe matches only if every ingredient check is AVAILABLE. Missing
 *     ingredients, insufficient quantities, unknown units and incompatible
 *     units (e.g. pantry in grams but recipe in millilitres) all exclude it.
 *
 * The class has no Android dependencies so it can be unit-tested on the JVM.
 */
public class RecipeMatcher {

    /** Tolerance for floating-point rounding when comparing quantities. */
    private static final double EPSILON = 1e-6;

    private final LocalDate today;
    private final boolean excludeExpired;

    /** A matcher that counts every pantry item, regardless of expiry date. */
    public RecipeMatcher() {
        this(LocalDate.now(), false);
    }

    /**
     * @param today          the date used to decide whether an item has expired
     * @param excludeExpired when true, expired pantry items are ignored
     */
    public RecipeMatcher(LocalDate today, boolean excludeExpired) {
        this.today = today;
        this.excludeExpired = excludeExpired;
    }

    /** Returns only the recipes that satisfy the strict-matching rule. */
    public List<Recipe> findStrictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Map<UnitConverter.Dimension, Double>> index = buildPantryIndex(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (evaluate(recipe, index).isMatch()) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /**
     * Optional stretch feature: recipes that miss exactly ONE ingredient.
     * These are kept completely separate from the strict suggestions.
     */
    public List<MatchResult> findAlmostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Map<UnitConverter.Dimension, Double>> index = buildPantryIndex(pantry);
        List<MatchResult> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, index);
            if (!result.getChecks().isEmpty() && result.getUnmetCount() == 1) {
                almost.add(result);
            }
        }
        return almost;
    }

    /** Checks a single recipe against the pantry and explains each ingredient. */
    public MatchResult evaluate(Recipe recipe, List<PantryItem> pantry) {
        return evaluate(recipe, buildPantryIndex(pantry));
    }

    /** Convenience method: true when the recipe passes the strict rule. */
    public boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        return evaluate(recipe, pantry).isMatch();
    }

    private MatchResult evaluate(Recipe recipe,
                                 Map<String, Map<UnitConverter.Dimension, Double>> index) {
        // If a recipe lists the same ingredient twice in compatible units,
        // the pantry must cover the combined amount.
        Map<String, Double> requiredTotals = new HashMap<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            UnitConverter.Dimension dimension = UnitConverter.getDimension(ingredient.getUnit());
            if (dimension != null && ingredient.getQuantity() > 0) {
                String groupKey = IngredientNormalizer.normalize(ingredient.getName()) + "|" + dimension;
                double base = UnitConverter.toBase(ingredient.getQuantity(), ingredient.getUnit());
                Double previous = requiredTotals.get(groupKey);
                requiredTotals.put(groupKey, previous == null ? base : previous + base);
            }
        }

        List<IngredientCheck> checks = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            checks.add(checkIngredient(ingredient, index, requiredTotals));
        }
        return new MatchResult(recipe, checks);
    }

    private IngredientCheck checkIngredient(RecipeIngredient ingredient,
                                            Map<String, Map<UnitConverter.Dimension, Double>> index,
                                            Map<String, Double> requiredTotals) {
        String key = IngredientNormalizer.normalize(ingredient.getName());
        Map<UnitConverter.Dimension, Double> available = index.get(key);
        UnitConverter.Dimension dimension = UnitConverter.getDimension(ingredient.getUnit());

        if (available == null || available.isEmpty()) {
            return new IngredientCheck(ingredient, IngredientCheck.Status.MISSING, 0);
        }
        if (dimension == null || ingredient.getQuantity() <= 0 || !available.containsKey(dimension)) {
            // The pantry has this ingredient but its quantity cannot be compared
            // reliably with the recipe, so the recipe must not be suggested.
            return new IngredientCheck(ingredient, IngredientCheck.Status.INCOMPATIBLE_UNIT, 0);
        }

        double availableBase = available.get(dimension);
        double requiredBase = requiredTotals.get(key + "|" + dimension);
        double availableInRecipeUnit = UnitConverter.fromBase(availableBase, ingredient.getUnit());

        if (availableBase + EPSILON >= requiredBase) {
            return new IngredientCheck(ingredient, IngredientCheck.Status.AVAILABLE, availableInRecipeUnit);
        }
        return new IngredientCheck(ingredient, IngredientCheck.Status.INSUFFICIENT, availableInRecipeUnit);
    }

    /**
     * Groups the pantry by normalised name and unit dimension, summing the
     * quantities (in base units) of duplicate rows. Rows with an unknown unit,
     * a non-positive quantity, a blank name or (optionally) a past expiry date
     * are ignored.
     */
    Map<String, Map<UnitConverter.Dimension, Double>> buildPantryIndex(List<PantryItem> pantry) {
        Map<String, Map<UnitConverter.Dimension, Double>> index = new HashMap<>();
        if (pantry == null) {
            return index;
        }
        for (PantryItem item : pantry) {
            String key = IngredientNormalizer.normalize(item.getName());
            UnitConverter.Dimension dimension = UnitConverter.getDimension(item.getUnit());
            if (key.isEmpty() || dimension == null || item.getQuantity() <= 0) {
                continue;
            }
            if (excludeExpired && ExpiryUtils.isExpired(item, today)) {
                continue;
            }
            Map<UnitConverter.Dimension, Double> byDimension = index.get(key);
            if (byDimension == null) {
                byDimension = new EnumMap<>(UnitConverter.Dimension.class);
                index.put(key, byDimension);
            }
            double base = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Double previous = byDimension.get(dimension);
            byDimension.put(dimension, previous == null ? base : previous + base);
        }
        return index;
    }
}
