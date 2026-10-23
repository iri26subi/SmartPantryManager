# Video Demonstration Script (5–7 minutes)

Structure from the brief (Section 5.1). Target timings add up to about 6 minutes,
which leaves room either way. **Narrate in your own words**: the notes below are
prompts, not lines to read out. Record at 1080p (OBS Studio works well), export
as MP4 (H.264) and keep the file small enough for the 50 MB ZIP limit.

**Before recording:** uninstall the app from the emulator (fresh install), open
Android Studio with the key files in tabs (`RecipeMatcher.java`, `MatchResult.java`,
`DatabaseHelper.java`, `PantryRepository.java`, `PantryAdapter.java`, `MainActivity.java`),
open https://github.com/iri26subi/SmartPantryManager in a browser, and have the
presentation deck ready. Its speaker notes follow this script.

---

## 1. GitHub walkthrough (≈ 1:00)

- Show the repository page, the README (screenshots, database choice) and the green
  **Android CI** badge.
- Open **Commits** and scroll through the history from oldest to newest.
- Briefly say how the project developed: the initial app, the add-button bug found on the
  emulator and fixed with a test, extra unit tests, the CI workflow (and the failed first run
  caused by `gradlew` not being executable), then documentation, screenshots and the report.

## 2. Live app demo (≈ 2:30)

1. Launch the app: empty pantry message.
2. **Create:** add `Rice`, 1, kg, with an expiry date. Then show validation:
   save with a blank name and with quantity `0` to show the error messages.
3. **Read:** Settings → Load sample pantry, then show the full list on the Pantry screen.
4. **Update:** tap Rice, change its quantity, save.
5. **Delete:** bin icon on Rice → confirm.
6. **Strict-matching proof** (keep only ONE Eggs row; rows with the same name are added together):
   - Open Recipes → Ready to cook. Point out Scrambled Eggs (needs 3 eggs; the pantry has 6).
   - Go back to the pantry and edit Eggs to **2**. Return to Recipes: Scrambled Eggs is **gone**.
   - Open the "Almost there" tab: Scrambled Eggs shows "egg (need 3 pcs, have 2 pcs)".
   - Set Eggs back to 4: it reappears under Ready to cook.
7. Open a recipe: ingredients with ticks/crosses and the method. Go back.
8. Settings: toggle a setting.
9. **Persistence:** swipe the app away from Recents, reopen it, and show the data and setting are still there.

## 3. Concept explanation (≈ 2:00): pick THREE and point at the code

| Topic | Where to point |
|---|---|
| Strict-matching algorithm | `matching/RecipeMatcher.java`: `buildPantryIndex()` (combines duplicates, base units), `checkIngredient()` (MISSING / INSUFFICIENT / INCOMPATIBLE_UNIT / AVAILABLE), `MatchResult.isMatch()` (every ingredient must be AVAILABLE). Mention `IngredientNormalizer` (plurals) and `UnitConverter` (no gram↔ml). |
| Database end-to-end | `data/DatabaseHelper.java` (tables, foreign key, `onCreate` seeding once, `onUpgrade` keeps the pantry) → `data/PantryRepository.java` (insert/getAll/update/delete with `?` placeholders) → called from `MainActivity.loadPantry()` and `AddEditIngredientActivity.saveIngredient()`. |
| RecyclerView + Adapter | `ui/adapter/PantryAdapter.java`: `onCreateViewHolder` inflates `item_pantry.xml`, `ViewHolder` caches views, `onBindViewHolder` fills them, the `Listener` interface sends clicks back to `MainActivity`. |
| Intents | `AddEditIngredientActivity.newEditIntent()` puts `EXTRA_ITEM_ID`; `MainActivity` launches it with `registerForActivityResult` and gets a result message back; `RecipeDetailActivity.newIntent()` passes `EXTRA_RECIPE_ID`; `BaseActivity.navigateToTab()` for the bottom bar. |
| Activity lifecycle | `onCreate` sets up views once; `onResume` reloads data in `MainActivity` and re-runs matching in `SuggestedRecipesActivity`, so returning from another screen always shows current data; `onSaveInstanceState` keeps the expiry date and selected tab across rotation. |

## 4. Database justification (≈ 0:45)

Why SQLite (say it in your own words): data is personal and stays on one device;
works offline; no server, account or cost; covered in the module; relational
tables suit recipes ↔ ingredients with a foreign key; SQLiteOpenHelper keeps the
SQL visible and easy to explain.

---

**Final check:** the video is between 5:00 and 7:00, your voice is audible
throughout, and the text on screen is readable.
