package com.smartpantry.manager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpantry.manager.R;

/**
 * Shared behaviour for every screen: edge-to-edge window insets, the toolbar
 * "up" arrow and the bottom navigation bar.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android 15+ draws apps edge-to-edge, so we pad the layout ourselves
        // to keep content clear of the status and navigation bars.
        EdgeToEdge.enable(this);
    }

    /**
     * Pads the root view (id "root") so it is not hidden behind system bars.
     *
     * @param padBottom true for screens without a bottom navigation bar; the
     *                  BottomNavigationView already pads itself for the system
     *                  navigation bar. Also keeps forms above the keyboard.
     */
    protected void applyWindowInsets(boolean padBottom) {
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = padBottom ? Math.max(bars.bottom, ime.bottom) : 0;
            view.setPadding(bars.left, bars.top, bars.right, bottom);
            return insets;
        });
    }

    /** Shows a back arrow in the toolbar that behaves like the system Back button. */
    protected MaterialToolbar setUpToolbarWithUp() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationContentDescription(R.string.nav_up);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        return toolbar;
    }

    /**
     * Wires up the bottom navigation bar shared by the three top-level screens
     * (Pantry, Recipes, Settings).
     *
     * The Pantry screen (MainActivity) is always at the bottom of the back
     * stack. Moving to another tab starts that screen with an explicit Intent
     * and closes the current one unless it is the Pantry screen, so pressing
     * Back from Recipes or Settings always returns to the Pantry.
     */
    protected void setUpBottomNavigation(int currentItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(currentItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId != currentItemId) {
                navigateToTab(itemId);
            }
            // Returning false keeps the current tab highlighted on this screen.
            return false;
        });
    }

    @SuppressWarnings("deprecation") // overridePendingTransition is the simplest API that works on API 26+
    protected void navigateToTab(int itemId) {
        Intent intent;
        if (itemId == R.id.nav_pantry) {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (itemId == R.id.nav_recipes) {
            intent = new Intent(this, SuggestedRecipesActivity.class);
        } else if (itemId == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        if (!(this instanceof MainActivity)) {
            finish();
            overridePendingTransition(0, 0);
        }
    }
}
