package com.smartpantry.manager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.AppSettings;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.ui.adapter.PantryAdapter;
import com.smartpantry.manager.util.ExpiryUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Pantry list screen (the launcher screen).
 *
 * Shows every pantry item from SQLite in a RecyclerView and lets the user add
 * (FAB), edit (tap a card) and delete (bin icon) ingredients. The list is
 * reloaded in onResume(), so it is always current when the user returns from
 * the Add/Edit screen or from another tab.
 */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private PantryRepository pantryRepository;
    private AppSettings settings;
    private PantryAdapter adapter;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private View emptyState;
    private View expiryBanner;
    private TextView expiryBannerText;
    private ExtendedFloatingActionButton fabAdd;

    /** Starts Add/Edit and shows its confirmation message when it returns RESULT_OK. */
    private final ActivityResultLauncher<Intent> addEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String message = result.getData().getStringExtra(AddEditIngredientActivity.EXTRA_RESULT_MESSAGE);
                    if (message != null) {
                        showMessage(message);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyWindowInsets(false);
        setUpBottomNavigation(R.id.nav_pantry);

        pantryRepository = new PantryRepository(this);
        settings = new AppSettings(this);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.pantryRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        expiryBanner = findViewById(R.id.expiryBanner);
        expiryBannerText = findViewById(R.id.expiryBannerText);
        fabAdd = findViewById(R.id.fabAdd);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Shrink the FAB to an icon while scrolling down so it does not hide the list.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy > 0) {
                    fabAdd.shrink();
                } else if (dy < 0) {
                    fabAdd.extend();
                }
            }
        });

        fabAdd.setOnClickListener(v -> addEditLauncher.launch(AddEditIngredientActivity.newAddIntent(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** READ: loads the pantry from SQLite and updates the list, empty state and banner. */
    private void loadPantry() {
        List<PantryItem> items = pantryRepository.getAll();
        LocalDate today = LocalDate.now();
        boolean alertsEnabled = settings.isExpiryAlertsEnabled();
        int windowDays = settings.getExpiryWindowDays();

        adapter.setItems(items, alertsEnabled, windowDays, today);

        boolean isEmpty = items.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (isEmpty) {
            // Nothing to scroll, so show the full "Add ingredient" label again.
            fabAdd.extend();
        }
        toolbar.setSubtitle(isEmpty ? null
                : getResources().getQuantityString(R.plurals.pantry_summary, items.size(), items.size()));

        updateExpiryBanner(items, today, alertsEnabled, windowDays);
    }

    private void updateExpiryBanner(List<PantryItem> items, LocalDate today, boolean alertsEnabled, int windowDays) {
        int expiringSoon = 0;
        int expired = 0;
        for (PantryItem item : items) {
            if (ExpiryUtils.isExpired(item, today)) {
                expired++;
            } else if (ExpiryUtils.isExpiringSoon(item, today, windowDays)) {
                expiringSoon++;
            }
        }
        if (alertsEnabled && (expiringSoon > 0 || expired > 0)) {
            expiryBannerText.setText(getString(R.string.expiry_banner, expiringSoon, windowDays, expired));
            expiryBanner.setVisibility(View.VISIBLE);
        } else {
            expiryBanner.setVisibility(View.GONE);
        }
    }

    /** UPDATE: opens the Add/Edit screen for the tapped item, passing its id in the Intent. */
    @Override
    public void onEditItem(PantryItem item) {
        addEditLauncher.launch(AddEditIngredientActivity.newEditIntent(this, item.getId()));
    }

    /** DELETE: asks for confirmation, then removes the item from SQLite. */
    @Override
    public void onDeleteItem(PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (pantryRepository.delete(item.getId())) {
                        showMessage(getString(R.string.item_deleted, item.getName()));
                    } else {
                        showMessage(getString(R.string.error_delete_failed));
                    }
                    loadPantry();
                })
                .show();
    }

    private void showMessage(String message) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).setAnchorView(fabAdd).show();
    }
}
