package com.smartpantry.manager.data;

import com.smartpantry.manager.model.Recipe;
import com.smartpantry.manager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * The starter recipe collection that is written to SQLite the first time the
 * database is created.
 *
 * Quantities use the units supported by UnitConverter: countable items in
 * "pcs", dry goods in grams and liquids in ml/tbsp/cup (which convert to ml),
 * so they can be compared reliably with the user's pantry.
 * Water is assumed to be available and is not listed as an ingredient.
 */
public final class RecipeSeedData {

    private RecipeSeedData() {
        // utility class
    }

    public static List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("Scrambled Eggs",
                "Soft, creamy eggs ready in five minutes.", 5, 1,
                "Crack the eggs into a bowl, add the milk and salt and whisk well.\n"
                        + "Melt the butter in a non-stick pan over a low heat.\n"
                        + "Pour in the eggs and stir slowly with a spatula.\n"
                        + "Remove from the heat while the eggs are still slightly soft and serve.",
                ing("egg", 3, "pcs"), ing("milk", 50, "ml"), ing("butter", 10, "g"), ing("salt", 1, "g")));

        recipes.add(recipe("Cheese Omelette",
                "A fluffy omelette filled with melted cheddar.", 10, 1,
                "Whisk the eggs with the salt.\n"
                        + "Melt the butter in a frying pan over a medium heat.\n"
                        + "Pour in the eggs and tilt the pan so they cover the base.\n"
                        + "When the top is almost set, sprinkle the grated cheese over one half.\n"
                        + "Fold the omelette in half, cook for one more minute and serve.",
                ing("egg", 3, "pcs"), ing("cheddar cheese", 40, "g"), ing("butter", 10, "g"),
                ing("salt", 1, "g")));

        recipes.add(recipe("French Toast",
                "Golden, sweet fried bread for breakfast.", 15, 2,
                "Whisk the eggs, milk and sugar together in a shallow dish.\n"
                        + "Dip each bread slice into the mixture so both sides are coated.\n"
                        + "Melt the butter in a frying pan over a medium heat.\n"
                        + "Fry the slices for 2-3 minutes per side until golden brown.\n"
                        + "Serve warm.",
                ing("bread slice", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"),
                ing("butter", 15, "g"), ing("sugar", 10, "g")));

        recipes.add(recipe("Pancakes",
                "Thick, fluffy pancakes made from pantry basics.", 25, 4,
                "Mix the flour, sugar and baking powder in a large bowl.\n"
                        + "Melt the butter. Whisk the eggs, milk and melted butter together.\n"
                        + "Pour the wet mixture into the dry ingredients and stir until just combined.\n"
                        + "Heat a lightly greased pan and pour in about 3 tablespoons of batter per pancake.\n"
                        + "Flip when bubbles appear on the surface and cook until golden on both sides.",
                ing("flour", 150, "g"), ing("egg", 2, "pcs"), ing("milk", 1, "cup"),
                ing("sugar", 25, "g"), ing("butter", 30, "g"), ing("baking powder", 10, "g")));

        recipes.add(recipe("Tomato Pasta",
                "A quick fresh tomato and garlic pasta.", 20, 2,
                "Cook the pasta in salted boiling water until al dente, then drain.\n"
                        + "Chop the tomatoes and finely slice the garlic.\n"
                        + "Heat the olive oil in a pan and fry the garlic for 1 minute.\n"
                        + "Add the tomatoes and salt and simmer for 8 minutes until saucy.\n"
                        + "Toss the pasta through the sauce and serve.",
                ing("pasta", 200, "g"), ing("tomato", 4, "pcs"), ing("garlic clove", 2, "pcs"),
                ing("olive oil", 2, "tbsp"), ing("salt", 2, "g")));

        recipes.add(recipe("Garlic Butter Pasta",
                "Simple pasta tossed in garlic butter and parmesan.", 15, 2,
                "Cook the pasta in salted boiling water until al dente.\n"
                        + "Meanwhile, finely chop the garlic.\n"
                        + "Melt the butter in a pan over a low heat and gently cook the garlic for 2 minutes.\n"
                        + "Drain the pasta, add it to the pan and toss with the grated parmesan.",
                ing("pasta", 200, "g"), ing("garlic clove", 4, "pcs"), ing("butter", 40, "g"),
                ing("parmesan cheese", 30, "g")));

        recipes.add(recipe("Vegetable Stir-Fry",
                "Crunchy vegetables in a savoury soy and garlic sauce.", 20, 2,
                "Slice the carrots, bell pepper and onion into thin strips.\n"
                        + "Crush the garlic.\n"
                        + "Heat the vegetable oil in a wok or large pan over a high heat.\n"
                        + "Stir-fry the onion and carrots for 3 minutes, then add the pepper and garlic.\n"
                        + "Add the soy sauce, stir-fry for 2 more minutes and serve.",
                ing("carrot", 2, "pcs"), ing("bell pepper", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic clove", 2, "pcs"), ing("soy sauce", 3, "tbsp"), ing("vegetable oil", 2, "tbsp")));

        recipes.add(recipe("Egg Fried Rice",
                "A great way to use up leftover rice and frozen peas.", 25, 2,
                "Cook the rice, spread it out and let it cool (leftover rice works best).\n"
                        + "Heat the vegetable oil in a wok and scramble the eggs, then remove them.\n"
                        + "Add the rice and peas to the wok and stir-fry for 4 minutes.\n"
                        + "Return the eggs, add the soy sauce and sliced spring onions and toss well.",
                ing("rice", 200, "g"), ing("egg", 2, "pcs"), ing("peas", 100, "g"),
                ing("soy sauce", 2, "tbsp"), ing("vegetable oil", 2, "tbsp"), ing("spring onion", 2, "pcs")));

        recipes.add(recipe("Tomato Soup",
                "A comforting, smooth soup made from fresh tomatoes.", 35, 4,
                "Chop the onion, garlic and tomatoes.\n"
                        + "Melt the butter in a pot and soften the onion and garlic for 5 minutes.\n"
                        + "Add the tomatoes, stock and salt and simmer for 20 minutes.\n"
                        + "Blend until smooth and serve hot.",
                ing("tomato", 6, "pcs"), ing("onion", 1, "pcs"), ing("garlic clove", 2, "pcs"),
                ing("butter", 20, "g"), ing("vegetable stock", 500, "ml"), ing("salt", 2, "g")));

        recipes.add(recipe("Mashed Potatoes",
                "Creamy buttery mash, the perfect side dish.", 30, 4,
                "Peel the potatoes and cut them into even chunks.\n"
                        + "Boil in salted water for 15-20 minutes until soft, then drain.\n"
                        + "Warm the milk and butter together.\n"
                        + "Mash the potatoes, beat in the milk mixture and salt and serve.",
                ing("potato", 4, "pcs"), ing("butter", 40, "g"), ing("milk", 100, "ml"), ing("salt", 2, "g")));

        recipes.add(recipe("Potato Wedges",
                "Crispy oven-baked wedges with smoked paprika.", 45, 2,
                "Preheat the oven to 200 degrees Celsius.\n"
                        + "Cut the potatoes into wedges, leaving the skin on.\n"
                        + "Toss the wedges with the olive oil, salt and paprika.\n"
                        + "Spread on a baking tray and bake for 35 minutes, turning halfway.",
                ing("potato", 3, "pcs"), ing("olive oil", 3, "tbsp"), ing("salt", 3, "g"),
                ing("paprika", 3, "g")));

        recipes.add(recipe("Chicken Curry",
                "A mild, creamy coconut chicken curry.", 45, 4,
                "Cut the chicken into bite-sized pieces. Chop the onion, garlic and tomatoes.\n"
                        + "Heat the oil in a large pot and fry the onion until soft.\n"
                        + "Add the garlic and curry powder and cook for 1 minute.\n"
                        + "Add the chicken and brown it on all sides.\n"
                        + "Stir in the tomatoes and coconut milk and simmer for 25 minutes.\n"
                        + "Serve with rice or bread.",
                ing("chicken breast", 2, "pcs"), ing("onion", 1, "pcs"), ing("tomato", 2, "pcs"),
                ing("garlic clove", 3, "pcs"), ing("curry powder", 15, "g"), ing("coconut milk", 400, "ml"),
                ing("vegetable oil", 2, "tbsp")));

        recipes.add(recipe("Lemon Garlic Chicken",
                "Pan-fried chicken breasts with a zesty lemon glaze.", 30, 2,
                "Season the chicken breasts with the salt.\n"
                        + "Heat the olive oil in a pan and cook the chicken for 6-7 minutes per side.\n"
                        + "Add the crushed garlic and cook for 1 minute.\n"
                        + "Squeeze over the lemon juice, let it bubble for 1 minute and serve.",
                ing("chicken breast", 2, "pcs"), ing("lemon", 1, "pcs"), ing("garlic clove", 3, "pcs"),
                ing("olive oil", 2, "tbsp"), ing("salt", 2, "g")));

        recipes.add(recipe("Spaghetti Bolognese",
                "A classic rich beef and tomato pasta sauce.", 50, 4,
                "Finely chop the onion and garlic and chop the tomatoes.\n"
                        + "Heat the olive oil and fry the onion and garlic until soft.\n"
                        + "Add the beef mince and cook until browned, breaking it up.\n"
                        + "Add the tomatoes and simmer for 30 minutes.\n"
                        + "Cook the spaghetti until al dente, drain and serve with the sauce.",
                ing("beef mince", 500, "g"), ing("spaghetti", 300, "g"), ing("onion", 1, "pcs"),
                ing("garlic clove", 2, "pcs"), ing("tomato", 4, "pcs"), ing("olive oil", 1, "tbsp")));

        recipes.add(recipe("Grilled Cheese Sandwich",
                "Crispy toasted bread with gooey melted cheese.", 10, 1,
                "Butter one side of each bread slice.\n"
                        + "Place one slice butter-side down in a pan and top with the cheese.\n"
                        + "Cover with the second slice, butter-side up.\n"
                        + "Cook over a medium heat for 3 minutes per side until golden and melted.",
                ing("bread slice", 2, "pcs"), ing("cheddar cheese", 50, "g"), ing("butter", 15, "g")));

        recipes.add(recipe("Greek Salad",
                "A fresh salad of tomato, cucumber, olives and feta.", 15, 2,
                "Cut the tomatoes and cucumber into chunks.\n"
                        + "Thinly slice the red onion.\n"
                        + "Combine the vegetables and olives in a bowl.\n"
                        + "Crumble the feta on top and drizzle with the olive oil.",
                ing("tomato", 3, "pcs"), ing("cucumber", 1, "pcs"), ing("red onion", 1, "pcs"),
                ing("feta cheese", 100, "g"), ing("black olive", 50, "g"), ing("olive oil", 3, "tbsp")));

        recipes.add(recipe("Banana Smoothie",
                "A thick, naturally sweet breakfast smoothie.", 5, 2,
                "Peel and slice the bananas.\n"
                        + "Add the bananas, milk, yoghurt and honey to a blender.\n"
                        + "Blend until smooth and serve immediately.",
                ing("banana", 2, "pcs"), ing("milk", 250, "ml"), ing("plain yoghurt", 125, "g"),
                ing("honey", 1, "tbsp")));

        recipes.add(recipe("Banana Bread",
                "Moist loaf that rescues over-ripe bananas.", 75, 8,
                "Preheat the oven to 180 degrees Celsius and grease a loaf tin.\n"
                        + "Mash the bananas in a large bowl.\n"
                        + "Melt the butter and mix it into the bananas with the sugar and eggs.\n"
                        + "Fold in the flour and baking soda until just combined.\n"
                        + "Pour into the tin and bake for 60 minutes, until a skewer comes out clean.",
                ing("banana", 3, "pcs"), ing("flour", 250, "g"), ing("sugar", 150, "g"),
                ing("egg", 2, "pcs"), ing("butter", 100, "g"), ing("baking soda", 5, "g")));

        recipes.add(recipe("Oat Porridge",
                "Warm, creamy oats topped with banana and honey.", 10, 1,
                "Place the oats and milk in a small pot.\n"
                        + "Bring to a simmer and cook for 5 minutes, stirring often.\n"
                        + "Slice the banana.\n"
                        + "Spoon the porridge into a bowl and top with the banana and honey.",
                ing("rolled oats", 80, "g"), ing("milk", 300, "ml"), ing("banana", 1, "pcs"),
                ing("honey", 1, "tbsp")));

        recipes.add(recipe("Tuna Pasta Salad",
                "A filling cold pasta salad for lunchboxes.", 20, 3,
                "Cook the pasta until al dente, drain and rinse under cold water.\n"
                        + "Drain the tuna and sweetcorn.\n"
                        + "Finely chop the red onion.\n"
                        + "Mix everything with the mayonnaise and chill before serving.",
                ing("pasta", 250, "g"), ing("canned tuna", 170, "g"), ing("sweetcorn", 100, "g"),
                ing("red onion", 1, "pcs"), ing("mayonnaise", 4, "tbsp")));

        recipes.add(recipe("Bean and Cheese Quesadilla",
                "Crispy tortillas filled with beans, pepper and cheese.", 15, 2,
                "Drain the kidney beans and roughly mash them.\n"
                        + "Finely dice the bell pepper.\n"
                        + "Spread the beans over one tortilla and top with the pepper and grated cheese.\n"
                        + "Cover with the second tortilla.\n"
                        + "Dry-fry in a pan for 3 minutes per side, then cut into wedges.",
                ing("tortilla", 2, "pcs"), ing("kidney beans", 200, "g"), ing("cheddar cheese", 80, "g"),
                ing("bell pepper", 1, "pcs")));

        recipes.add(recipe("Guacamole",
                "A fresh avocado dip with lime and tomato.", 10, 4,
                "Halve the avocados, remove the stones and scoop the flesh into a bowl.\n"
                        + "Mash the avocado with the lime juice and salt.\n"
                        + "Finely chop the red onion and tomato and stir them through.\n"
                        + "Serve straight away.",
                ing("avocado", 2, "pcs"), ing("lime", 1, "pcs"), ing("red onion", 1, "pcs"),
                ing("tomato", 1, "pcs"), ing("salt", 1, "g")));

        recipes.add(recipe("Chickpea Curry",
                "A hearty vegetarian curry made from store-cupboard staples.", 35, 4,
                "Chop the onion, garlic and tomatoes.\n"
                        + "Heat the oil and fry the onion for 5 minutes until soft.\n"
                        + "Add the garlic and curry powder and cook for 1 minute.\n"
                        + "Add the tomatoes and drained chickpeas with a splash of water.\n"
                        + "Simmer for 20 minutes until thick and serve with rice.",
                ing("chickpeas", 400, "g"), ing("onion", 1, "pcs"), ing("tomato", 3, "pcs"),
                ing("garlic clove", 2, "pcs"), ing("curry powder", 10, "g"), ing("vegetable oil", 2, "tbsp")));

        recipes.add(recipe("Fruit Salad",
                "A colourful mix of fresh fruit with a honey drizzle.", 10, 4,
                "Core and dice the apples.\n"
                        + "Peel and slice the bananas.\n"
                        + "Peel the oranges and cut them into segments.\n"
                        + "Combine the fruit in a bowl, drizzle with the honey and toss gently.",
                ing("apple", 2, "pcs"), ing("banana", 2, "pcs"), ing("orange", 2, "pcs"),
                ing("honey", 1, "tbsp")));

        return recipes;
    }

    private static Recipe recipe(String name, String description, int minutes, int servings,
                                 String instructions, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(0, name, description, instructions, minutes, servings);
        for (RecipeIngredient ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }
        return recipe;
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
