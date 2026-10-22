package com.smartpantry.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartpantry.manager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Create, Read, Update and Delete operations for the pantry_items table.
 *
 * All queries use parameter placeholders (?) rather than string concatenation
 * of user input, which protects against SQL injection. Write methods catch
 * SQLException and report failure through their return value so the UI can
 * show a message instead of crashing.
 */
public class PantryRepository {

    private static final String TAG = "PantryRepository";

    private final DatabaseHelper dbHelper;

    public PantryRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /** CREATE: returns the new row id, or -1 if the insert failed. */
    public long insert(PantryItem item) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long id = db.insertOrThrow(DatabaseHelper.TABLE_PANTRY, null, toValues(item));
            item.setId(id);
            return id;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert pantry item", e);
            return -1;
        }
    }

    /** CREATE (bulk): inserts several items in one transaction. */
    public boolean insertAll(List<PantryItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (PantryItem item : items) {
                item.setId(db.insertOrThrow(DatabaseHelper.TABLE_PANTRY, null, toValues(item)));
            }
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert pantry items", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /** READ: every pantry item, sorted alphabetically by name. */
    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PANTRY, null, null, null, null, null,
                DatabaseHelper.COL_PANTRY_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read pantry items", e);
        }
        return items;
    }

    /** READ: a single item, or null if no item has this id. */
    public PantryItem getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PANTRY, null,
                DatabaseHelper.COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read pantry item " + id, e);
        }
        return null;
    }

    /** UPDATE: returns true if exactly one row was changed. */
    public boolean update(PantryItem item) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.update(DatabaseHelper.TABLE_PANTRY, toValues(item),
                    DatabaseHelper.COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
            return rows == 1;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update pantry item " + item.getId(), e);
            return false;
        }
    }

    /** DELETE: returns true if exactly one row was removed. */
    public boolean delete(long id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(DatabaseHelper.TABLE_PANTRY,
                    DatabaseHelper.COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
            return rows == 1;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete pantry item " + id, e);
            return false;
        }
    }

    /** DELETE (all): empties the pantry and returns the number of rows removed. */
    public int deleteAll() {
        try {
            return dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_PANTRY, null, null);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to clear pantry", e);
            return 0;
        }
    }

    public long count() {
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), DatabaseHelper.TABLE_PANTRY);
    }

    private static ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PANTRY_NAME, item.getName().trim());
        values.put(DatabaseHelper.COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(DatabaseHelper.COL_PANTRY_UNIT, item.getUnit());
        if (item.hasExpiryDate()) {
            values.put(DatabaseHelper.COL_PANTRY_EXPIRY, item.getExpiryDate());
        } else {
            values.putNull(DatabaseHelper.COL_PANTRY_EXPIRY);
        }
        return values;
    }

    private static PantryItem fromCursor(Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_EXPIRY);
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_UNIT)),
                cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
    }
}
