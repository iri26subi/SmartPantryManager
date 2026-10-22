package com.smartpantry.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

/**
 * Creates and upgrades the on-device SQLite database.
 *
 * Schema:
 *  pantry_items       (id, name, quantity, unit, expiry_date)
 *  recipes            (id, name UNIQUE, description, instructions, prep_minutes, servings)
 *  recipe_ingredients (id, recipe_id -> recipes.id ON DELETE CASCADE, name, quantity, unit)
 *
 * The starter recipes are seeded inside onCreate(), which Android calls only
 * once, when the database file does not exist yet. The seeding method also
 * checks the row count and the recipe name is UNIQUE, so recipes can never
 * be duplicated on later launches.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;

    // pantry_items table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_DESCRIPTION = "description";
    public static final String COL_RECIPE_INSTRUCTIONS = "instructions";
    public static final String COL_RECIPE_PREP_MINUTES = "prep_minutes";
    public static final String COL_RECIPE_SERVINGS = "servings";

    // recipe_ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static final String CREATE_PANTRY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PANTRY + " ("
                    + COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_PANTRY_NAME + " TEXT NOT NULL, "
                    + COL_PANTRY_QUANTITY + " REAL NOT NULL CHECK (" + COL_PANTRY_QUANTITY + " > 0), "
                    + COL_PANTRY_UNIT + " TEXT NOT NULL, "
                    + COL_PANTRY_EXPIRY + " TEXT)";

    private static final String CREATE_RECIPES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " ("
                    + COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_NAME + " TEXT NOT NULL UNIQUE, "
                    + COL_RECIPE_DESCRIPTION + " TEXT, "
                    + COL_RECIPE_INSTRUCTIONS + " TEXT NOT NULL, "
                    + COL_RECIPE_PREP_MINUTES + " INTEGER NOT NULL DEFAULT 0, "
                    + COL_RECIPE_SERVINGS + " INTEGER NOT NULL DEFAULT 1)";

    private static final String CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " ("
                    + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RI_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_RI_NAME + " TEXT NOT NULL, "
                    + COL_RI_QUANTITY + " REAL NOT NULL CHECK (" + COL_RI_QUANTITY + " > 0), "
                    + COL_RI_UNIT + " TEXT NOT NULL, "
                    + "FOREIGN KEY (" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES
                    + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_RI_INDEX =
            "CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_recipe ON "
                    + TABLE_RECIPE_INGREDIENTS + "(" + COL_RI_RECIPE_ID + ")";

    private static DatabaseHelper instance;

    /**
     * One shared helper for the whole app, so every screen uses the same
     * database connection. The application context avoids leaking an Activity.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Closes and forgets the shared instance. Only used by the database unit
     * tests, which need a fresh helper for every test application.
     */
    static synchronized void resetInstanceForTesting() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite does not enforce foreign keys unless asked to.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PANTRY);
        db.execSQL(CREATE_RECIPES);
        db.execSQL(CREATE_RECIPE_INGREDIENTS);
        db.execSQL(CREATE_RI_INDEX);
        seedRecipes(db);
    }

    /**
     * Upgrades rebuild the recipe tables (which only contain seed data) and
     * re-seed them, but keep the user's pantry items intact.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db); // pantry table uses CREATE TABLE IF NOT EXISTS, so it is preserved
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Inserts the starter recipes in a single transaction. Does nothing if the
     * recipes table already has rows, so it is safe to call more than once.
     */
    void seedRecipes(SQLiteDatabase db) {
        if (DatabaseUtils.queryNumEntries(db, TABLE_RECIPES) > 0) {
            return;
        }
        db.beginTransaction();
        try {
            for (Recipe recipe : RecipeSeedData.getRecipes()) {
                ContentValues recipeValues = new ContentValues();
                recipeValues.put(COL_RECIPE_NAME, recipe.getName());
                recipeValues.put(COL_RECIPE_DESCRIPTION, recipe.getDescription());
                recipeValues.put(COL_RECIPE_INSTRUCTIONS, recipe.getInstructions());
                recipeValues.put(COL_RECIPE_PREP_MINUTES, recipe.getPrepMinutes());
                recipeValues.put(COL_RECIPE_SERVINGS, recipe.getServings());
                long recipeId = db.insertWithOnConflict(TABLE_RECIPES, null, recipeValues,
                        SQLiteDatabase.CONFLICT_IGNORE);
                if (recipeId == -1) {
                    continue; // a recipe with this name already exists
                }
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(COL_RI_RECIPE_ID, recipeId);
                    ingredientValues.put(COL_RI_NAME, ingredient.getName());
                    ingredientValues.put(COL_RI_QUANTITY, ingredient.getQuantity());
                    ingredientValues.put(COL_RI_UNIT, ingredient.getUnit());
                    db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
