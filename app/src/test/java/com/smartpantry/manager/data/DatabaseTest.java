package com.smartpantry.manager.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.smartpantry.manager.matching.RecipeMatcher;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercises the real SQLite database code (DatabaseHelper and the two
 * repositories) on the JVM using Robolectric.
 */
@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {

    private Context context;
    private PantryRepository pantryRepository;
    private RecipeRepository recipeRepository;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstanceForTesting();
        context = RuntimeEnvironment.getApplication();
        pantryRepository = new PantryRepository(context);
        recipeRepository = new RecipeRepository(context);
    }

    @After
    public void tearDown() {
        DatabaseHelper.resetInstanceForTesting();
    }

    /** Simulates closing the app and opening it again: a brand-new helper on the same file. */
    private void reopenDatabase() {
        DatabaseHelper.resetInstanceForTesting();
        pantryRepository = new PantryRepository(context);
        recipeRepository = new RecipeRepository(context);
    }

    @Test
    public void recipesAreSeededOnFirstLaunch() {
        List<Recipe> recipes = recipeRepository.getAllWithIngredients();
        assertEquals(RecipeSeedData.getRecipes().size(), recipes.size());
        assertTrue(recipes.size() >= 20);
        for (Recipe recipe : recipes) {
            assertFalse(recipe.getName() + " has no ingredients", recipe.getIngredients().isEmpty());
        }
    }

    @Test
    public void recipesAreNotDuplicatedOnLaterLaunches() {
        long firstCount = recipeRepository.count();
        reopenDatabase();
        assertEquals(firstCount, recipeRepository.count());

        // Calling the seeding method again directly must not add anything either.
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        helper.seedRecipes(helper.getWritableDatabase());
        assertEquals(firstCount, recipeRepository.count());
    }

    @Test
    public void getByIdReturnsRecipeWithIngredientsAndSteps() {
        Recipe first = recipeRepository.getAllWithIngredients().get(0);
        Recipe loaded = recipeRepository.getById(first.getId());
        assertNotNull(loaded);
        assertEquals(first.getName(), loaded.getName());
        assertEquals(first.getIngredients().size(), loaded.getIngredients().size());
        assertTrue(loaded.getSteps().size() >= 3);
        assertNull(recipeRepository.getById(999_999));
    }

    @Test
    public void pantryCrudCycle() {
        // Create
        PantryItem eggs = new PantryItem("Eggs", 6, "pcs", "2026-11-01");
        long id = pantryRepository.insert(eggs);
        assertTrue(id > 0);

        // Read
        PantryItem loaded = pantryRepository.getById(id);
        assertNotNull(loaded);
        assertEquals("Eggs", loaded.getName());
        assertEquals(6, loaded.getQuantity(), 1e-9);
        assertEquals("pcs", loaded.getUnit());
        assertEquals("2026-11-01", loaded.getExpiryDate());
        assertEquals(1, pantryRepository.getAll().size());

        // Update
        loaded.setQuantity(2);
        loaded.setExpiryDate(null);
        assertTrue(pantryRepository.update(loaded));
        PantryItem updated = pantryRepository.getById(id);
        assertEquals(2, updated.getQuantity(), 1e-9);
        assertNull(updated.getExpiryDate());

        // Delete
        assertTrue(pantryRepository.delete(id));
        assertNull(pantryRepository.getById(id));
        assertFalse(pantryRepository.delete(id));
        assertEquals(0, pantryRepository.count());
    }

    @Test
    public void pantryIsSortedAlphabeticallyIgnoringCase() {
        pantryRepository.insert(new PantryItem("tomatoes", 3, "pcs", null));
        pantryRepository.insert(new PantryItem("Butter", 250, "g", null));
        pantryRepository.insert(new PantryItem("apples", 4, "pcs", null));
        List<PantryItem> items = pantryRepository.getAll();
        assertEquals("apples", items.get(0).getName());
        assertEquals("Butter", items.get(1).getName());
        assertEquals("tomatoes", items.get(2).getName());
    }

    @Test
    public void pantryDataPersistsAfterReopening() {
        pantryRepository.insert(new PantryItem("Milk", 1, "L", "2026-10-25"));
        reopenDatabase();
        List<PantryItem> items = pantryRepository.getAll();
        assertEquals(1, items.size());
        assertEquals("Milk", items.get(0).getName());
        assertEquals("2026-10-25", items.get(0).getExpiryDate());
    }

    @Test
    public void databaseRejectsNonPositiveQuantities() {
        // The CHECK constraint is a second line of defence behind form validation.
        assertEquals(-1, pantryRepository.insert(new PantryItem("Flour", 0, "g", null)));
        assertEquals(-1, pantryRepository.insert(new PantryItem("Flour", -5, "g", null)));
        assertEquals(0, pantryRepository.count());
    }

    @Test
    public void deletingARecipeCascadesToItsIngredients() {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        Recipe recipe = recipeRepository.getAllWithIngredients().get(0);
        assertFalse(recipeRepository.getIngredients(recipe.getId()).isEmpty());

        db.delete(DatabaseHelper.TABLE_RECIPES, DatabaseHelper.COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});

        assertTrue(recipeRepository.getIngredients(recipe.getId()).isEmpty());
    }

    @Test
    public void upgradeKeepsPantryAndReseedsRecipes() {
        pantryRepository.insert(new PantryItem("Rice", 1, "kg", null));
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        helper.onUpgrade(db, 1, 2);

        assertEquals(1, pantryRepository.count());
        assertEquals(RecipeSeedData.getRecipes().size(), recipeRepository.count());
        assertEquals(RecipeSeedData.getRecipes().size(),
                DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_RECIPES));
    }

    @Test
    public void suggestionsChangeWhenThePantryChanges() {
        // The scenario from the video brief: remove one ingredient and watch a recipe disappear.
        LocalDate today = LocalDate.now();
        assertTrue(pantryRepository.insertAll(DemoPantryData.getItems(today)));
        RecipeMatcher matcher = new RecipeMatcher(today, true);

        assertTrue(names(matcher.findStrictMatches(recipeRepository.getAllWithIngredients(),
                pantryRepository.getAll())).contains("Scrambled Eggs"));

        for (PantryItem item : pantryRepository.getAll()) {
            if (item.getName().equals("Eggs")) {
                item.setQuantity(2); // Scrambled Eggs needs 3
                pantryRepository.update(item);
            }
        }
        assertFalse(names(matcher.findStrictMatches(recipeRepository.getAllWithIngredients(),
                pantryRepository.getAll())).contains("Scrambled Eggs"));

        pantryRepository.insert(new PantryItem("egg", 1, "pcs", null)); // duplicate row, aggregated
        assertTrue(names(matcher.findStrictMatches(recipeRepository.getAllWithIngredients(),
                pantryRepository.getAll())).contains("Scrambled Eggs"));
    }

    private static List<String> names(List<Recipe> recipes) {
        List<String> names = new ArrayList<>();
        for (Recipe recipe : recipes) {
            names.add(recipe.getName());
        }
        return names;
    }
}
