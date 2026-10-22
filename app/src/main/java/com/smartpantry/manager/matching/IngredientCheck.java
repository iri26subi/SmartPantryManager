package com.smartpantry.manager.matching;

import com.smartpantry.manager.model.RecipeIngredient;

/**
 * The outcome of checking one recipe ingredient against the pantry.
 */
public class IngredientCheck {

    public enum Status {
        /** The pantry holds at least the required quantity. */
        AVAILABLE,
        /** The ingredient is in the pantry, but not enough of it. */
        INSUFFICIENT,
        /** The ingredient is not in the pantry at all. */
        MISSING,
        /**
         * The ingredient is in the pantry, but only in a unit that cannot be
         * reliably compared with the recipe's unit (e.g. grams vs millilitres),
         * or the recipe uses an unknown unit.
         */
        INCOMPATIBLE_UNIT
    }

    private final RecipeIngredient ingredient;
    private final Status status;
    private final double availableQuantity; // expressed in the recipe ingredient's unit

    public IngredientCheck(RecipeIngredient ingredient, Status status, double availableQuantity) {
        this.ingredient = ingredient;
        this.status = status;
        this.availableQuantity = availableQuantity;
    }

    public RecipeIngredient getIngredient() {
        return ingredient;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSatisfied() {
        return status == Status.AVAILABLE;
    }

    /** How much of the ingredient the pantry holds, converted to the recipe's unit. */
    public double getAvailableQuantity() {
        return availableQuantity;
    }
}
