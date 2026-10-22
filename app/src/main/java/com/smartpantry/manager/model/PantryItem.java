package com.smartpantry.manager.model;

/**
 * One ingredient the user has at home (one row of the pantry_items table).
 *
 * The expiry date is optional and is stored as an ISO-8601 string (yyyy-MM-dd)
 * so that it sorts correctly in SQLite and parses easily with java.time.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // null when the user did not enter one

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    /** Convenience constructor for an item that has not been saved yet. */
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.trim().isEmpty();
    }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }
}
