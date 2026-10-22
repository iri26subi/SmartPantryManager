package com.smartpantry.manager.matching;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns a free-text ingredient name into a canonical "matching key".
 *
 * Two names refer to the same ingredient only when their keys are exactly equal.
 * The normaliser deliberately avoids substring or fuzzy matching, so that
 * "butter" never matches "peanut butter" and "egg" never matches "egg white".
 *
 * Steps:
 *  1. lower-case, replace punctuation with spaces and collapse whitespace
 *  2. singularise the LAST word ("cherry tomatoes" -> "cherry tomato")
 *  3. map a small set of true synonyms to one name ("scallion" -> "spring onion")
 */
public final class IngredientNormalizer {

    /** Words that end in "s" but are already singular (or have no plural). */
    private static final Set<String> INVARIANT = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus",
            "octopus", "haggis", "series", "species", "grits", "brussels"));

    /** Plurals that the simple suffix rules below would get wrong. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Different names for exactly the same ingredient (applied after singularising). */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("potatoes", "potato");
        IRREGULAR.put("tomatoes", "tomato");
        IRREGULAR.put("mangoes", "mango");
        IRREGULAR.put("cookies", "cookie");
        IRREGULAR.put("brownies", "brownie");
        IRREGULAR.put("smoothies", "smoothie");

        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("garbanzo bean", "chickpea");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("plain yogurt", "plain yoghurt");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("sweet corn", "sweetcorn");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("castor sugar", "sugar");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("bicarbonate of soda", "baking soda");
        SYNONYMS.put("bicarb", "baking soda");
        SYNONYMS.put("mayo", "mayonnaise");
    }

    private IngredientNormalizer() {
        // utility class
    }

    /**
     * Returns the canonical matching key for an ingredient name, or an empty
     * string when the name is null or contains no letters/digits.
     */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.toLowerCase(Locale.ROOT)
                .replace("&", " and ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        int lastSpace = cleaned.lastIndexOf(' ');
        String prefix = lastSpace >= 0 ? cleaned.substring(0, lastSpace + 1) : "";
        String lastWord = lastSpace >= 0 ? cleaned.substring(lastSpace + 1) : cleaned;
        String key = prefix + singularize(lastWord);

        String synonym = SYNONYMS.get(key);
        return synonym != null ? synonym : key;
    }

    /** Returns true when both names normalise to the same non-empty key. */
    public static boolean sameIngredient(String a, String b) {
        String keyA = normalize(a);
        return !keyA.isEmpty() && keyA.equals(normalize(b));
    }

    /**
     * Converts a single English word to its singular form using a small set of
     * rules that cover the common cases found in ingredient names.
     */
    static String singularize(String word) {
        if (word.length() <= 3 || INVARIANT.contains(word)) {
            return word;
        }
        String irregular = IRREGULAR.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";      // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);            // avocadoes -> avocado
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("sses")
                || word.endsWith("xes") || word.endsWith("zzes")) {
            return word.substring(0, word.length() - 2);            // peaches -> peach
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);            // eggs -> egg
        }
        return word;
    }
}
