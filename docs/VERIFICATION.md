# Verification record

Date: 2026-10-22. Local environment: Windows 11, Temurin JDK 11.0.32.1,
Maven 3.9.16.

- `mvn -B --no-transfer-progress verify`: **passed**, 38 tests, zero failures,
  errors or skips. The executable artifact is `target/wordtrail.jar`.
- The original `--check-api hello` check passed against Free Dictionary API earlier
  in the day. During this extension it returned HTTP 530 for `climate` and
  `accommodation`. After adding the backup, both packaged-JAR live checks passed
  and returned real definitions sourced from `https://api.datamuse.com/words`.
- Full headless Swing workflow: initial administrator setup, browsing/filtering,
  save/review, reveal/study flashcard, quiz validation/completion, admin word and
  question creation, learner isolation, application close/reopen and history recovery.
- Nine real component renders generated in `target/ui-snapshots/`; the personal
  editor, numeric review sorting and online-result screens were also inspected.
- New workflow checks cover learner additions and definition edits, account
  isolation, two reviews of the same word on one day, restart recovery, generated
  personal quizzes, correct answers moving between positions, lookup after table
  sorting, and usable controls/preserved saved content after a lookup failure.
- Version-1 progress without new fields loads with its study history preserved.
- `git diff --check`: no whitespace errors.
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Check`:
  **passed**. The Windows launcher locates the packaged entry point without
  opening a GUI or making a network request.
- The same launcher with `-Build -Check` runs the full verification before checking
  the entry point. The JAR plugin always recreates the input artifact so incremental
  builds do not re-shade the previous dependency-containing JAR.

The Swing render is an offscreen render of the actual components, not a screenshot
of a manually operated native desktop window. Automated tests use deterministic
dictionary doubles or a local HTTP server. The public service is checked separately
so normal CI does not require it to be online.

Java 17 and Linux execution are configured in CI and were not executed locally.
Delivery is local only; no branch was uploaded and no remote workflow or PR was
created. The CI configuration is not evidence of a remote test run.
The shade plugin reports the expected shared
`META-INF/MANIFEST.MF` resource; its manifest transformer provides the executable
main class.
