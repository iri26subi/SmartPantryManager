package com.smartpantry.manager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.smartpantry.manager.model.PantryItem;

import org.junit.Test;

import java.time.LocalDate;

public class ExpiryUtilsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private static PantryItem expiring(String isoDate) {
        return new PantryItem("Milk", 1, "L", isoDate);
    }

    @Test
    public void parsesIsoDatesAndRejectsInvalidInput() {
        assertEquals(LocalDate.of(2026, 10, 25), ExpiryUtils.parse("2026-10-25"));
        assertEquals(LocalDate.of(2026, 10, 25), ExpiryUtils.parse(" 2026-10-25 "));
        assertNull(ExpiryUtils.parse(null));
        assertNull(ExpiryUtils.parse(""));
        assertNull(ExpiryUtils.parse("25/10/2026"));
        assertNull(ExpiryUtils.parse("2026-02-30"));
    }

    @Test
    public void countsDaysUntilExpiry() {
        assertEquals(Long.valueOf(5), ExpiryUtils.daysUntilExpiry(expiring("2026-10-25"), TODAY));
        assertEquals(Long.valueOf(0), ExpiryUtils.daysUntilExpiry(expiring("2026-10-20"), TODAY));
        assertEquals(Long.valueOf(-1), ExpiryUtils.daysUntilExpiry(expiring("2026-10-19"), TODAY));
        assertNull(ExpiryUtils.daysUntilExpiry(expiring(null), TODAY));
    }

    @Test
    public void itemIsExpiredOnlyAfterItsExpiryDay() {
        assertFalse(ExpiryUtils.isExpired(expiring("2026-10-20"), TODAY));
        assertTrue(ExpiryUtils.isExpired(expiring("2026-10-19"), TODAY));
        assertFalse(ExpiryUtils.isExpired(expiring(null), TODAY));
    }

    @Test
    public void expiringSoonCoversTodayUpToTheWindow() {
        assertTrue(ExpiryUtils.isExpiringSoon(expiring("2026-10-20"), TODAY, 3));
        assertTrue(ExpiryUtils.isExpiringSoon(expiring("2026-10-23"), TODAY, 3));
        assertFalse(ExpiryUtils.isExpiringSoon(expiring("2026-10-24"), TODAY, 3));
        assertFalse(ExpiryUtils.isExpiringSoon(expiring("2026-10-19"), TODAY, 3)); // already expired
        assertFalse(ExpiryUtils.isExpiringSoon(expiring(null), TODAY, 3));
    }
}
