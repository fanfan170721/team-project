# Running and modifying WordTrail

## Run the application

The project uses Java 11, Maven and Java Swing. CI checks JDK 11 and 17. Install JDK 11 or later and
Maven 3.9 or later before using the command-line build. From the project root, run:

```sh
mvn clean verify
java -jar target/wordtrail.jar
```

In IntelliJ IDEA, open `pom.xml`, wait for Maven import, select JDK 11 or later,
and run `src/main/java/com/wordtrail/app/Main.java`. Keep the working directory
set to the project root.

On Windows, double-click `run.cmd` in the project root, or run this in PowerShell:

```powershell
.\run.ps1
```

The launcher uses the project root as its working directory and detects added,
changed and deleted build inputs. Use `.\run.ps1 -Build` to force a clean build and tests.
`.\run.ps1 -Check` checks the packaged entry point without opening a window or
accessing the network.

`mvn verify` creates `target/wordtrail.jar`, including runtime dependencies and
sample content. Running the packaged JAR only requires Java 11 or later. Build
artifacts and local account data are excluded from Git.

To check the packaged application without opening a window or using the network:

```sh
java "-Djava.awt.headless=true" --class-path target/wordtrail.jar tools/VerifyPackage.java target/package-check
```

This checks packaged dependencies and seed content, saves a test word and verifies
it survives reopening an isolated profile. It does not use personal account data.

The shared feature base is `Emilia/project-delivery`. UC-08 is available in the UI.
UC-09 currently has a tested core only; CSV export and a review-plan screen are
not yet available. See [HANDOFF.md](HANDOFF.md) for current contribution status.

The first launch asks you to create an administrator. Usernames contain 3–30
English letters, digits or underscores; passwords contain 8–128 characters.
Usernames are case-insensitive. No default username or password is provided.
Setup signs in the new administrator. To create a learner, log out and change
**Action** to **Register a learner account**. Administrators can also use all
learning features.

## Use the screens

1. **Vocabulary:** Select a topic or search for a word or meaning. Selecting a
   table row shows its definition and example. **Save for review later** adds it
   to your private review list; online lookup calls the dictionary API.
   **Add my word** creates a personal word, and **Edit my definition** changes
   your definition of the selected word. These changes affect only the current
   account and are used by flashcards and newly started quizzes.
2. **Flashcards:** Select a topic and choose **Start deck**. Select **Use review
   list only** to study saved words. Recall the meaning, select **Reveal
   definition**, then **Complete review** to record a completed review. A card
   visit can be recorded once. After moving to another card and returning, the
   word can be reviewed again, including on the same day. Daily study totals
   still count distinct words. **Next card** wraps to the first card after the
   last one.
3. **Quiz:** Choose a topic and question count. When fewer questions are
   available, the quiz uses those available. Submit one answer, read the
   feedback and continue. Results are saved only after the final question;
   abandoned quizzes are not recorded.
4. **Review later:** View and remove words saved by the current account.
   **Reviews** shows completed-review counts and **Last reviewed** shows the
   most recent completion time. **Order** supports alphabetical order, lowest
   or highest review count, and most recent review. Column headers also sort
   the table. The Vocabulary screen offers the same ordering choices.
5. **Progress:** View distinct words studied, the study streak, daily totals for
   the last 14 days, quiz history and overall accuracy. Accuracy is weighted
   across all answered questions.
6. **Admin:** This screen is available only to administrators. Add a word, then
   use **Add quiz question** to link a question to that word, enter four distinct
   choices and select the correct answer. You can add new topics. Online
   lookup can fill a definition, which remains editable before saving.

## Data and APIs

By default, data is stored in `.wordtrail/wordtrail.json` under the working
directory. Close the application before copying this file as a backup. Do not
commit personal account data; `.gitignore` excludes its directory. To use a
separate demonstration environment, run:

```sh
java -jar target/wordtrail.jar --data-dir target/my-demo
```

Only one application instance can use a data directory at a time. If stored data
is corrupt, the application reports an error and preserves the original file
instead of clearing it. Rebuilding the project does not remove existing data.

The primary external service is [Free Dictionary API](https://dictionaryapi.dev/).
An earlier live check encountered HTTP 530, so the implementation includes
[Datamuse](https://www.datamuse.com/api/) as a fallback. The interface displays
the responding provider's source link and keeps online results separate from
your saved definition. Looking up a word does not save a change. If both
services fail, the application reports the failure and preserves saved data.
Datamuse previously announced an API-key requirement for 2027; consult its
current documentation before continuing to use or replacing that adapter.

The course DAO served as a design reference; this project does not copy its
shared HTTP-server or plaintext-password approach. Dictionary lookup provides
the external API integration, while local JSON provides persistence. The 16
sample words and 16 sample questions remain usable offline; online lookup
reports a connection error when unavailable.

Existing accounts and learning records remain compatible; do not delete
`.wordtrail` when updating. Older records without review counts start counting
when completed reviews are first recorded; historical counts are not inferred.
After updating, close any older WordTrail window and restart with `run.cmd` or
`Main.main()`.

To check the live dictionary service separately, run:

```sh
java -jar target/wordtrail.jar --check-api hello
```

## Find the code to change

| Change | Main location |
| --- | --- |
| Screens, wording and layout | `src/main/java/com/wordtrail/view/` |
| Input coordination and display conversion | `src/main/java/com/wordtrail/adapter/` |
| Use-case business rules | `src/main/java/com/wordtrail/usecase/` |
| Domain data | `src/main/java/com/wordtrail/entity/` |
| Files, APIs and password hashing | `src/main/java/com/wordtrail/data/` |
| Dependency wiring and entry point | `src/main/java/com/wordtrail/app/` |
| Initial vocabulary and questions | `src/main/resources/seed.json` |
| Unit and integration tests | `src/test/java/com/wordtrail/` |

`seed.json` is imported only when a data directory is first created. To test
updated seed content, use a new `--data-dir` or add content through the
administrator forms. Do not delete useful learning records to reload seed data.

## Git and teamwork

The integration branch is `Emilia/project-delivery`. It preserves the authorship
and history of the original AI-assisted implementation and includes the signed
TeamContract from `main`. Contract PR #1 has been merged.

The README distinguishes implemented baseline features from planned MVP work.
Fuxuan's CSV export and Xinyu's daily review plan remain assigned work for those
members; their operating instructions are in [TEAM_TASKS.zh-CN.md](TEAM_TASKS.zh-CN.md).
Assignments, contract signatures and documentation-only changes do not establish
that an individual's use case has been implemented.

Use branches, tests and pull requests for feature changes, and retain actual
review records. `mvn verify` also generates coverage and Checkstyle reports;
see [VERIFICATION.md](VERIFICATION.md) and [DEVELOPMENT.md](DEVELOPMENT.md).

Read [ARCHITECTURE.md](ARCHITECTURE.md) and [DEMO.md](DEMO.md) before presenting,
and be able to explain the code and design choices.
