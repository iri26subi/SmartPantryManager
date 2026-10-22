package com.smartpantry.manager.matching;

import com.smartpantry.manager.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The outcome of checking a whole recipe against the pantry: one
 * {@link IngredientCheck} per ingredient line of the recipe.
 */
public class MatchResult {

    private final Recipe recipe;
    private final List<IngredientCheck> checks;

    public MatchResult(Recipe recipe, List<IngredientCheck> checks) {
        this.recipe = recipe;
        this.checks = Collections.unmodifiableList(new ArrayList<>(checks));
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<IngredientCheck> getChecks() {
        return checks;
    }

    /**
     * The strict rule: a recipe matches only if it has at least one ingredient
     * and EVERY ingredient is available in at least the required quantity.
     */
    public boolean isMatch() {
        if (checks.isEmpty()) {
            return false;
        }
        for (IngredientCheck check : checks) {
            if (!check.isSatisfied()) {
                return false;
            }
        }
        return true;
    }

    /** Ingredients that are missing, insufficient or in an incompatible unit. */
    public List<IngredientCheck> getUnmetChecks() {
        List<IngredientCheck> unmet = new ArrayList<>();
        for (IngredientCheck check : checks) {
            if (!check.isSatisfied()) {
                unmet.add(check);
            }
        }
        return unmet;
    }

    public int getUnmetCount() {
        return getUnmetChecks().size();
    }
}
