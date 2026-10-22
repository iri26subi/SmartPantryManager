package com.smartpantry.manager.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User preferences, persisted with SharedPreferences.
 *
 * SharedPreferences is used instead of a SQLite table because these are a
 * handful of simple key/value settings, which is exactly what it is designed for.
 */
public class AppSettings {

    private static final String PREFS_NAME = "smart_pantry_settings";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_EXPIRY_WINDOW_DAYS = "expiry_window_days";
    private static final String KEY_EXCLUDE_EXPIRED = "exclude_expired_from_matching";

    public static final int DEFAULT_EXPIRY_WINDOW_DAYS = 3;
    public static final int[] EXPIRY_WINDOW_OPTIONS = {1, 2, 3, 5, 7};

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Highlight items that expire soon and show a warning banner on the pantry screen. */
    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** How many days ahead an item counts as "expiring soon". */
    public int getExpiryWindowDays() {
        return prefs.getInt(KEY_EXPIRY_WINDOW_DAYS, DEFAULT_EXPIRY_WINDOW_DAYS);
    }

    public void setExpiryWindowDays(int days) {
        prefs.edit().putInt(KEY_EXPIRY_WINDOW_DAYS, days).apply();
    }

    /** When true, expired pantry items are not counted by the recipe matcher. */
    public boolean isExcludeExpiredEnabled() {
        return prefs.getBoolean(KEY_EXCLUDE_EXPIRED, true);
    }

    public void setExcludeExpiredEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXCLUDE_EXPIRED, enabled).apply();
    }
}
