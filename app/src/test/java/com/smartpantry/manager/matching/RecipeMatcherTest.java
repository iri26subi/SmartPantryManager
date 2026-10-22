package com.smartpantry.manager.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the strict-matching rule: a recipe is suggested only when EVERY
 * ingredient is in the pantry in at least the required quantity.
 */
public class RecipeMatcherTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private final RecipeMatcher matcher = new RecipeMatcher(TODAY, false);

    // ---- helpers ---------------------------------------------------------

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(0, name, "", "Step one", 10, 1);
        recipe.setIngredients(new ArrayList<>(Arrays.asList(ingredients)));
        return recipe;
    }

    private static RecipeIngredient need(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static PantryItem have(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    private static List<PantryItem> pantry(PantryItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    // ---- examples from the brief ----------------------------------------

    @Test
    public void recipeNeeds3Eggs_pantryHas4_isEligible() {
        Recipe omelette = recipe("Omelette", need("egg", 3, "pcs"));
        assertTrue(matcher.canMake(omelette, pantry(have("egg", 4, "pcs"))));
    }

    @Test
    public void recipeNeeds3Eggs_pantryHasExactly3_isEligible() {
        Recipe omelette = recipe("Omelette", need("egg", 3, "pcs"));
        assertTrue(matcher.canMake(omelette, pantry(have("egg", 3, "pcs"))));
    }

    @Test
    public void recipeNeeds3Eggs_pantryHas2_isNotEligible() {
        Recipe omelette = recipe("Omelette", need("egg", 3, "pcs"));
        MatchResult result = matcher.evaluate(omelette, pantry(have("egg", 2, "pcs")));
        assertFalse(result.isMatch());
        assertEquals(IngredientCheck.Status.INSUFFICIENT, result.getChecks().get(0).getStatus());
        assertEquals(2.0, result.getChecks().get(0).getAvailableQuantity(), 1e-9);
    }

    @Test
    public void recipeNeedsEggsFlourMilk_milkMissing_isNotEligible() {
        Recipe pancakes = recipe("Pancakes",
                need("egg", 2, "pcs"), need("flour", 150, "g"), need("milk", 250, "ml"));
        MatchResult result = matcher.evaluate(pancakes,
                pantry(have("egg", 6, "pcs"), have("flour", 1, "kg")));
        assertFalse(result.isMatch());
        assertEquals(1, result.getUnmetCount());
        assertEquals(IngredientCheck.Status.MISSING, result.getUnmetChecks().get(0).getStatus());
        assertEquals("milk", result.getUnmetChecks().get(0).getIngredient().getName());
    }

    @Test
    public void recipeNeeds5Ingredients_only4Available_isNotEligible() {
        Recipe recipe = recipe("Five",
                need("egg", 1, "pcs"), need("flour", 100, "g"), need("milk", 100, "ml"),
                need("butter", 20, "g"), need("sugar", 10, "g"));
        List<PantryItem> items = pantry(have("egg", 6, "pcs"), have("flour", 500, "g"),
                have("milk", 1, "L"), have("butter", 250, "g"));
        assertFalse(matcher.canMake(recipe, items));
        assertTrue(matcher.findStrictMatches(Arrays.asList(recipe), items).isEmpty());
    }

    @Test
    public void allIngredientsAvailable_isEligible() {
        Recipe recipe = recipe("Five",
                need("egg", 1, "pcs"), need("flour", 100, "g"), need("milk", 100, "ml"),
                need("butter", 20, "g"), need("sugar", 10, "g"));
        List<PantryItem> items = pantry(have("egg", 6, "pcs"), have("flour", 500, "g"),
                have("milk", 1, "L"), have("butter", 250, "g"), have("sugar", 1, "kg"));
        assertTrue(matcher.canMake(recipe, items));
    }

    // ---- name normalisation ----------------------------------------------

    @Test
    public void pluralAndCaseDifferencesStillMatch() {
        Recipe salad = recipe("Salad", need("tomato", 2, "pcs"), need("Red Onion", 1, "pcs"));
        assertTrue(matcher.canMake(salad, pantry(have("Tomatoes", 3, "pcs"), have("red onions ", 1, "pcs"))));
    }

    @Test
    public void unrelatedIngredientsDoNotMatch() {
        assertFalse(matcher.canMake(recipe("Toast", need("butter", 10, "g")),
                pantry(have("peanut butter", 500, "g"))));
        assertFalse(matcher.canMake(recipe("Omelette", need("egg", 2, "pcs")),
                pantry(have("egg whites", 6, "pcs"))));
        assertFalse(matcher.canMake(recipe("Mojito", need("lime", 1, "pcs")),
                pantry(have("lemon", 4, "pcs"))));
    }

    // ---- units -------------------------------------------------------------

    @Test
    public void compatibleUnitsAreConverted() {
        // 1 cup (250 ml) of milk against 1 L in the pantry
        assertTrue(matcher.canMake(recipe("Pancakes", need("milk", 1, "cup")), pantry(have("milk", 1, "L"))));
        // 2 tbsp (30 ml) of oil against 25 ml in the pantry
        assertFalse(matcher.canMake(recipe("Stir-fry", need("oil", 2, "tbsp")), pantry(have("oil", 25, "ml"))));
        // 150 g of flour against 0.2 kg in the pantry
        assertTrue(matcher.canMake(recipe("Bread", need("flour", 150, "g")), pantry(have("flour", 0.2, "kg"))));
    }

    @Test
    public void incompatibleUnitsExcludeTheRecipe() {
        // Salt in grams cannot be compared with salt in teaspoons (needs density).
        MatchResult result = matcher.evaluate(recipe("Soup", need("salt", 1, "tsp")),
                pantry(have("salt", 500, "g")));
        assertFalse(result.isMatch());
        assertEquals(IngredientCheck.Status.INCOMPATIBLE_UNIT, result.getChecks().get(0).getStatus());
    }

    @Test
    public void unknownRecipeUnitExcludesTheRecipe() {
        assertFalse(matcher.canMake(recipe("Mystery", need("flour", 1, "handful")),
                pantry(have("flour", 1, "kg"))));
    }

    @Test
    public void pantryItemWithUnknownUnitIsIgnored() {
        assertFalse(matcher.canMake(recipe("Bread", need("flour", 100, "g")),
                pantry(have("flour", 5, "bags"))));
    }

    // ---- duplicates ----------------------------------------------------------

    @Test
    public void duplicatePantryRowsAreAddedTogether() {
        Recipe recipe = recipe("Big omelette", need("egg", 5, "pcs"));
        assertTrue(matcher.canMake(recipe, pantry(have("Eggs", 3, "pcs"), have("egg", 2, "pcs"))));
        assertFalse(matcher.canMake(recipe, pantry(have("Eggs", 3, "pcs"), have("egg", 1, "pcs"))));
    }

    @Test
    public void duplicatePantryRowsInDifferentCompatibleUnitsAreConvertedAndAdded() {
        Recipe recipe = recipe("Cake", need("sugar", 1.2, "kg"));
        assertTrue(matcher.canMake(recipe, pantry(have("sugar", 1, "kg"), have("sugar", 200, "g"))));
    }

    @Test
    public void duplicateRowsInIncompatibleUnitsAreNotAdded() {
        // 300 g plus 300 ml of yoghurt must not be treated as 600 g.
        Recipe recipe = recipe("Smoothie", need("yoghurt", 500, "g"));
        assertFalse(matcher.canMake(recipe, pantry(have("yoghurt", 300, "g"), have("yoghurt", 300, "ml"))));
    }

    @Test
    public void recipeListingTheSameIngredientTwiceNeedsTheCombinedAmount() {
        Recipe recipe = recipe("Layered", need("butter", 50, "g"), need("butter", 50, "g"));
        assertFalse(matcher.canMake(recipe, pantry(have("butter", 80, "g"))));
        assertTrue(matcher.canMake(recipe, pantry(have("butter", 100, "g"))));
    }

    // ---- expiry --------------------------------------------------------------

    @Test
    public void expiredItemsAreIgnoredWhenTheSettingIsOn() {
        RecipeMatcher strictExpiry = new RecipeMatcher(TODAY, true);
        Recipe recipe = recipe("Cereal", need("milk", 200, "ml"));
        PantryItem expiredMilk = new PantryItem("milk", 1, "L", "2026-10-19");
        PantryItem freshMilk = new PantryItem("milk", 1, "L", "2026-10-20"); // expires today, still usable

        assertFalse(strictExpiry.canMake(recipe, pantry(expiredMilk)));
        assertTrue(strictExpiry.canMake(recipe, pantry(freshMilk)));
        assertTrue(matcher.canMake(recipe, pantry(expiredMilk))); // setting off
    }

    // ---- list operations -------------------------------------------------------

    @Test
    public void findStrictMatchesReturnsOnlyFullyCoveredRecipes() {
        Recipe eggs = recipe("Scrambled eggs", need("egg", 3, "pcs"), need("butter", 10, "g"));
        Recipe pancakes = recipe("Pancakes", need("egg", 2, "pcs"), need("flour", 150, "g"));
        Recipe omelette = recipe("Omelette", need("egg", 4, "pcs"));
        List<PantryItem> items = pantry(have("eggs", 3, "pcs"), have("butter", 250, "g"));

        List<Recipe> matches = matcher.findStrictMatches(Arrays.asList(eggs, pancakes, omelette), items);

        assertEquals(1, matches.size());
        assertEquals("Scrambled eggs", matches.get(0).getName());
    }

    @Test
    public void findAlmostThereReturnsRecipesMissingExactlyOneIngredient() {
        Recipe ready = recipe("Ready", need("egg", 1, "pcs"));
        Recipe oneShort = recipe("One short", need("egg", 1, "pcs"), need("flour", 100, "g"));
        Recipe twoShort = recipe("Two short", need("flour", 100, "g"), need("milk", 100, "ml"));
        List<PantryItem> items = pantry(have("egg", 2, "pcs"));

        List<MatchResult> almost = matcher.findAlmostThere(Arrays.asList(ready, oneShort, twoShort), items);

        assertEquals(1, almost.size());
        assertEquals("One short", almost.get(0).getRecipe().getName());
        assertFalse(almost.get(0).isMatch());
    }

    @Test
    public void recipeWithNoIngredientsIsNeverSuggested() {
        assertFalse(matcher.canMake(recipe("Empty"), pantry(have("egg", 1, "pcs"))));
    }

    @Test
    public void emptyPantryMatchesNothing() {
        assertFalse(matcher.canMake(recipe("Toast", need("bread slice", 2, "pcs")), new ArrayList<>()));
        assertFalse(matcher.canMake(recipe("Toast", need("bread slice", 2, "pcs")), null));
    }
}
