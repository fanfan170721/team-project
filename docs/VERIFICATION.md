# Verification record

## Latest verification — 2026-11-17

Environment: Windows, Temurin JDK 11.0.32 and Maven 3.9.16. The final integration
checks ran in `team-project-delivery` after splitting the controller/presenter
adapters, adding boundary regression tests and configuring quality reports.

- `mvn -B --no-transfer-progress verify`: **BUILD SUCCESS**, **47 tests passed**,
  zero failures, errors or skipped tests. The build produced
  `target/wordtrail.jar` with its JSON dependency and seed resources.
- The suite included two source-dependency architecture checks, four headless
  Swing workflow tests, and tests for authentication, presenters, learning,
  quizzes, personal vocabulary, persistence, hashing and dictionary adapters.
- `java -jar target/wordtrail.jar --help` against the final integration JAR:
  **passed**. The packaged entry point
  printed its usage and data-directory options without opening a GUI.
- The final integration JAR's `java -jar target/wordtrail.jar --check-api hello`:
  **passed** against the live
  primary Free Dictionary API. It returned a definition and the source
  `https://api.dictionaryapi.dev/api/v2/entries/en/hello`.
- The original worktree and its existing `TeamContract.md` edit were preserved.
  All integration changes are in the separate delivery worktree. No native GUI
  was launched for these checks.

## Coverage and style evidence

JaCoCo 0.8.16 measures all classes in the Entity and Use Case packages, including
the use-case ports, without excluding inconvenient classes or exception paths.

| Scope | Covered lines | Missed lines | Line coverage |
| --- | ---: | ---: | ---: |
| Entity | 102 | 0 | 100% |
| Use Case | 337 | 0 | 100% |
| Use Case ports | 6 | 0 | 100% |
| Complete course layers | **445** | **0** | **100%** |

The new tests cover malformed question choices, invalid score bounds, absent or
oversized login passwords, invalid answer-selection forms and immutable quiz
choices. They verify rejected input does not start a session or save partial data.
Additional adapter tests check hidden answers, refreshed personal display data,
provider-result isolation and meaningful error translation.

The Maven `verify` lifecycle now enforces 100% line coverage for all Entity and
Use Case packages and subpackages. The gate passed against the current coverage
data. A negative control against the earlier 99.10% baseline correctly failed,
so the rule is checked as an actual gate rather than merely displayed in a report.

Checkstyle 10.26.1, through Maven Checkstyle 3.6.0, inspected **87 Java files** and
reported **661 findings** with the configured rules. Findings remain visible;
the report goal is non-blocking. The successful Maven build does not claim a
clean style report. Most current findings concern compact one-line statements,
brace layout, line length and wildcard imports.

Generated evidence after `mvn verify`:

- `target/site/jacoco-course-layers/index.html` and `jacoco.xml`: full course layers.
- `target/site/jacoco/index.html`: full application coverage, including UI/adapters.
- `target/site/checkstyle.html` and `target/checkstyle-result.xml`: actual findings.
- `target/surefire-reports/`: test cases and results.
- `target/ui-snapshots/`: offscreen renders of the actual Swing components.

CI uploads these reports as artifacts. New MVP use cases need their own tests and
must preserve the measured core-layer coverage; this snapshot does not cover
UC-09/10, which have not yet been implemented.

Automated dictionary tests use test doubles or a local HTTP server. The live API
diagnostic is a separate observation of service availability at the time of the
check, not a guarantee that the public service will remain available. The backup
provider was covered by automated tests but was not exercised by this successful
primary-provider live request.

The Swing tests operate real components in headless mode. They do not establish
that every native window layout has been manually checked on another computer.
The subsequent [GitHub Actions run for e0214f3](https://github.com/fanfan170721/team-project/actions/runs/44185870260)
passed all four `mvn verify` jobs: Ubuntu and Windows, each with Java 11 and 17.
This establishes automated build/test results on clean hosted runners; a
teammate's local IDE setup and native GUI installation still need their own checks.

The shade plugin reported a shared `META-INF/MANIFEST.MF` resource warning. The
build succeeded, and the packaged entry point and live diagnostic both ran.

## Architecture scope

The implementation has entities, focused use-case input interfaces and
interactors, repository/service ports, infrastructure adapters, eight specific
controller/presenter pairs, plain view models and Swing views. The architecture tests check source
dependency directions; they are not a complete assessment of every design rule.

Interactors currently return results to the controller, which calls the presenter.
There is no separate `OutputBoundary` callback interface for each use case. This
return-value design must be assessed against the course's required architecture;
passing the tests does not establish full course compliance.

## Delivery and attribution

The team-contract PR #1 has been merged. Application review status is recorded in
the repository's pull requests. The original AI-assisted implementation
history is retained; it does not show that multiple teammates independently
implemented these use cases. Any assigned ownership, review and individual
contribution must reflect the actual team's work.

## Earlier observations — 2026-10-22

The earlier record reported 38 passing tests, inspected offscreen component
renders, and successful launcher checks. During that earlier extension, the
primary dictionary service returned HTTP 530 for `climate` and `accommodation`;
packaged-JAR diagnostics then succeeded through Datamuse. Those are historical
observations and were not repeated as part of the 2026-11-17 verification.
