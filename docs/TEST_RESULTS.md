# Test Results

What was actually run and observed. Dates are when the checks were carried out.

## 1. Automated tests (22–23 October 2026)

Command: `./gradlew testDebugUnitTest` (JUnit 4 + Robolectric 4.16.1, on the JVM).
The same command runs in GitHub Actions on every push (`.github/workflows/android-ci.yml`).

| Test class | Tests | What it covers | Result |
|---|---|---|---|
| `RecipeMatcherTest` | 21 | Brief's examples (3 eggs vs 4 or 2, missing milk, 4 of 5 ingredients), plurals, unrelated names, unit conversion, incompatible units, duplicate rows, expiry setting, "Almost there" | Pass |
| `IngredientNormalizerTest` | 9 | Singular/plural, synonyms, no false matches (butter vs peanut butter) | Pass |
| `UnitConverterTest` | 6 | Aliases, conversion factors, mass vs volume refused | Pass |
| `IngredientValidatorTest` | 9 | Blank, invalid and long names; empty, zero, negative, non-numeric and huge quantities; comma decimals | Pass |
| `ExpiryUtilsTest` | 4 | Date parsing, days until expiry, expired / expiring-soon boundaries | Pass |
| `QuantityFormatterTest` | 3 | Whole numbers, fractions, units | Pass |
| `RecipeSeedDataTest` | 4 | At least 20 recipes, unique names, complete data, sample pantry behaviour | Pass |
| `DatabaseTest` | 10 | Real SQLite: seeding without duplicates, full CRUD cycle, data after reopening, CHECK constraint, cascade delete, upgrade keeps pantry | Pass |
| `ScreenFlowTest` | 15 | Every Activity inflates, navigation Intents, validation, save/edit, empty states, settings persistence, add button label | Pass |
| **Total** | **81** | | **81 passed, 0 failed** |

Android Lint (`./gradlew lintDebug`): 0 errors. The remaining warnings are style suggestions
(for example plural strings) and one deliberate exception: the adaptive launcher icon must stay
in `mipmap-anydpi-v26`.

## 2. Manual checks on the emulator (22–23 October 2026)

Device: Android emulator, Google `sdk_gphone16k_x86_64` image, API 37, 1080 × 2400.
Screenshots of each check are in [`screenshots/`](screenshots) and in the written report.

| # | Check | Expected | Observed |
|---|---|---|---|
| 1 | Open the app with an empty pantry | Empty-state message | As expected |
| 2 | Save with a blank name and quantity 0 | Field errors, nothing saved | As expected |
| 3 | Add Rice, 1 kg, expiry 30 Oct 2026 | Back on the list, "Rice saved", 13 items | As expected |
| 4 | Edit Rice to 2 kg | List shows 2 kg | As expected |
| 5 | Delete Rice, confirm | Removed, back to 12 items | As expected |
| 6 | Sample pantry → Recipes | 5 of 24 ready to cook | As expected |
| 7 | Edit Eggs 6 → 2 | Scrambled Eggs and Cheese Omelette leave "Ready to cook" (5 → 3) and appear under "Almost there" with "egg (need 3 pcs, have 2 pcs)" | As expected |
| 8 | Edit Eggs back to 6 | Both recipes return (5) | As expected |
| 9 | Garlic Butter Pasta detail | Parmesan marked "Not in your pantry"; cannot cook | As expected |
| 10 | Settings → Clear pantry | Confirmation dialog, then empty pantry and "No recipes match your pantry yet" | As expected |
| 11 | Force-stop the app and relaunch | All 12 pantry items still there | As expected |
| 12 | Scroll the list, then clear the pantry | Add button shows its full label on the empty screen | Failed first (icon only); fixed in commit "Fix add button staying collapsed…", then as expected |
