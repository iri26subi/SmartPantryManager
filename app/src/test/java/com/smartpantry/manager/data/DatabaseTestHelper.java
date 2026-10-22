package com.smartpantry.manager.data;

/** Gives tests in other packages access to the package-private database reset hook. */
public final class DatabaseTestHelper {

    private DatabaseTestHelper() {
    }

    public static void reset() {
        DatabaseHelper.resetInstanceForTesting();
    }
}
