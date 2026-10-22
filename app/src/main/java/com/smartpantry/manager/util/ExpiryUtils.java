package com.smartpantry.manager.util;

import com.smartpantry.manager.model.PantryItem;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Helpers for the optional expiry date of a pantry item.
 * Dates are stored as ISO strings (yyyy-MM-dd).
 */
public final class ExpiryUtils {

    private ExpiryUtils() {
        // utility class
    }

    /** Parses an ISO date, returning null for blank or invalid input. */
    public static LocalDate parse(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Days from today until the item expires (negative once expired),
     * or null when the item has no valid expiry date.
     */
    public static Long daysUntilExpiry(PantryItem item, LocalDate today) {
        LocalDate expiry = parse(item.getExpiryDate());
        if (expiry == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, expiry);
    }

    /** An item is expired from the day after its expiry date. */
    public static boolean isExpired(PantryItem item, LocalDate today) {
        Long days = daysUntilExpiry(item, today);
        return days != null && days < 0;
    }

    /** True when the item expires today or within the next {@code windowDays} days. */
    public static boolean isExpiringSoon(PantryItem item, LocalDate today, int windowDays) {
        Long days = daysUntilExpiry(item, today);
        return days != null && days >= 0 && days <= windowDays;
    }
}
