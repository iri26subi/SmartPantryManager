# GitHub Guide

Repository: **https://github.com/iri26subi/SmartPantryManager** (public, branch `main`).

## Getting the project on another computer

```bash
git clone https://github.com/iri26subi/SmartPantryManager.git
```

Then open the folder in Android Studio (**File → Open**) and let Gradle sync.

## Making a change

```bash
git status                      # see what changed
git add <files>
git commit -m "Clear message saying what changed and where"
git push
```

Good messages say *what* and *where*, for example
`Fix add button staying collapsed when the pantry becomes empty`.
Avoid `update`, `final` or `changes`.

## Continuous integration

Every push to `main` runs [`.github/workflows/android-ci.yml`](../.github/workflows/android-ci.yml),
which builds the debug APK and runs all unit tests. The result is shown by the
**Android CI** badge in the README and on the repository's **Actions** tab, where the
HTML test report can be downloaded from each run.

## What is not committed

`.gitignore` leaves out build output (`build/`, `.gradle/`), Android Studio settings (`.idea/`),
`local.properties` (it holds the path to the Android SDK on one computer), signing keys,
and the assignment brief PDF. `.gitattributes` keeps `gradlew` with Unix line endings, and the
file is stored as executable so it runs on Linux and macOS.

## Submission checklist (Section 4 of the brief)

- [x] Public repository
- [x] README with the app name and description, database choice and reason, setup and run steps
- [ ] At least 10 meaningful commits (check with `git log --oneline | wc -l`)
- [ ] Repository link in the report opens in a private/incognito browser window
