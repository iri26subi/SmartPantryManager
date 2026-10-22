package com.smartpantry.manager.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.BuildConfig;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.AppSettings;
import com.smartpantry.manager.data.DemoPantryData;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.data.RecipeRepository;

import java.time.LocalDate;

/**
 * Settings screen. Preferences are saved immediately to SharedPreferences
 * (via AppSettings), so they survive the app being closed and reopened.
 */
public class SettingsActivity extends BaseActivity {

    private AppSettings settings;
    private PantryRepository pantryRepository;
    private RecipeRepository recipeRepository;
    private TextView aboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        applyWindowInsets(false);
        setUpBottomNavigation(R.id.nav_settings);

        settings = new AppSettings(this);
        pantryRepository = new PantryRepository(this);
        recipeRepository = new RecipeRepository(this);
        aboutText = findViewById(R.id.aboutText);

        setUpExpirySettings();
        setUpMatchingSettings();
        setUpDataButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAboutText();
    }

    private void setUpExpirySettings() {
        MaterialSwitch alertsSwitch = findViewById(R.id.expiryAlertsSwitch);
        TextInputLayout windowLayout = findViewById(R.id.expiryWindowLayout);
        MaterialAutoCompleteTextView windowInput = findViewById(R.id.expiryWindowInput);

        // Build the dropdown labels ("1 day", "3 days", ...) from the allowed options.
        int[] options = AppSettings.EXPIRY_WINDOW_OPTIONS;
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labels[i] = getResources().getQuantityString(R.plurals.days_count, options[i], options[i]);
        }
        windowInput.setSimpleItems(labels);
        int currentDays = settings.getExpiryWindowDays();
        windowInput.setText(getResources().getQuantityString(R.plurals.days_count, currentDays, currentDays), false);
        windowInput.setOnItemClickListener((parent, view, position, id) ->
                settings.setExpiryWindowDays(options[position]));

        alertsSwitch.setChecked(settings.isExpiryAlertsEnabled());
        windowLayout.setEnabled(alertsSwitch.isChecked());
        alertsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            settings.setExpiryAlertsEnabled(isChecked);
            windowLayout.setEnabled(isChecked);
        });
    }

    private void setUpMatchingSettings() {
        MaterialSwitch excludeExpiredSwitch = findViewById(R.id.excludeExpiredSwitch);
        excludeExpiredSwitch.setChecked(settings.isExcludeExpiredEnabled());
        excludeExpiredSwitch.setOnCheckedChangeListener((button, isChecked) ->
                settings.setExcludeExpiredEnabled(isChecked));
    }

    private void setUpDataButtons() {
        MaterialButton loadDemoButton = findViewById(R.id.loadDemoButton);
        loadDemoButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.demo_confirm_title)
                .setMessage(R.string.demo_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_load, (dialog, which) -> {
                    boolean ok = pantryRepository.insertAll(DemoPantryData.getItems(LocalDate.now()));
                    showMessage(getString(ok ? R.string.demo_loaded : R.string.error_generic));
                    updateAboutText();
                })
                .show());

        MaterialButton clearButton = findViewById(R.id.clearPantryButton);
        clearButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_confirm_title)
                .setMessage(R.string.clear_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    int deleted = pantryRepository.deleteAll();
                    showMessage(getString(R.string.pantry_cleared, deleted));
                    updateAboutText();
                })
                .show());
    }

    private void updateAboutText() {
        aboutText.setText(getString(R.string.about_text, BuildConfig.VERSION_NAME,
                (int) recipeRepository.count(), (int) pantryRepository.count()));
    }

    private void showMessage(String message) {
        Snackbar.make(aboutText, message, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottomNav)
                .show();
    }
}
