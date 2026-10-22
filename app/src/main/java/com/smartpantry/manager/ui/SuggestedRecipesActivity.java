package com.smartpantry.manager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.AppSettings;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.data.RecipeRepository;
import com.smartpantry.manager.matching.IngredientCheck;
import com.smartpantry.manager.matching.MatchResult;
import com.smartpantry.manager.matching.RecipeMatcher;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;
import com.smartpantry.manager.ui.adapter.RecipeAdapter;
import com.smartpantry.manager.util.QuantityFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen.
 *
 * Loads all recipes and the current pantry from SQLite and runs the strict
 * RecipeMatcher. The "Ready to cook" tab lists ONLY recipes whose every
 * ingredient is in the pantry in sufficient quantity. The optional "Almost
 * there" tab is kept separate and lists recipes missing exactly one ingredient.
 *
 * Matching is re-run in onResume(), so suggestions always reflect the latest
 * pantry after the user adds, edits or deletes an ingredient.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.OnRecipeClickListener {

    private static final String STATE_SHOW_ALMOST = "state_show_almost";

    private RecipeRepository recipeRepository;
    private PantryRepository pantryRepository;
    private AppSettings settings;
    private RecipeAdapter adapter;

    private MaterialButtonToggleGroup modeToggle;
    private MaterialButton readyButton;
    private MaterialButton almostButton;
    private TextView summaryText;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyMessage;

    private boolean showAlmostThere;
    private int totalRecipeCount;
    private List<Recipe> strictMatches = new ArrayList<>();
    private List<MatchResult> almostThere = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        applyWindowInsets(false);
        setUpBottomNavigation(R.id.nav_recipes);

        recipeRepository = new RecipeRepository(this);
        pantryRepository = new PantryRepository(this);
        settings = new AppSettings(this);

        modeToggle = findViewById(R.id.modeToggle);
        readyButton = findViewById(R.id.readyButton);
        almostButton = findViewById(R.id.almostButton);
        summaryText = findViewById(R.id.summaryText);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptyMessage = findViewById(R.id.emptyMessage);

        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            showAlmostThere = savedInstanceState.getBoolean(STATE_SHOW_ALMOST, false);
        }
        modeToggle.check(showAlmostThere ? R.id.almostButton : R.id.readyButton);
        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showAlmostThere = checkedId == R.id.almostButton;
                render();
            }
        });

        MaterialButton goToPantryButton = findViewById(R.id.goToPantryButton);
        goToPantryButton.setOnClickListener(v -> navigateToTab(R.id.nav_pantry));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOW_ALMOST, showAlmostThere);
    }

    /** Reads recipes and pantry from SQLite and runs the strict-matching algorithm. */
    private void refreshSuggestions() {
        List<Recipe> recipes = recipeRepository.getAllWithIngredients();
        List<PantryItem> pantry = pantryRepository.getAll();
        RecipeMatcher matcher = new RecipeMatcher(LocalDate.now(), settings.isExcludeExpiredEnabled());

        totalRecipeCount = recipes.size();
        strictMatches = matcher.findStrictMatches(recipes, pantry);
        almostThere = matcher.findAlmostThere(recipes, pantry);

        readyButton.setText(getString(R.string.mode_ready_count, strictMatches.size()));
        almostButton.setText(getString(R.string.mode_almost_count, almostThere.size()));
        render();
    }

    private void render() {
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        if (showAlmostThere) {
            for (MatchResult result : almostThere) {
                rows.add(new RecipeAdapter.Row(result.getRecipe(), describeMissing(result.getUnmetChecks().get(0))));
            }
            summaryText.setText(R.string.summary_almost);
            emptyTitle.setText(R.string.empty_almost_title);
            emptyMessage.setText(R.string.empty_almost_message);
        } else {
            for (Recipe recipe : strictMatches) {
                rows.add(new RecipeAdapter.Row(recipe, null));
            }
            summaryText.setText(getString(R.string.summary_ready, strictMatches.size(), totalRecipeCount));
            emptyTitle.setText(R.string.empty_ready_title);
            emptyMessage.setText(R.string.empty_ready_message);
        }
        adapter.setRows(rows);

        boolean isEmpty = rows.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /** Builds a short explanation such as "egg (need 3 pcs, have 2 pcs)". */
    private String describeMissing(IngredientCheck check) {
        RecipeIngredient ingredient = check.getIngredient();
        String required = QuantityFormatter.format(ingredient.getQuantity(), ingredient.getUnit());
        switch (check.getStatus()) {
            case INSUFFICIENT:
                return getString(R.string.missing_need_more, ingredient.getName(), required,
                        QuantityFormatter.format(check.getAvailableQuantity(), ingredient.getUnit()));
            case INCOMPATIBLE_UNIT:
                return getString(R.string.missing_incompatible, ingredient.getName());
            default:
                return getString(R.string.missing_item, required, ingredient.getName());
        }
    }

    /** Opens the detail screen, passing the recipe id through an explicit Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        startActivity(RecipeDetailActivity.newIntent(this, recipe.getId()));
    }
}
