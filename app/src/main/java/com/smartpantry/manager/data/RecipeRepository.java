package com.smartpantry.manager.data;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read operations for the recipes and recipe_ingredients tables.
 * (Recipes are written once by the seeding code in DatabaseHelper.)
 */
public class RecipeRepository {

    private static final String TAG = "RecipeRepository";

    private final DatabaseHelper dbHelper;

    public RecipeRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Loads every recipe together with its ingredients. Uses two queries in
     * total (one per table) instead of one ingredient query per recipe.
     */
    public List<Recipe> getAllWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        Map<Long, Recipe> byId = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPES, null, null, null, null, null,
                DatabaseHelper.COL_RECIPE_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                Recipe recipe = recipeFromCursor(cursor);
                recipes.add(recipe);
                byId.put(recipe.getId(), recipe);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read recipes", e);
            return recipes;
        }

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null, null, null,
                null, null, DatabaseHelper.COL_RI_ID + " ASC")) {
            while (cursor.moveToNext()) {
                RecipeIngredient ingredient = ingredientFromCursor(cursor);
                Recipe recipe = byId.get(ingredient.getRecipeId());
                if (recipe != null) {
                    recipe.addIngredient(ingredient);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read recipe ingredients", e);
        }
        return recipes;
    }

    /** Loads one recipe with its ingredients, or null if the id does not exist. */
    public Recipe getById(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Recipe recipe = null;
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPES, null,
                DatabaseHelper.COL_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                recipe = recipeFromCursor(cursor);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read recipe " + recipeId, e);
        }
        if (recipe != null) {
            recipe.setIngredients(getIngredients(recipeId));
        }
        return recipe;
    }

    /** All ingredient lines of one recipe, in the order they were seeded. */
    public List<RecipeIngredient> getIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null,
                DatabaseHelper.COL_RI_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, DatabaseHelper.COL_RI_ID + " ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(ingredientFromCursor(cursor));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to read ingredients for recipe " + recipeId, e);
        }
        return ingredients;
    }

    public long count() {
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), DatabaseHelper.TABLE_RECIPES);
    }

    private static Recipe recipeFromCursor(Cursor cursor) {
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_INSTRUCTIONS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_PREP_MINUTES)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_SERVINGS)));
    }

    private static RecipeIngredient ingredientFromCursor(Cursor cursor) {
        return new RecipeIngredient(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_RECIPE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_UNIT)));
    }
}
