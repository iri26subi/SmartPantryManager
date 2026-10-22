# Written Report Template: Smart Pantry Manager

> Use this as the skeleton for your Word/PDF report. Sections are in the order
> the brief requires (Section 6). Everything in **[square brackets]** is yours
> to fill in. Screenshots, challenges and reflections must be **your own**,
> taken from your own running app and your own experience.
> File name: `Studentnumber_Surname_MobileAppDev700_Assignment.docx`

---

## 1. Cover page

- **Smart Pantry Manager**
- [Your full name]
- [Student number]
- Mobile App Development 700
- [Submission date]
- GitHub repository: [https://github.com/your-username/SmartPantryManager] (must be a working link)

## 2. Table of contents

Use Word's automatic table of contents (References → Table of Contents) after
applying Heading 1/Heading 2 styles.

## 3. Introduction

Points to cover in your own words:
- The problem: households throw away food because they forget what they have, or
  find recipes that need a shopping trip.
- What the app does: tracks the pantry and suggests only recipes that can be cooked
  right now, using the strict-matching rule.
- Who it is for: [e.g. students, families, anyone trying to cut waste and save money].
- Platform and technologies: Java, Android Studio, SQLite (SQLiteOpenHelper),
  RecyclerView, Material Design components.

## 4. System design

### 4.1 Screen flow

Redraw this neatly (draw.io, PowerPoint or by hand) and insert it as a figure:

```
                ┌──────────────── Bottom navigation ────────────────┐
                │                                                   │
        ┌───────▼───────┐        ┌────────────────────┐      ┌──────▼──────┐
        │  Pantry List  │◄──────►│ Suggested Recipes  │◄────►│  Settings   │
        │ (MainActivity)│        │ Ready / Almost     │      │             │
        └───┬───────▲───┘        └─────────┬──────────┘      └─────────────┘
   + / tap  │       │ save / back          │ tap recipe (EXTRA_RECIPE_ID)
 (EXTRA_ITEM_ID)    │                      ▼
        ┌───▼───────┴───┐        ┌────────────────────┐
        │ Add / Edit    │        │   Recipe Detail    │
        │ Ingredient    │        │ (back to list)     │
        └───────────────┘        └────────────────────┘
```

### 4.2 Data model / ER diagram

```
┌────────────────────┐       ┌────────────────────┐ 1     * ┌──────────────────────┐
│   pantry_items     │       │      recipes       │─────────│  recipe_ingredients  │
├────────────────────┤       ├────────────────────┤         ├──────────────────────┤
│ PK id              │       │ PK id              │         │ PK id                │
│    name            │       │    name (UNIQUE)   │         │ FK recipe_id         │
│    quantity (>0)   │       │    description     │         │    name              │
│    unit            │       │    instructions    │         │    quantity (>0)     │
│    expiry_date     │       │    prep_minutes    │         │    unit              │
└────────────────────┘       │    servings        │         └──────────────────────┘
                             └────────────────────┘
```

`pantry_items` has no foreign key to recipes on purpose: pantry items are linked
to recipe ingredients **by normalised name at run time** (the matching algorithm),
not by a stored id. Explain in your own words why that makes sense.
Settings are stored in SharedPreferences (key/value), not in a table.

## 5. Screenshots of every output

Take these on **your own emulator/phone** (Android Studio: Running Devices → camera
icon, or `Ctrl+S` in the emulator). Give each a figure number and a one- or
two-sentence caption saying what it shows.

| # | Screenshot to take | Caption should mention |
|---|---|---|
| 1 | Pantry screen, empty | Empty-state message |
| 2 | Add Ingredient form, empty | Fields, unit dropdown |
| 3 | Validation errors (blank name, quantity 0) | Input validation |
| 4 | Date picker open | Optional expiry date |
| 5 | Pantry list with several items | RecyclerView + custom adapter, expiry badges |
| 6 | Edit Ingredient with existing data | Update (CRUD) |
| 7 | Delete confirmation dialog | Delete (CRUD) |
| 8 | Pantry after the delete (Snackbar visible) | List refreshed |
| 9 | Suggested Recipes: "Ready to cook" with results | Strict matching |
| 10 | Suggested Recipes: no matches message | Zero-match feedback |
| 11 | "Almost there" tab | Optional stretch, clearly separate |
| 12 | Recipe Detail: all ticks | Recipe you can make |
| 13 | Recipe Detail: a cross showing a missing/short ingredient | Why a recipe is excluded |
| 14 | Strict rule proof: before and after removing one ingredient | Recipe disappears |
| 15 | Settings screen | Saved preferences |
| 16 | App reopened, data still there | Persistence |

## 6. Key code snippets (choose 3–5)

Suggested snippets. Copy **short** excerpts and explain each in your own words:

1. `RecipeMatcher.checkIngredient(...)` and `MatchResult.isMatch()`: the strict
   "every ingredient, enough quantity" rule.
2. `IngredientNormalizer.normalize(...)` / `singularize(...)`: why exact matching
   after normalisation avoids both "tomato vs tomatoes" failures and false matches
   like "butter vs peanut butter".
3. `UnitConverter`: base units, and why mass and volume are deliberately not converted.
4. `DatabaseHelper.onCreate(...)` + `seedRecipes(...)`: schema, foreign key, seeding
   once inside a transaction.
5. `PantryAdapter.onBindViewHolder(...)` or `MainActivity.onResume()` → `loadPantry()`:
   how the RecyclerView shows fresh database data.

## 7. Challenges and solutions (2–3)

Write about **real** problems you met while building, running or changing the app,
for example a build/Gradle error, an emulator problem, a bug you found while testing,
or something in the matching logic you had to think through. For each: what
happened, how you worked out the cause, and what fixed it.

- [Challenge 1]
- [Challenge 2]
- [Challenge 3 (optional)]

## 8. Conclusion and reflection

- [What you learned about Activities and their lifecycle, Intents, RecyclerView, SQLite]
- [What works well]
- [What you would improve with more time. Ideas: "Mark as cooked" that subtracts
  the ingredients from the pantry, barcode scanning, user-added recipes, expiry
  notifications with WorkManager, density-based gram/ml conversion for common
  ingredients, background threads for database work]

## 9. Reference list

Use one consistent style (e.g. Harvard). List only sources you actually used, for example:

- Android Developers (n.d.) *Save data using SQLite*. Available at:
  https://developer.android.com/training/data-storage/sqlite (Accessed: [date]).
- Android Developers (n.d.) *Create dynamic lists with RecyclerView*. Available at:
  https://developer.android.com/develop/ui/views/layout/recyclerview (Accessed: [date]).
- Android Developers (n.d.) *Intents and intent filters*. Available at:
  https://developer.android.com/guide/components/intents-filters (Accessed: [date]).
- Android Developers (n.d.) *The activity lifecycle*. Available at:
  https://developer.android.com/guide/components/activities/activity-lifecycle (Accessed: [date]).
- Material Design (n.d.) *Material Components for Android*. Available at:
  https://m3.material.io/develop/android/mdc-android (Accessed: [date]).
- [Your module notes / textbook]
- [Any AI tools used, if your institution requires you to declare them]
