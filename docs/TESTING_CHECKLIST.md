# Testing Evidence Checklist

Tick each item **after you have run it yourself** on an emulator or phone. Take
a screenshot for the report where marked 📸. Do not tick anything you have not
actually checked.

## A. Automated tests (Android Studio or terminal)

- [ ] `./gradlew testDebugUnitTest` passes (73 tests). 📸 the green test run in
      Android Studio (right-click `app/src/test/java` → *Run 'Tests in ...'*) or the HTML
      report at `app/build/reports/tests/testDebugUnitTest/index.html`
- [ ] `./gradlew assembleDebug` builds `app-debug.apk`

## B. Pantry CRUD

| # | Steps | Expected | ✓ |
|---|---|---|---|
| B1 | Fresh install, open app | Empty-state message on Pantry screen 📸 | [ ] |
| B2 | + → name `Eggs`, 6, pcs, pick an expiry date → Save | Back on Pantry, "Eggs saved" Snackbar, card shows 6 pcs and the date 📸 | [ ] |
| B3 | Tap the Eggs card → change to 4 → Save | Card shows 4 pcs (Update) 📸 | [ ] |
| B4 | Bin icon on Eggs → Delete | Confirmation dialog 📸, then item removed (Delete) | [ ] |
| B5 | Add an item with an expiry date 1–2 days away | "Expiring soon" badge and yellow banner 📸 | [ ] |
| B6 | Add an item with yesterday's expiry date | "Expired" badge | [ ] |

## C. Input validation

| # | Input | Expected | ✓ |
|---|---|---|---|
| C1 | Blank name | "Please enter an ingredient name" 📸 | [ ] |
| C2 | Name `123` or `milk!` | "Use letters, numbers, spaces…" | [ ] |
| C3 | Quantity blank | "Please enter a quantity" | [ ] |
| C4 | Quantity `0` | "Quantity must be greater than zero" 📸 | [ ] |
| C5 | Quantity `-2` | "Quantity must be greater than zero" | [ ] |
| C6 | Quantity `1,5` | Accepted as 1.5 | [ ] |
| C7 | Fix an error and type again | The error message clears | [ ] |

## D. Strict matching (the most important part)

Start from an empty pantry each time (Settings → Clear pantry).

| # | Pantry | Check in Recipes | Expected | ✓ |
|---|---|---|---|---|
| D1 | Empty | Ready to cook | "No recipes match your pantry yet" 📸 | [ ] |
| D2 | Settings → Load sample pantry | Ready to cook | Includes Scrambled Eggs, Tomato Pasta, Grilled Cheese Sandwich 📸 | [ ] |
| D3 | Edit Eggs to **2** | Ready to cook | Scrambled Eggs (needs 3) **disappears** 📸 | [ ] |
| D4 | Edit Eggs back to **4** | Ready to cook | Scrambled Eggs **reappears** 📸 | [ ] |
| D5 | Sample pantry | Almost there | Garlic Butter Pasta, "Missing: 30 g parmesan cheese" 📸 | [ ] |
| D6 | Add `Parmesan cheese` 100 g | Ready to cook | Garlic Butter Pasta now listed | [ ] |
| D7 | Add `tomatoes` (plural) | Detail of a tomato recipe | Tomato ingredient shows a tick (plural handled) | [ ] |
| D8 | Milk stored as 1 L, recipe needs 1 cup | Pancakes detail | Milk ticked (unit conversion) | [ ] |
| D9 | Salt stored as 1 tsp (volume), recipe needs 1 g | Scrambled Eggs detail | Salt shows "unit that cannot be compared", recipe excluded | [ ] |
| D10 | Two rows: Eggs 2 pcs + Eggs 1 pcs | Ready to cook | Scrambled Eggs listed (duplicates combined) | [ ] |
| D11 | Settings → Ignore expired ON, Milk expired | Ready to cook | Recipes needing milk excluded | [ ] |

## E. Navigation

- [ ] Bottom bar moves between Pantry, Recipes and Settings
- [ ] Back from Recipes or Settings returns to Pantry
- [ ] Tapping a recipe opens Recipe Detail; toolbar arrow, Back button and system Back return to the list
- [ ] Rotating the Add/Edit screen keeps the typed values

## F. Persistence

- [ ] Add items, **swipe the app away from Recents** (or Settings → Apps → Force stop), reopen: items still there 📸
- [ ] Change a setting, close and reopen the app: setting kept
- [ ] Recipe count in Settings → About stays at 24 after several restarts (no duplicate seeding)
