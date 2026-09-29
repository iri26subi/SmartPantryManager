package com.smartpantry.manager.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.AppSettings;
import com.smartpantry.manager.data.DatabaseTestHelper;
import com.smartpantry.manager.data.DemoPantryData;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.data.RecipeRepository;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;

import java.time.LocalDate;
import java.util.List;

/**
 * Screen-level tests: every Activity inflates with the real layouts and
 * theme, navigation fires the expected explicit Intents, and the Add/Edit
 * form validates input before touching the database.
 */
@RunWith(RobolectricTestRunner.class)
public class ScreenFlowTest {

    private PantryRepository pantryRepository;

    @Before
    public void setUp() {
        DatabaseTestHelper.reset();
        pantryRepository = new PantryRepository(RuntimeEnvironment.getApplication());
    }

    @After
    public void tearDown() {
        DatabaseTestHelper.reset();
    }

    // ---- Pantry list ------------------------------------------------------------

    @Test
    public void pantryScreenShowsEmptyStateWhenPantryIsEmpty() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.emptyState).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.pantryRecyclerView).getVisibility());
    }

    @Test
    public void addButtonShowsItsLabelAgainWhenThePantryBecomesEmpty() {
        pantryRepository.insertAll(DemoPantryData.getItems(LocalDate.now()));
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        ExtendedFloatingActionButton fab = controller.get().findViewById(R.id.fabAdd);
        fab.shrink(); // as if the user had scrolled down the list
        ShadowLooper.idleMainLooper();

        controller.pause();
        pantryRepository.deleteAll();
        controller.resume();
        ShadowLooper.idleMainLooper();

        assertTrue(fab.isExtended());
    }

    @Test
    public void pantryScreenListsItemsFromTheDatabase() {
        pantryRepository.insertAll(DemoPantryData.getItems(LocalDate.now()));
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        RecyclerView list = activity.findViewById(R.id.pantryRecyclerView);
        assertEquals(View.VISIBLE, list.getVisibility());
        assertEquals(12, list.getAdapter().getItemCount());
        assertEquals(View.GONE, activity.findViewById(R.id.emptyState).getVisibility());
        // The sample milk expires in 2 days, so the expiry banner is shown.
        assertEquals(View.VISIBLE, activity.findViewById(R.id.expiryBanner).getVisibility());
    }

    @Test
    public void addButtonOpensAddEditScreen() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        activity.findViewById(R.id.fabAdd).performClick();
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(AddEditIngredientActivity.class.getName(), next.getComponent().getClassName());
        assertFalse(next.hasExtra(AddEditIngredientActivity.EXTRA_ITEM_ID));
    }

    @Test
    public void bottomNavigationOpensRecipesAndSettings() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);

        nav.setSelectedItemId(R.id.nav_recipes);
        assertEquals(SuggestedRecipesActivity.class.getName(),
                shadowOf(activity).getNextStartedActivity().getComponent().getClassName());

        nav.setSelectedItemId(R.id.nav_settings);
        assertEquals(SettingsActivity.class.getName(),
                shadowOf(activity).getNextStartedActivity().getComponent().getClassName());
        // The Pantry tab stays highlighted on the Pantry screen.
        assertEquals(R.id.nav_pantry, nav.getSelectedItemId());
    }

    // ---- Add / Edit -----------------------------------------------------------------

    @Test
    public void blankNameAndZeroQuantityAreRejected() {
        AddEditIngredientActivity activity = Robolectric.buildActivity(AddEditIngredientActivity.class,
                AddEditIngredientActivity.newAddIntent(RuntimeEnvironment.getApplication())).setup().get();

        this.<TextInputEditText>view(activity, R.id.nameInput).setText("   ");
        this.<TextInputEditText>view(activity, R.id.quantityInput).setText("0");
        activity.findViewById(R.id.saveButton).performClick();

        assertNotNull(this.<TextInputLayout>view(activity, R.id.nameLayout).getError());
        assertNotNull(this.<TextInputLayout>view(activity, R.id.quantityLayout).getError());
        assertFalse(activity.isFinishing());
        assertEquals(0, pantryRepository.count());
    }

    @Test
    public void nonNumericQuantityIsRejected() {
        AddEditIngredientActivity activity = Robolectric.buildActivity(AddEditIngredientActivity.class).setup().get();
        this.<TextInputEditText>view(activity, R.id.nameInput).setText("Eggs");
        this.<TextInputEditText>view(activity, R.id.quantityInput).setText("-3");
        activity.findViewById(R.id.saveButton).performClick();
        assertNotNull(this.<TextInputLayout>view(activity, R.id.quantityLayout).getError());
        assertEquals(0, pantryRepository.count());
    }

    @Test
    public void validIngredientIsSavedAndScreenCloses() {
        AddEditIngredientActivity activity = Robolectric.buildActivity(AddEditIngredientActivity.class).setup().get();
        this.<TextInputEditText>view(activity, R.id.nameInput).setText("  Free-range eggs ");
        this.<TextInputEditText>view(activity, R.id.quantityInput).setText("6");
        this.<MaterialAutoCompleteTextView>view(activity, R.id.unitInput).setText("pcs", false);
        activity.findViewById(R.id.saveButton).performClick();

        assertTrue(activity.isFinishing());
        assertEquals(Activity.RESULT_OK, shadowOf(activity).getResultCode());
        List<PantryItem> items = pantryRepository.getAll();
        assertEquals(1, items.size());
        assertEquals("Free-range eggs", items.get(0).getName());
        assertEquals(6, items.get(0).getQuantity(), 1e-9);
        assertNull(items.get(0).getExpiryDate());
    }

    @Test
    public void editModeLoadsAndUpdatesTheItem() {
        long id = pantryRepository.insert(new PantryItem("Milk", 1, "L", "2026-12-01"));
        Intent intent = AddEditIngredientActivity.newEditIntent(RuntimeEnvironment.getApplication(), id);
        AddEditIngredientActivity activity = Robolectric.buildActivity(AddEditIngredientActivity.class, intent)
                .setup().get();

        TextInputEditText quantity = view(activity, R.id.quantityInput);
        assertEquals("Milk", this.<TextInputEditText>view(activity, R.id.nameInput).getText().toString());
        assertEquals("1", quantity.getText().toString());
        assertEquals("L", this.<MaterialAutoCompleteTextView>view(activity, R.id.unitInput).getText().toString());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.deleteButton).getVisibility());

        quantity.setText("0.5");
        activity.findViewById(R.id.saveButton).performClick();

        assertTrue(activity.isFinishing());
        PantryItem updated = pantryRepository.getById(id);
        assertEquals(0.5, updated.getQuantity(), 1e-9);
        assertEquals("2026-12-01", updated.getExpiryDate());
    }

    // ---- Suggested recipes ------------------------------------------------------

    @Test
    public void suggestionsShowOnlyStrictMatches() {
        pantryRepository.insertAll(DemoPantryData.getItems(LocalDate.now()));
        SuggestedRecipesActivity activity = Robolectric.buildActivity(SuggestedRecipesActivity.class).setup().get();
        RecyclerView list = activity.findViewById(R.id.recipeRecyclerView);
        int readyCount = list.getAdapter().getItemCount();
        assertTrue(readyCount > 0);
        assertEquals(View.GONE, activity.findViewById(R.id.emptyState).getVisibility());

        // Switch to the separate "Almost there" tab.
        activity.findViewById(R.id.almostButton).performClick();
        ShadowLooper.idleMainLooper();
        int almostCount = list.getAdapter().getItemCount();
        assertTrue(almostCount > 0);
        TextView summary = activity.findViewById(R.id.summaryText);
        assertEquals(activity.getString(R.string.summary_almost), summary.getText().toString());
    }

    @Test
    public void emptyPantryShowsNoMatchMessage() {
        SuggestedRecipesActivity activity = Robolectric.buildActivity(SuggestedRecipesActivity.class).setup().get();
        assertEquals(0, this.<RecyclerView>view(activity, R.id.recipeRecyclerView).getAdapter().getItemCount());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.emptyState).getVisibility());
        assertEquals(activity.getString(R.string.empty_ready_title),
                this.<TextView>view(activity, R.id.emptyTitle).getText().toString());
    }

    @Test
    public void suggestionsRefreshWhenReturningToTheScreen() {
        ActivityController<SuggestedRecipesActivity> controller =
                Robolectric.buildActivity(SuggestedRecipesActivity.class).setup();
        RecyclerView list = controller.get().findViewById(R.id.recipeRecyclerView);
        assertEquals(0, list.getAdapter().getItemCount());

        controller.pause();
        pantryRepository.insertAll(DemoPantryData.getItems(LocalDate.now()));
        controller.resume();

        assertTrue(list.getAdapter().getItemCount() > 0);
    }

    // ---- Recipe detail ---------------------------------------------------------------

    @Test
    public void recipeDetailShowsIngredientsAndMethod() {
        Recipe recipe = new RecipeRepository(RuntimeEnvironment.getApplication()).getAllWithIngredients().get(0);
        Intent intent = RecipeDetailActivity.newIntent(RuntimeEnvironment.getApplication(), recipe.getId());
        RecipeDetailActivity activity = Robolectric.buildActivity(RecipeDetailActivity.class, intent).setup().get();

        assertEquals(recipe.getName(), this.<TextView>view(activity, R.id.recipeName).getText().toString());
        RecyclerView ingredients = activity.findViewById(R.id.ingredientsRecyclerView);
        assertEquals(recipe.getIngredients().size(), ingredients.getAdapter().getItemCount());
        String method = this.<TextView>view(activity, R.id.instructionsText).getText().toString();
        assertTrue(method.startsWith("1. "));
        assertEquals(activity.getString(R.string.status_cannot_cook, recipe.getIngredients().size()),
                this.<TextView>view(activity, R.id.statusText).getText().toString());

        activity.findViewById(R.id.backButton).performClick();
        assertTrue(activity.isFinishing());
    }

    @Test
    public void recipeDetailWithUnknownIdCloses() {
        Intent intent = RecipeDetailActivity.newIntent(RuntimeEnvironment.getApplication(), 999_999);
        RecipeDetailActivity activity = Robolectric.buildActivity(RecipeDetailActivity.class, intent).setup().get();
        assertTrue(activity.isFinishing());
    }

    // ---- Settings --------------------------------------------------------------------

    @Test
    public void settingsArePersisted() {
        SettingsActivity activity = Robolectric.buildActivity(SettingsActivity.class).setup().get();
        MaterialSwitch alerts = activity.findViewById(R.id.expiryAlertsSwitch);
        MaterialSwitch excludeExpired = activity.findViewById(R.id.excludeExpiredSwitch);
        assertTrue(alerts.isChecked());
        assertTrue(excludeExpired.isChecked());

        alerts.performClick();
        excludeExpired.performClick();

        AppSettings settings = new AppSettings(RuntimeEnvironment.getApplication());
        assertFalse(settings.isExpiryAlertsEnabled());
        assertFalse(settings.isExcludeExpiredEnabled());
        assertFalse(activity.findViewById(R.id.expiryWindowLayout).isEnabled());
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T view(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }
}
