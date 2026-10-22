package com.smartpantry.manager.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.manager.R;
import com.smartpantry.manager.data.PantryRepository;
import com.smartpantry.manager.matching.IngredientNormalizer;
import com.smartpantry.manager.matching.UnitConverter;
import com.smartpantry.manager.model.PantryItem;
import com.smartpantry.manager.util.ExpiryUtils;
import com.smartpantry.manager.util.IngredientValidator;
import com.smartpantry.manager.util.QuantityFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Form used both to ADD a new pantry item and to EDIT an existing one.
 *
 * The mode is decided by the Intent: if it carries EXTRA_ITEM_ID the item is
 * loaded from SQLite and updated on save, otherwise a new row is inserted.
 * Every field is validated before anything is written to the database.
 */
public class AddEditIngredientActivity extends BaseActivity {

    public static final String EXTRA_ITEM_ID = "com.smartpantry.manager.EXTRA_ITEM_ID";
    public static final String EXTRA_RESULT_MESSAGE = "com.smartpantry.manager.EXTRA_RESULT_MESSAGE";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final long NO_ID = -1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

    private PantryRepository pantryRepository;
    private PantryItem editingItem; // null when adding a new item

    /** The chosen expiry date as an ISO string (yyyy-MM-dd), or null. */
    private String selectedExpiry;

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;
    private TextInputLayout expiryLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private MaterialAutoCompleteTextView unitInput;
    private TextInputEditText expiryInput;
    private MaterialButton clearExpiryButton;

    /** Intent for adding a new ingredient. */
    public static Intent newAddIntent(Context context) {
        return new Intent(context, AddEditIngredientActivity.class);
    }

    /** Intent for editing an existing ingredient; the id is passed as an Intent extra. */
    public static Intent newEditIntent(Context context, long itemId) {
        return new Intent(context, AddEditIngredientActivity.class).putExtra(EXTRA_ITEM_ID, itemId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        applyWindowInsets(true);
        MaterialToolbar toolbar = setUpToolbarWithUp();

        pantryRepository = new PantryRepository(this);
        bindViews();

        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        if (itemId != NO_ID) {
            editingItem = pantryRepository.getById(itemId);
            if (editingItem == null) {
                Toast.makeText(this, R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(R.string.title_edit_ingredient);
            MaterialButton deleteButton = findViewById(R.id.deleteButton);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> confirmDelete());
        }

        if (savedInstanceState != null) {
            // Text fields restore themselves; the expiry date is kept separately.
            setExpiry(savedInstanceState.getString(STATE_EXPIRY));
        } else if (editingItem != null) {
            populateFields(editingItem);
        } else {
            unitInput.setText(UnitConverter.PIECES, false);
            setExpiry(null);
        }
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        unitLayout = findViewById(R.id.unitLayout);
        expiryLayout = findViewById(R.id.expiryLayout);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        expiryInput = findViewById(R.id.expiryInput);
        clearExpiryButton = findViewById(R.id.clearExpiryButton);

        // Clear an error message as soon as the user starts correcting the field.
        nameInput.addTextChangedListener(new ErrorClearingWatcher(nameLayout));
        quantityInput.addTextChangedListener(new ErrorClearingWatcher(quantityLayout));
        unitInput.setOnItemClickListener((parent, view, position, id) -> unitLayout.setError(null));

        expiryInput.setOnClickListener(v -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(v -> showDatePicker());
        clearExpiryButton.setOnClickListener(v -> setExpiry(null));

        MaterialButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveIngredient());
    }

    private void populateFields(PantryItem item) {
        nameInput.setText(item.getName());
        quantityInput.setText(QuantityFormatter.format(item.getQuantity()));
        unitInput.setText(item.getUnit(), false);
        setExpiry(item.getExpiryDate());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, selectedExpiry);
    }

    private void showDatePicker() {
        LocalDate initial = ExpiryUtils.parse(selectedExpiry);
        if (initial == null) {
            initial = LocalDate.now();
        }
        // DatePickerDialog months are 0-based; LocalDate months are 1-based.
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> setExpiry(LocalDate.of(year, month + 1, dayOfMonth).toString()),
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth())
                .show();
    }

    private void setExpiry(String isoDate) {
        LocalDate date = ExpiryUtils.parse(isoDate);
        selectedExpiry = date == null ? null : date.toString();
        expiryInput.setText(date == null ? "" : date.format(DATE_FORMAT));
        clearExpiryButton.setVisibility(date == null ? View.GONE : View.VISIBLE);
    }

    /** Validates every field, then INSERTs or UPDATEs the item in SQLite. */
    private void saveIngredient() {
        String rawName = textOf(nameInput);
        String rawQuantity = textOf(quantityInput);
        String unit = UnitConverter.normalizeUnit(unitInput.getText().toString());

        boolean valid = true;
        View firstInvalid = null;

        IngredientValidator.NameError nameError = IngredientValidator.validateName(rawName);
        if (nameError != IngredientValidator.NameError.NONE) {
            nameLayout.setError(getString(nameErrorMessage(nameError)));
            firstInvalid = nameInput;
            valid = false;
        }

        IngredientValidator.QuantityError quantityError = IngredientValidator.validateQuantity(rawQuantity);
        if (quantityError != IngredientValidator.QuantityError.NONE) {
            quantityLayout.setError(getString(quantityErrorMessage(quantityError)));
            if (firstInvalid == null) {
                firstInvalid = quantityInput;
            }
            valid = false;
        }

        if (unit == null) {
            unitLayout.setError(getString(R.string.error_unit_required));
            valid = false;
        }

        if (!valid) {
            if (firstInvalid != null) {
                firstInvalid.requestFocus();
            }
            return;
        }

        String name = IngredientValidator.cleanName(rawName);
        double quantity = IngredientValidator.parseQuantity(rawQuantity);

        boolean saved;
        String duplicateName = null;
        if (editingItem == null) {
            duplicateName = findCompatibleDuplicate(name, unit);
            PantryItem newItem = new PantryItem(name, quantity, unit, selectedExpiry);
            saved = pantryRepository.insert(newItem) != -1;
        } else {
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(selectedExpiry);
            saved = pantryRepository.update(editingItem);
        }

        if (!saved) {
            Snackbar.make(nameInput, R.string.error_save_failed, Snackbar.LENGTH_LONG).show();
            return;
        }

        String message = duplicateName != null
                ? getString(R.string.item_saved_combined, name, duplicateName)
                : getString(R.string.item_saved, name);
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT_MESSAGE, message));
        finish();
    }

    /**
     * Duplicate handling: separate rows for the same ingredient are allowed
     * (e.g. two packs with different expiry dates). The matcher adds their
     * quantities together, so we just tell the user that this will happen.
     * Returns the existing item's name, or null if there is no compatible duplicate.
     */
    private String findCompatibleDuplicate(String name, String unit) {
        String key = IngredientNormalizer.normalize(name);
        for (PantryItem existing : pantryRepository.getAll()) {
            if (IngredientNormalizer.normalize(existing.getName()).equals(key)
                    && UnitConverter.areCompatible(existing.getUnit(), unit)) {
                return existing.getName();
            }
        }
        return null;
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, editingItem.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (pantryRepository.delete(editingItem.getId())) {
                        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT_MESSAGE,
                                getString(R.string.item_deleted, editingItem.getName())));
                        finish();
                    } else {
                        Snackbar.make(nameInput, R.string.error_delete_failed, Snackbar.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private static int nameErrorMessage(IngredientValidator.NameError error) {
        switch (error) {
            case REQUIRED:
                return R.string.error_name_required;
            case TOO_LONG:
                return R.string.error_name_too_long;
            default:
                return R.string.error_name_invalid;
        }
    }

    private static int quantityErrorMessage(IngredientValidator.QuantityError error) {
        switch (error) {
            case REQUIRED:
                return R.string.error_quantity_required;
            case NOT_POSITIVE:
                return R.string.error_quantity_positive;
            case TOO_LARGE:
                return R.string.error_quantity_too_large;
            default:
                return R.string.error_quantity_invalid;
        }
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    /** Removes a field's error message once the user edits it. */
    private static class ErrorClearingWatcher implements TextWatcher {
        private final TextInputLayout layout;

        ErrorClearingWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            layout.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
