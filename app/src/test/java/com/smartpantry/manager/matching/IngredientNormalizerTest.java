package com.smartpantry.manager.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IngredientNormalizerTest {

    @Test
    public void lowerCasesTrimsAndCollapsesSpaces() {
        assertEquals("olive oil", IngredientNormalizer.normalize("  Olive   OIL "));
    }

    @Test
    public void removesPunctuation() {
        assertEquals("self raising flour", IngredientNormalizer.normalize("Self-raising flour"));
    }

    @Test
    public void singularisesCommonPlurals() {
        assertEquals("egg", IngredientNormalizer.normalize("Eggs"));
        assertEquals("tomato", IngredientNormalizer.normalize("tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("Potatoes"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals("peach", IngredientNormalizer.normalize("peaches"));
        assertEquals("radish", IngredientNormalizer.normalize("radishes"));
        assertEquals("bay leaf", IngredientNormalizer.normalize("bay leaves"));
        assertEquals("olive", IngredientNormalizer.normalize("olives"));
        assertEquals("rolled oat", IngredientNormalizer.normalize("Rolled oats"));
        assertEquals("garlic clove", IngredientNormalizer.normalize("garlic cloves"));
        assertEquals("cookie", IngredientNormalizer.normalize("cookies"));
    }

    @Test
    public void onlyTheLastWordIsSingularised() {
        assertEquals("chicken breast", IngredientNormalizer.normalize("Chicken breasts"));
        assertEquals("cherry tomato", IngredientNormalizer.normalize("cherry tomatoes"));
    }

    @Test
    public void wordsThatAreAlreadySingularAreUnchanged() {
        assertEquals("hummus", IngredientNormalizer.normalize("Hummus"));
        assertEquals("couscous", IngredientNormalizer.normalize("couscous"));
        assertEquals("asparagus", IngredientNormalizer.normalize("asparagus"));
        assertEquals("molasses", IngredientNormalizer.normalize("molasses"));
        assertEquals("watercress", IngredientNormalizer.normalize("watercress"));
        assertEquals("rice", IngredientNormalizer.normalize("rice"));
        assertEquals("cheese", IngredientNormalizer.normalize("cheese"));
    }

    @Test
    public void singularAndPluralFormsAreTheSameIngredient() {
        assertTrue(IngredientNormalizer.sameIngredient("tomato", "Tomatoes"));
        assertTrue(IngredientNormalizer.sameIngredient("chickpea", "chickpeas"));
        assertTrue(IngredientNormalizer.sameIngredient("kidney beans", "Kidney Bean"));
        assertTrue(IngredientNormalizer.sameIngredient("cheese", "cheeses"));
    }

    @Test
    public void synonymsMapToOneName() {
        assertTrue(IngredientNormalizer.sameIngredient("scallions", "spring onion"));
        assertTrue(IngredientNormalizer.sameIngredient("capsicum", "bell peppers"));
        assertTrue(IngredientNormalizer.sameIngredient("plain yogurt", "plain yoghurt"));
        assertTrue(IngredientNormalizer.sameIngredient("ground beef", "beef mince"));
    }

    @Test
    public void unrelatedIngredientsAreNotTheSame() {
        assertFalse(IngredientNormalizer.sameIngredient("butter", "peanut butter"));
        assertFalse(IngredientNormalizer.sameIngredient("egg", "egg white"));
        assertFalse(IngredientNormalizer.sameIngredient("onion", "red onion"));
        assertFalse(IngredientNormalizer.sameIngredient("lemon", "lime"));
        assertFalse(IngredientNormalizer.sameIngredient("garlic", "garlic clove"));
        assertFalse(IngredientNormalizer.sameIngredient("pea", "peanut"));
    }

    @Test
    public void blankNamesNeverMatch() {
        assertEquals("", IngredientNormalizer.normalize(null));
        assertEquals("", IngredientNormalizer.normalize("   "));
        assertFalse(IngredientNormalizer.sameIngredient("", ""));
        assertFalse(IngredientNormalizer.sameIngredient("!!", "??"));
    }
}
