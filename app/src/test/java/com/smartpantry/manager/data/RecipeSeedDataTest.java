package com.smartpantry.manager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.manager.matching.IngredientNormalizer;
import com.smartpantry.manager.matching.MatchResult;
import com.smartpantry.manager.matching.RecipeMatcher;
import com.smartpantry.manager.matching.UnitConverter;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Sanity checks on the seeded recipes and the sample pantry. */
public class RecipeSeedDataTest {

    private final List<Recipe> recipes = RecipeSeedData.getRecipes();

    @Test
    public void thereAreAtLeastTwentyRecipes() {
        assertTrue("found " + recipes.size(), recipes.size() >= 20);
    }

    @Test
    public void recipeNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (Recipe recipe : recipes) {
            assertTrue("duplicate " + recipe.getName(), names.add(recipe.getName().toLowerCase()));
        }
    }

    @Test
    public void everyRecipeIsComplete() {
        for (Recipe recipe : recipes) {
            String name = recipe.getName();
            assertFalse(name, recipe.getDescription().trim().isEmpty());
            assertTrue(name + " needs ingredients", recipe.getIngredients().size() >= 3);
            assertTrue(name + " needs at least 3 steps", recipe.getSteps().size() >= 3);
            assertTrue(name, recipe.getPrepMinutes() > 0);
            assertTrue(name, recipe.getServings() > 0);
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                String label = name + ": " + ingredient.getName();
                assertTrue(label, ingredient.getQuantity() > 0);
                assertTrue(label + " unit " + ingredient.getUnit(), UnitConverter.isSupported(ingredient.getUnit()));
                assertFalse(label, IngredientNormalizer.normalize(ingredient.getName()).isEmpty());
            }
        }
    }

    @Test
    public void samplePantryDemonstratesTheStrictRule() {
        LocalDate today = LocalDate.of(2026, 10, 20);
        List<PantryItem> pantry = DemoPantryData.getItems(today);
        RecipeMatcher matcher = new RecipeMatcher(today, true);

        List<String> matched = new ArrayList<>();
        for (Recipe recipe : matcher.findStrictMatches(recipes, pantry)) {
            matched.add(recipe.getName());
        }
        assertTrue(matched.toString(), matched.contains("Scrambled Eggs"));
        assertTrue(matched.toString(), matched.contains("Tomato Pasta"));
        assertTrue(matched.toString(), matched.contains("Grilled Cheese Sandwich"));
        // Needs parmesan, which the sample pantry does not contain.
        assertFalse(matched.toString(), matched.contains("Garlic Butter Pasta"));
        // Needs 6 tomatoes and vegetable stock; the sample pantry has 5 tomatoes and no stock.
        assertFalse(matched.toString(), matched.contains("Tomato Soup"));

        List<String> almost = new ArrayList<>();
        for (MatchResult result : matcher.findAlmostThere(recipes, pantry)) {
            almost.add(result.getRecipe().getName());
        }
        assertTrue(almost.toString(), almost.contains("Garlic Butter Pasta"));
    }
}
