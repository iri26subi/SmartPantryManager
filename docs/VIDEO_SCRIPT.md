# Video Script: Smart Pantry Manager (target 6:00, allowed 5:00–7:00)

Follows Section 5.1 of the assignment brief, in its order:

| Part | Brief requires | Target |
|---|---|---|
| 1 | GitHub walkthrough: scroll the commit history and narrate how the project evolved | 1:00 |
| 2 | Live demo: every core feature, a full CRUD cycle, proof of the strict rule, data after reopening | 2:30 |
| 3 | Concept explanation: **three** topics, pointing at the actual lines of code | 2:00 |
| 4 | Database justification: why SQLite | 0:45 |

How to read this script:
- **SHOW** is what should be on screen.
- **DO** is what you click.
- **SAY** is the narration. Say it in your own words; the brief marks your understanding, and reading word for word sounds flat.
- **CODE** is the file and line to open.

Spoken at a normal pace, the SAY text is about 5½ minutes. Keep talking while you tap and type, never
sitting in silence, so the demo actions don't push you past 7:00.

---

## 0. Before you press Record (10 minutes of setup)

**Recorder (OBS Studio, free):**
1. *Settings → Video*: Base and Output resolution **1920×1080**, 30 FPS.
2. *Settings → Output*, Output Mode **Advanced** → *Recording*: Format **mp4**, Encoder **x264**,
   Rate control **CBR**, Bitrate **800 Kbps**. Audio bitrate **128**. Six minutes at this rate is
   about 40 MB, which keeps the ZIP under 50 MB. Screens with little movement stay sharp at this bitrate.
3. Add a **Display Capture** source and your microphone. Do a 10-second test recording and play it
   back to check that your voice is clear.

**Android Studio:**
1. Open the project: *File → Open →* `E:\Documents\3rd year semester 2\mobile app dev 700\SmartPantryManager`.
2. Make the code readable on video: *File → Settings → Editor → Font → Size* **18**.
   (Alternative: *View → Appearance → Enter Presentation Mode* while explaining code.)
3. Line numbers are on by default. If not: right-click the left margin → *Show Line Numbers*.
4. Open these files in tabs now so you don't have to search while recording.
   Use **Ctrl+Shift+N** and type the file name:
   - `RecipeMatcher.java` and `MatchResult.java` (folder `app/src/main/java/com/smartpantry/manager/matching/`)
   - `DatabaseHelper.java` and `PantryRepository.java` (`…/manager/data/`)
   - `PantryAdapter.java` (`…/manager/ui/adapter/`) and `MainActivity.java` (`…/manager/ui/`)
5. To jump to a line during the video: click in the file, press **Ctrl+G**, type the line number, Enter.

**Emulator:**
1. Start it from *Device Manager* and run the app with the green **Run ▶** button.
2. In the app, go to **Settings → Clear pantry → Clear** so the demo starts from an empty pantry.
3. Close the app (swipe it away from Recents) so you can open it fresh on camera.

**Browser:** open https://github.com/iri26subi/SmartPantryManager in one tab and the
**Commits** page (click "*N* commits" under the green Code button) in a second tab.

**Optional:** the presentation deck can open the video (cover slide) and fill gaps between parts.
Its speaker notes match this script.

---

## Part 1: GitHub walkthrough (0:00 – 1:00)

**SHOW:** the GitHub repository home page.

**SAY:**
> Hi, my name is Irfaan Subrathee, student number 402311943, and this is my Mobile App Development 700
> project, Smart Pantry Manager. It's a Java Android app that tracks the ingredients you have at home and
> only suggests recipes you can cook with exactly those ingredients, to reduce food waste.
>
> This is the public GitHub repository, with a README, screenshots, and a badge showing that the
> automatic build and tests pass.

**DO:** scroll slowly down the README to the screenshots, then switch to the **Commits** tab.
Scroll from the **bottom (oldest)** to the **top (newest)**.

**SAY** (point at each commit as you reach it):
> It starts with the Android project: five screens, the SQLite database and the matching logic. Then a
> bug fix: the Add button stayed collapsed on an empty pantry, so I fixed it and added a test. Next come
> more unit tests and a GitHub Actions build, whose first run failed because the Gradle script wasn't
> executable on Linux. The last commits add screenshots, test results and the report.

---

## Part 2: Live demo (1:00 – 3:30)

**SHOW:** the emulator, full screen or next to Android Studio.

### 2a. Empty state (≈10 s)
**DO:** open Smart Pantry Manager from the app drawer.
**SAY:**
> When the pantry is empty, the app shows a helpful message instead of a blank screen.

### 2b. Create and validation (≈35 s)
**DO:** tap **Add ingredient**. Leave the name blank, type **0** in Quantity, tap **Save ingredient**.
**SAY:**
> Every form is validated. A blank name is rejected, and so is a quantity of zero or less.

**DO:** type **Rice** as the name, change the quantity to **1**, tap the Unit box and choose **kg**,
tap the expiry field and pick a date next week, tap **OK**, then **Save ingredient**.
**SAY:**
> With valid input the item is inserted into SQLite and I'm returned to the list, which refreshes
> straight away. That's the Create operation.

### 2c. Read (≈15 s)
**DO:** go to **Settings → Load sample pantry → Load**, then tap the **Pantry** tab and scroll the list.
**SAY:**
> To save time I've loaded a sample pantry. The list is a RecyclerView reading from the database: name,
> quantity, unit and expiry date, with a warning for items that expire soon. That's Read.

### 2d. Update (≈15 s)
**DO:** tap the **Rice** card, change the quantity to **2**, tap **Save ingredient**.
**SAY:**
> Tapping an item opens the same form in edit mode with its saved values. I change it to 2 kg and it's
> updated. That's Update.

### 2e. Delete (≈10 s)
**DO:** tap the red **bin** icon on Rice, then **Delete**.
**SAY:**
> And the bin icon deletes, after a confirmation. That completes the full CRUD cycle.

### 2f. Strict-matching proof (≈50 s), the most important part
**DO:** tap the **Recipes** tab. Point at **Ready to cook (5)** and at **Scrambled Eggs**.
**SAY:**
> The Suggested Recipes screen only lists recipes where every ingredient is in my pantry in a high
> enough quantity. Five of the 24 recipes qualify, including Scrambled Eggs, which needs three eggs.
> I have six.

**DO:** tap **Pantry**, tap **Eggs**, change 6 to **2**, **Save**, tap **Recipes**.
**SAY:**
> If I drop the eggs to two, Scrambled Eggs and Cheese Omelette both disappear, because they need three
> eggs. Partial matches are never suggested.

**DO:** tap **Almost there**.
**SAY:**
> They appear in this separate "Almost there" list, which shows exactly what's missing: egg, need three,
> have two. It's clearly separated from the real suggestions.

**DO:** still on **Almost there**, tap **Garlic Butter Pasta**. Point at the red cross on parmesan
cheese. Tap the back arrow.
**SAY:**
> Opening a recipe shows every ingredient with a tick or a cross. Here parmesan is missing, so this
> recipe can't be suggested. The method is underneath.

**DO:** set Eggs back to **6** and show Scrambled Eggs is back under Ready to cook.

### 2g. Persistence (≈15 s)
**DO:** press the square **Recents** button, swipe Smart Pantry Manager away, then reopen it.
**SAY:**
> Finally, I close the app completely and reopen it. All the data is still here, because it's stored
> in SQLite on the device, not in memory.

---

## Part 3: Concepts with the code (3:30 – 5:30)

**SHOW:** Android Studio. Brief: explain **three** topics and point at the actual lines.
These three are covered below. Keep each to about 40 seconds.

### 3a. The strict-matching algorithm (≈45 s)
**CODE:** `RecipeMatcher.java` → **Ctrl+G 115** (`checkIngredient`)

**SAY** (point at each line as you say it):
> The matching logic is in RecipeMatcher. For each recipe ingredient, checkIngredient gives one of four
> verdicts.
> - Line 123: if the pantry doesn't have the ingredient at all, it's **MISSING**.
> - Line 128: if I have it but in a unit that can't be compared, like grams against teaspoons, it's
>   **INCOMPATIBLE_UNIT**. Guessing a conversion could suggest a recipe I can't actually make.
> - Line 135: otherwise both amounts are converted to grams, millilitres or pieces and compared. Enough is
>   **AVAILABLE**; too little is **INSUFFICIENT**.

**CODE:** **Ctrl+G 147** (`buildPantryIndex`)
> Before that, buildPantryIndex groups the pantry by normalised name, so "Tomatoes" matches "tomato", and
> it adds up duplicate rows with compatible units.

**CODE:** switch to `MatchResult.java` → **Ctrl+G 35** (`isMatch`)
> And here is the strict rule itself. A recipe matches only if every single check is AVAILABLE. One
> missing or short ingredient returns false.

### 3b. The database, end to end (≈40 s)
**CODE:** `DatabaseHelper.java` → **Ctrl+G 58**
> The database uses SQLiteOpenHelper with three tables: pantry_items, recipes and recipe_ingredients.
> Line 82 is the foreign key from recipe ingredients to recipes, with ON DELETE CASCADE, and line 118
> switches foreign keys on.

**CODE:** **Ctrl+G 125** (`onCreate`) then **154** (`seedRecipes`)
> onCreate runs only once, when the database file is first created. It builds the tables and calls
> seedRecipes, which inserts the 24 recipes in one transaction, line 158. It checks the row count first,
> so recipes are never duplicated.

**CODE:** `PantryRepository.java` → **Ctrl+G 35**, then scroll past **66, 96, 109**
> PantryRepository holds the CRUD operations: insert, getAll, update and delete. Every query uses
> question-mark placeholders instead of joining user input into SQL.

### 3c. RecyclerView and the custom Adapter (≈35 s)
**CODE:** `MainActivity.java` → **Ctrl+G 96** (`onResume`) and **108**
> The pantry screen reloads the data in onResume, so after adding or editing an item the list is always
> current. Line 108 hands the new list to the adapter.

**CODE:** `PantryAdapter.java` → **Ctrl+G 68**, then **74**, then **130**
> In the adapter, onCreateViewHolder inflates the card layout. onBindViewHolder fills one card with an
> item's name, quantity and expiry. The ViewHolder class caches the views, so findViewById isn't called
> on every scroll.

**CODE:** **Ctrl+G 83**, then `MainActivity.java` **Ctrl+G 144**
> Clicks go back to the activity through a Listener interface. MainActivity then starts the edit screen
> with an explicit Intent, passing the item's id as an extra.

---

## Part 4: Database justification (5:30 – 6:15)

**SHOW:** the database slide in the deck, or `DatabaseHelper.java`.

**SAY:**
> I chose SQLite for four reasons. First, a pantry is personal data for one user on one device, so
> there's nothing to sync. Second, it works fully offline, with no account, server or cost. Firebase or
> PostgreSQL would add a network dependency and a backend for data that never leaves the phone. Third,
> the data is relational: a recipe has many ingredients, which suits tables with a foreign key. And
> fourth, SQLite is built into Android and was covered in the module. I used SQLiteOpenHelper rather
> than Room to keep the SQL visible and easy to explain.
>
> That's Smart Pantry Manager. Thank you for watching.

**DO:** stop recording.

---

## After recording

1. Check the length is between **5:00 and 7:00** and that your voice is audible throughout.
2. Check the file size. If it's over about 40 MB, re-export at a lower bitrate, or ask for it to be compressed.
3. Name it `402311943_Subrathee_MobileAppDev700_Video.mp4`. It then goes into the submission ZIP next to
   the report.

## If something goes wrong while recording

- **Wrong tap or a typo:** keep going, fix it on screen and say what you're doing. Fixing it live is fine.
- **The recipe counts differ** (because the pantry isn't exactly the sample): explain the rule on whatever
  recipe is shown. What matters is that a recipe disappears when one ingredient becomes short.
- **Two Eggs rows:** if you added your own Eggs and also loaded the sample, the rows add up. Delete one
  row before the strict-rule step.
- **Running long:** shorten Part 2a and 2c. Never cut 2f (strict rule) or 2g (persistence).
