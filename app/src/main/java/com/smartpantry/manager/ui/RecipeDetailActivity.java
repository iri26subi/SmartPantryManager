package com.smartpantry.manager.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.AppSettings;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.data.RecipeRepository;
import com.smartpantry.manager.matching.MatchResult;
import com.smartpantry.manager.matching.RecipeMatcher;
import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.ui.adapter.IngredientCheckAdapter;

import java.time.LocalDate;
import java.util.List;

/**
 * Recipe detail screen: the recipe name, every required ingredient with its
 * quantity (and whether the pantry covers it), and the preparation method.
 * The recipe id arrives through the Intent that started this Activity.
 */
public class RecipeDetailActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "com.smartpantry.manager.EXTRA_RECIPE_ID";

    private MaterialToolbar toolbar;
    private RecyclerView ingredientsRecyclerView;

    public static Intent newIntent(Context context, long recipeId) {
        return new Intent(context, RecipeDetailActivity.class).putExtra(EXTRA_RECIPE_ID, recipeId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        applyWindowInsets(true);
        toolbar = setUpToolbarWithUp();

        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new RecipeRepository(this).getById(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.error_recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindRecipe(recipe);
    }

    private void bindRecipe(Recipe recipe) {
        toolbar.setTitle(recipe.getName());
        ((TextView) findViewById(R.id.recipeName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.recipeDescription)).setText(recipe.getDescription());
        ((TextView) findViewById(R.id.recipeMeta)).setText(getString(R.string.recipe_meta,
                recipe.getIngredients().size(), recipe.getPrepMinutes(), recipe.getServings()));

        // Check each ingredient against the current pantry with the same strict rule.
        RecipeMatcher matcher = new RecipeMatcher(LocalDate.now(), new AppSettings(this).isExcludeExpiredEnabled());
        MatchResult result = matcher.evaluate(recipe, new PantryRepository(this).getAll());
        ingredientsRecyclerView.setAdapter(new IngredientCheckAdapter(result.getChecks()));
        bindStatus(result);

        ((TextView) findViewById(R.id.instructionsText)).setText(formatSteps(recipe.getSteps()));
    }

    private void bindStatus(MatchResult result) {
        MaterialCardView statusCard = findViewById(R.id.statusCard);
        TextView statusText = findViewById(R.id.statusText);
        int background;
        int foreground;
        if (result.isMatch()) {
            statusText.setText(R.string.status_can_cook);
            statusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            background = MaterialColors.getColor(statusCard, com.google.android.material.R.attr.colorPrimaryContainer);
            foreground = MaterialColors.getColor(statusCard, com.google.android.material.R.attr.colorOnPrimaryContainer);
        } else {
            statusText.setText(getString(R.string.status_cannot_cook, result.getUnmetCount()));
            statusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_cancel, 0, 0, 0);
            background = MaterialColors.getColor(statusCard, com.google.android.material.R.attr.colorErrorContainer);
            foreground = MaterialColors.getColor(statusCard, com.google.android.material.R.attr.colorOnErrorContainer);
        }
        statusCard.setCardBackgroundColor(background);
        statusText.setTextColor(foreground);
        TextViewCompat.setCompoundDrawableTintList(statusText, ColorStateList.valueOf(foreground));
    }

    /** Turns the list of steps into "1. ...", "2. ..." separated by blank lines. */
    private String formatSteps(List<String> steps) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append(getString(R.string.step_format, i + 1, steps.get(i)));
        }
        return builder.toString();
    }
}
