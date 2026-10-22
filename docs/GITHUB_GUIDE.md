# GitHub Setup Guide

The brief (Section 4) needs a **public** GitHub repository with **at least 10
genuine, incremental commits** that have clear messages, and a README. Your
commit history is checked against your video and report timeline, and a
history made in one sitting just before the deadline gets flagged.

So commit **as you really work**: each time you study, change, test or fix
something, commit that step. Do not backdate commits or invent history.

## 1. One-time setup

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Create an empty **public** repository on github.com named `SmartPantryManager`
(do not add a README there, because the project already has one).

## 2. First commit and push

From inside the `SmartPantryManager` folder:

```bash
git init
git add .
git commit -m "Add Smart Pantry Manager project skeleton"
git branch -M main
git remote add origin https://github.com/<your-username>/SmartPantryManager.git
git push -u origin main
```

`.gitignore` already excludes `build/`, `.gradle/`, `.idea/` and `local.properties`.

You can also do all of this from Android Studio (VCS → *Share Project on GitHub*)
or from GitHub Desktop, which is installed on this PC.

## 3. Commit as you go

Typical genuine steps while you work through the project:

- Reading and testing the matcher → add your own test case → commit
  `Add test for <case> in RecipeMatcherTest`
- Running on the emulator and fixing something you notice → commit
  `Fix <what> on <screen>`
- Personalising the app (your own recipes, colours, wording) → commit per change
- Adding comments or explanations you will use in the video → commit
- Updating the README with your own notes → commit

Good messages say *what* and *where*: `Add expiry date picker to AddEditIngredientActivity`.
Avoid `update`, `final`, `changes`.

```bash
git add <files>
git commit -m "Clear message describing this step"
git push
```

## 4. Before submitting

- [ ] The repository is public, or the marker has been added as a collaborator
- [ ] At least 10 meaningful commits, spread over your real working sessions
- [ ] README shows the app name, description, database choice and reason, setup steps
- [ ] The repository link in your report opens correctly in a private/incognito browser window
