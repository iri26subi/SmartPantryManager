package com.smartpantry.manager.data;

import com.smartpantry.manager.model.PantryItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A small sample pantry that the user can load from the Settings screen to
 * try the app quickly. It satisfies a few recipes completely (e.g. Scrambled
 * Eggs, Tomato Pasta) and leaves others one ingredient short (e.g. Garlic
 * Butter Pasta needs parmesan), which makes the strict rule easy to see.
 */
public final class DemoPantryData {

    private DemoPantryData() {
        // utility class
    }

    public static List<PantryItem> getItems(LocalDate today) {
        List<PantryItem> items = new ArrayList<>();
        items.add(new PantryItem("Eggs", 6, "pcs", today.plusDays(10).toString()));
        items.add(new PantryItem("Milk", 1, "L", today.plusDays(2).toString()));
        items.add(new PantryItem("Butter", 250, "g", today.plusDays(30).toString()));
        items.add(new PantryItem("Salt", 500, "g", null));
        items.add(new PantryItem("Cheddar cheese", 200, "g", today.plusDays(14).toString()));
        items.add(new PantryItem("Bread slices", 10, "pcs", today.plusDays(4).toString()));
        items.add(new PantryItem("Tomatoes", 5, "pcs", today.plusDays(5).toString()));
        items.add(new PantryItem("Onions", 2, "pcs", null));
        items.add(new PantryItem("Garlic cloves", 8, "pcs", null));
        items.add(new PantryItem("Olive oil", 500, "ml", null));
        items.add(new PantryItem("Pasta", 500, "g", null));
        items.add(new PantryItem("Sugar", 1, "kg", null));
        return items;
    }
}
