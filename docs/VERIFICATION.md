# Verification record

## Latest verification — UC-08 follow-up, 2026-11-17

Environment: Windows, Temurin JDK 11.0.32 and Maven 3.9.16. These checks ran in
`team-project-allen-personal` after adding UC-08 input/output boundaries, save
feedback, regression tests and English project documentation.

- `mvn -B --no-transfer-progress verify`: **BUILD SUCCESS**, **58 tests passed**,
  zero failures, errors or skipped tests. The build produced
  `target/wordtrail.jar` with its JSON dependency and seed resources.
- The suite included two source-dependency architecture checks, five headless
  Swing workflow tests, and tests for authentication, presenters, learning,
  quizzes, personal vocabulary, persistence, hashing and dictionary adapters.
- `java -jar target/wordtrail.jar --help` against the UC-08 follow-up JAR:
  **passed**. The packaged entry point
  printed its usage and data-directory options without opening a GUI.
- The public dictionary API was not rechecked for this UC-08-only code change.
  The preceding baseline's successful live diagnostic is recorded below.
- The original worktree and its existing `TeamContract.md` edit were preserved.
  These changes are in a separate personal-feature worktree. No native GUI
  was launched for these checks.

## Coverage and style evidence

JaCoCo 0.8.16 measures all classes in the Entity and Use Case packages, including
the use-case ports, without excluding inconvenient classes or exception paths.

| Scope | Covered lines | Missed lines | Line coverage |
| --- | ---: | ---: | ---: |
| Entity | 102 | 0 | 100% |
| Use Case | 379 | 0 | 100% |
| Use Case ports | 6 | 0 | 100% |
| Complete course layers | **487** | **0** | **100%** |

New tests cover personal-word normalization, duplicates, input limits, missing
requests, account boundaries, read/save failures, exactly-once output after
persistence, presenter exceptions and stale view-model results. The personal
interactor has **53/53 lines and 18/18 branches covered**. This does not claim
100% branch coverage for the entire core.

The added Swing workflow verifies failed saves preserve the form and input,
successful saves select and scroll to the saved word, and corrected definition
edits persist with an English confirmation. The headless harness lays out new
component trees after each idle transition. Removing the scroll call made the
visibility assertion fail in a negative control; the call was restored before
the final successful full build. The real component render is retained at
`docs/screenshots/10-personal-save-result.png`.

The Maven `verify` lifecycle now enforces 100% line coverage for all Entity and
Use Case packages and subpackages. The gate passed against the current coverage
data. A negative control against the earlier 99.10% baseline correctly failed,
so the rule is checked as an actual gate rather than merely displayed in a report.

Checkstyle 10.26.1, through Maven Checkstyle 3.6.0, inspected **94 Java files** and
reported **666 findings** with the configured rules. Findings remain visible;
the report goal is non-blocking. The successful Maven build does not claim a
clean style report. Most current findings concern compact one-line statements,
brace layout, line length and wildcard imports. The report and diagnostic
messages are configured in English. A scan found no Chinese characters in the
generated Checkstyle XML or HTML reports.

Generated evidence after `mvn verify`:

- `target/site/jacoco-course-layers/index.html` and `jacoco.xml`: full course layers.
- `target/site/jacoco/index.html`: full application coverage, including UI/adapters.
- `target/site/checkstyle.html` and `target/checkstyle-result.xml`: actual findings.
- `target/surefire-reports/`: test cases and results.
- `target/ui-snapshots/`: offscreen renders of the actual Swing components.

CI uploads these reports as artifacts. New MVP use cases need their own tests and
must preserve the measured core-layer coverage; this snapshot does not cover
UC-09/10, which have not yet been implemented.

Automated dictionary tests use test doubles or a local HTTP server. A live API
diagnostic is a separate observation of service availability at the time of the
check, not a guarantee that the public service will remain available.

The Swing tests operate real components in headless mode. They do not establish
that every native window layout has been manually checked on another computer.
Consult the feature PR's checks for remote verification of this follow-up. A
teammate's local IDE setup and native GUI installation still need their own checks.

The shade plugin reported a shared `META-INF/MANIFEST.MF` resource warning. The
build succeeded, and the packaged entry point ran.

## Architecture scope

The implementation has entities, focused use-case input interfaces and
interactors, repository/service ports, infrastructure adapters, eight specific
controller/presenter pairs, plain view models and Swing views. The architecture tests check source
dependency directions; they are not a complete assessment of every design rule.

UC-08 edits use immutable input DTOs and an explicit output boundary implemented
by the dedicated presenter. The presenter writes an immutable save result to the
view model, and the Swing worker delivers it to the UI on the EDT. Other existing
use cases still return results through their controllers to presenters. The
architecture document describes this hybrid design and its remaining tradeoffs;
passing the tests does not establish full course compliance.

## Delivery and attribution

The team-contract PR #1 has been merged. Application review status is recorded in
the repository's pull requests. The original AI-assisted implementation
history is retained; it does not show that multiple teammates independently
implemented these use cases. Any assigned ownership, review and individual
contribution must reflect the actual team's work.

## Preceding delivery baseline — 2026-11-17

The delivery worktree passed 47 tests with 445/445 core lines covered and 661
Checkstyle findings across 87 Java files. Its packaged `--help` and live
`--check-api hello` diagnostics passed; the latter returned a definition from
`https://api.dictionaryapi.dev/api/v2/entries/en/hello`. The backup provider was
covered by automated tests but not exercised by that successful live request.

The [GitHub Actions run for e0214f3](https://github.com/fanfan170721/team-project/actions/runs/44185870260)
passed all four `mvn verify` jobs: Ubuntu and Windows, each with Java 11 and 17.
The [baseline PR run for 6763eff](https://github.com/fanfan170721/team-project/actions/runs/44187001425)
also completed successfully. These are baseline results, not the UC-08 follow-up's
CI evidence.

## Earlier observations — 2026-10-22

The earlier record reported 38 passing tests, inspected offscreen component
renders, and successful launcher checks. During that earlier extension, the
primary dictionary service returned HTTP 530 for `climate` and `accommodation`;
packaged-JAR diagnostics then succeeded through Datamuse. Those are historical
observations and were not repeated as part of the 2026-11-17 verification.
