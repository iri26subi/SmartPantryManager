package com.smartpantry.manager.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A recipe from the recipes table together with its ingredient lines.
 *
 * Instructions are stored as one string with one preparation step per line,
 * which keeps the schema simple while still allowing numbered steps in the UI.
 */
public class Recipe {

    private long id;
    private String name;
    private String description;
    private String instructions;
    private int prepMinutes;
    private int servings;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(long id, String name, String description, String instructions,
                  int prepMinutes, int servings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.prepMinutes = prepMinutes;
        this.servings = servings;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public void setPrepMinutes(int prepMinutes) {
        this.prepMinutes = prepMinutes;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /** Splits the stored instructions into individual, non-blank steps. */
    public List<String> getSteps() {
        List<String> steps = new ArrayList<>();
        if (instructions == null) {
            return steps;
        }
        for (String line : Arrays.asList(instructions.split("\n"))) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                steps.add(trimmed);
            }
        }
        return steps;
    }

    @Override
    public String toString() {
        return name;
    }
}
