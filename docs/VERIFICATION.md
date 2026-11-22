# Verification record

## Publication candidate and approved package fix — 2026-11-22

This dated snapshot covers `Emilia/publish-reviewed-mvp`, based on reviewed
delivery `924c56f`. Its application source, build configuration, launchers and
tools match that delivery revision; the publication increment updates status
documentation. At this snapshot, `main` is still `064dc56` and the publication
PR has not merged.

- [Xinyu approved `87d903e`](https://github.com/fanfan170721/team-project/pull/10#pullrequestreview-6009455370)
  after rechecking both earlier findings. Its eight push/PR checks passed;
  [PR #10](https://github.com/fanfan170721/team-project/pull/10) merged into
  delivery as `924c56f` on November 22. This supersedes the pending-review
  status in the historical entries below.
- Fresh local Maven `verify` finished at **13:15 EST** on Windows/JDK 11:
  **147 tests**, zero failures, errors or skips, with the executable JAR built.
  Entity/Use Case line coverage remains **633/633 (100%)**. Checkstyle reports
  **623 findings** with its existing scope and non-blocking behavior unchanged.
- All **31 package-check process assertions passed** against that fresh JAR,
  including the real UC-08/09/10 probe, reopening/isolation, a compiler failure
  before `main`, concurrent invocations, recovery and artifact mutation checks.
  The resulting JAR SHA-256 is
  `15d93eb781695deec9d992347a650b70aefe0a00e5952cb4edd581ef59ef62f1`.
- [PR #12](https://github.com/fanfan170721/team-project/pull/12) is a separate
  pending increment for synthetic demo preparation and presentation guides.
  It is not part of this publication candidate. After incorporating delivery at
  `0a7a20a`, its 31 package and 18 demo-tool assertions passed locally.
- Publication needs the current candidate's CI and peer review. These checks
  used synthetic data without native windows, sound or public API requests;
  they do not establish a timed rehearsal or individual TA demonstration.

## Review follow-up — concurrent package evidence, 2026-11-21

[Xinyu's review of `6bb9926`](https://github.com/fanfan170721/team-project/pull/10#pullrequestreview-6009182724)
confirmed the 147-test build and 23 existing package regressions, then reproduced
a false PASS when two wrappers shared an output directory. This was feedback
requiring a fix, not an approval to merge.

- A marker-coordinated regression reproduced the problem locally: the silent
  probe exited zero and its manifest accepted a concurrent valid probe's PASS.
- The wrapper now acquires an exclusive file lock before changing any evidence,
  keeps it through success/failure reporting, and rejects an overlapping check
  without modifying the active invocation's evidence.
- All **31 package-check process assertions passed** after the fix, including
  unchanged active evidence, rejection of the silent probe and successful reuse
  of the output directory after lock release. No application source changed.
- The earlier published revision's four matrix jobs passed in
  [CI run 44944509157](https://github.com/fanfan170721/team-project/actions/runs/44944509157).
  That run predates this locking change. Consult the current revision's
  [PR #10 checks](https://github.com/fanfan170721/team-project/pull/10/checks)
  for the fix's CI result; merge only after successful current checks and
  Xinyu's follow-up review.

## Latest verification — integrated MVP and packaged flows, 2026-11-21

Branch: `Emilia/package-verification-evidence` (PR #10). The measurements below
are the **3:00 p.m. local working-tree snapshot**, subsequently published as
`6bb9926`. They are not clean-commit provenance. Publication, CI and review
updates are recorded separately above.

- Full Maven verification finished at **3:00 p.m. EST**: **147 tests**, zero
  failures, errors or skips; executable JAR built.
- Entity/Use Case line coverage: **633/633 (100%)**, with the existing scope and
  thresholds unchanged. Checkstyle retained **623 findings** across **130 Java
  source files**; the report remains visible and non-blocking.
- The offline packaged-JAR probe **passed** personal-vocabulary editing and
  persistence (UC-08), account-isolated quiz-results CSV export (UC-09), review
  planning without progress mutation (UC-10), and reopening the stored profile.
  It used synthetic isolated data and no native window, audio or public API.
- **23 process-level package-check regression assertions passed**, separately
  from the JUnit total. The wrapper invalidates old success reports, rejects
  broken/missing/changed JARs or a child without a result, and records hash/outcome
  metadata. See [PACKAGE_VERIFICATION.md](PACKAGE_VERIFICATION.md).
- PR #9's renewed integration review covered `69b9ee9` (135 tests and eight
  successful online checks); it merged as `f968b44`. PR #11's `5a512ae`
  integration passed 147 tests and eight online checks before merging as
  `3f4b4f3`. All three complete MVP flows are now on delivery, while `main`
  remains the earlier `064dc56` baseline.
- These local results do not establish personal TA demonstrations, rehearsal,
  presentation, reflections or peer evaluations. See the follow-up above for
  the published revision's CI and review status.

The dated sections below are historical measurements of their named revisions.

## Latest verification — teammate integration and review follow-up, 2026-11-20

Branch: `Emilia/m2-demo-safety`, including delivery merge `cebde6d` and review
follow-up `80f0edd`. PR #6 now targets `Emilia/project-delivery` directly.

- `mvn -B --no-transfer-progress verify` finished successfully at **8:13 p.m.
  EST**: **100 tests**, zero failures, errors or skips. The executable JAR was
  built. Full Entity/Use Case line coverage is **633/633 (100%)**: Entity 103,
  Use Case 524 and Use Case ports 6. No classes or thresholds were excluded.
- Checkstyle reports **588 findings**; the project contains **113 Java source
  files**. The report remains visible and non-blocking. A passing build does
  not mean the style findings are resolved.
- The packaged offline check passed: application wiring, bundled seed data,
  JSON dependency and account/personal-word persistence across reopening. Its
  isolated profile was removed; no network request, native window or sound was
  used.
- PR #7's UC-10 core was reviewed and tested against the merged maintenance
  baseline: **87 tests**, **633/633 core lines**. PR #8's CSV adapter was checked
  independently and together with PR #7: the combined tree passed **98 tests**
  before this branch's two new review-time tests. Both PRs had eight successful
  checks, received Allen's approval and were merged into delivery as `e93804d`
  and `cebde6d`, respectively.
- The two new tests verify that review-time equality and hashing agree with
  chronological ordering, including repeated daylight-saving local times and
  never-reviewed values. Topic selection now uses a typed `TopicOption` combo
  box. Five changed documentation screenshots were refreshed from the passing
  headless Swing workflow output; these are component renders, not a native
  desktop rehearsal.
- PR #5 was merged as `ba26bfb`. Fuxuan's substantive review was posted as a
  comment **after** that merge; it was not a pre-merge approval event. The
  review-time equality, typed selector and screenshot updates address that
  feedback. PR #6 still requires another member's review before merging.

The launcher protection is unchanged from the earlier **152-assertion** run
below; that is previous evidence, not a new run in this integration check.
Likewise the latest live API observation remains the **5:12 p.m. EST** result
below. UC-09 still needs its Controller/Presenter/ViewModel, UI and application
wiring; UC-10 still needs those adapters and its UI/wiring. The tests do not
establish either complete UI flow or each member's own TA demonstration.
The published `main` remains the runnable baseline at `064dc56`.

## Earlier verification — M2 launcher preparation, 2026-11-20

Branch: `Emilia/m2-demo-safety`, based on the unchanged PR #5 head `f6a4ee2`.
The Java source is unchanged; this increment protects demo profiles in the
Windows launcher and corrects demonstration instructions.

- `mvn -B --no-transfer-progress verify`: **74 tests**, zero failures, errors or
  skips. Full Entity/Use Case line coverage remains **574/574 (100%)**; Checkstyle
  reports **586 findings across 104 Java files**. The executable JAR was built.
- The packaged offline check passed again: application wiring, bundled seed
  content, JSON dependency and account/personal-word persistence across reopening.
  Its temporary profile was removed, and no personal profile was accessed.
- The packaged `--check-api hello` diagnostic succeeded at approximately
  **5:12 p.m. EST**, returning the primary provider's definition and source
  `https://api.dictionaryapi.dev/api/v2/entries/en/hello`. No native window or
  sound was used. This observes service availability at that time only.
- The launcher harness passed **152 assertions** on Windows PowerShell 5.1 and
  PowerShell 7. Tests verify that existing profiles under `target/` survive both
  automatic and forced rebuild attempts unchanged; neither fake Maven nor fake
  Java runs after the guard stops execution. Explicit unsafe data paths,
  normalization, the `target-other` boundary, safe paths with spaces and `-Check`
  behavior are covered. These are script assertions, not extra JUnit cases.
- Persistent demo examples now use `.wordtrail/demo`. The launcher guard does
  not protect direct `mvn clean` or direct JAR commands. Documentation explains
  how to back up and relocate an old profile before rebuilding; no existing
  profile was moved or deleted by this change.

PR #5 at `f6a4ee2` passed eight checks across the
[push](https://github.com/fanfan170721/team-project/actions/runs/44643837835) and
[PR](https://github.com/fanfan170721/team-project/actions/runs/44643846486) runs,
covering Windows/Linux and Java 11/17. It still awaits a submitted peer review
at this snapshot. Consult the follow-up PR's own checks before merging this
launcher change. These checks do not establish completed teammate work or a
native GUI rehearsal.

PR #2 was also merged on November 20 as `064dc56` after checking its actual
review history and eight successful checks at `62adf65`. Xinyu's original
baseline approval is followed only by the reviewed PR #3 and PR #4 increments.
The PR #3 merge tree equals its approved head, and PR #4 adds exactly its
reviewed patch without extra merge-resolution changes. The resulting `main`
tree equals `62adf65`; no PR #5/#6 changes were included in that merge.

## Previous verification — shared maintenance, 2026-11-19

Base: `62adf65`, containing Allen's merged UC-08 (PR #3) and Fuxuan's merged
UC-09 core (PR #4). Shared build/documentation work is on
`Emilia/shared-project-maintenance`. Environment: Windows 11, Temurin JDK
11.0.32.1 and Maven 3.9.16.

- `mvn -B --no-transfer-progress verify`: **74 tests**, zero failures,
  errors or skips. The executable JAR was rebuilt. The preceding clean build
  passed all 72 then-existing tests but exposed two uncovered core statements;
  the final run includes behavior tests for both paths.
- Complete Entity/Use Case coverage: **574/574 lines (100%)**; Entity 103,
  Use Case 465, and Use Case ports 6. No class exclusions or threshold reductions.
- Checkstyle: **104 Java files, 586 findings**, down from 676, non-blocking and
  unsuppressed. Fifteen existing core files were reformatted without changing
  Java tokens. Expanding compact statements exposed missing tests for oversized
  examples and insufficient distinct quiz definitions; both now verify rejection
  without persisting invalid content or quiz results.
- Three topic-selector tests cover an actual topic named `All topics`, selection
  preservation and fallback after a topic is removed. Two real-table tests cover
  chronological review sorting during Toronto's repeated daylight-saving hour in
  both Vocabulary and Review later, including equal timestamps and never-reviewed
  words. Display text remains local time; ordering uses the original instant.
- The packaged `--help` entry point passed without a window or network request.
  Repeating `run.ps1 -Check` with unchanged inputs and JAR skipped rebuilding.
- A new subprocess regression confirms initialization failures terminate with
  exit code 1 even when the error dialog itself cannot be displayed. The child
  JVM is explicitly headless, so this does not test a native error window.
- `tools/VerifyPackage.java`, run in Java source-file mode with only the packaged
  JAR on its classpath, passed application wiring, seed vocabulary/questions,
  JSON dependency, account and personal-word save/reopen checks. It uses a fresh
  temporary profile and a dictionary fake that fails on any attempted lookup.
  The profile is removed after the check; `target/package-check/result.txt`
  retains the result. Personal learning data is never read or changed.
- `tools/Test-Launcher.ps1` passed **87 assertions** on Windows PowerShell 5.1
  and PowerShell 7.6.9. These are launcher regression assertions, not additional
  JUnit test cases. Isolated fake Maven/Java commands test input additions,
  edits/deletions, unchanged-input skipping, missing output, forced clean builds,
  build failures, input changes during build, and paths containing spaces. The
  stamp binds both input contents and the actual JAR hash, so an external build
  cannot leave a different executable marked as matching restored source files.
- Swing screenshots are now placed under the selected Maven build directory,
  rather than always writing to the default `target/ui-snapshots` directory.
  A separate five-test Swing run with `wordtrail.build.directory=target/isolated-check`
  passed and left the default screenshots' contents and modification times unchanged.
- Package-check negative controls removed `seed.json` and then `org/json/` from
  separate copies of the JAR. Each was correctly rejected with exit status 1 for
  the missing resource/dependency, demonstrating that the check exercises the package.
- No native GUI or sound playback was used. The live API was not repeated for
  this maintenance change; automated HTTP tests use local servers/test doubles.

The [delivery push run](https://github.com/fanfan170721/team-project/actions/runs/44620314535)
and [delivery PR run](https://github.com/fanfan170721/team-project/actions/runs/44620328913)
passed Windows/Linux on Java 11/17 at `62adf65`. This maintenance adds offline
packaged-application checks to all four jobs and launcher regression checks to
Windows jobs, with a hard failure if the executable JAR upload is missing.
The [maintenance push run](https://github.com/fanfan170721/team-project/actions/runs/44640187073)
and [maintenance PR run](https://github.com/fanfan170721/team-project/actions/runs/44641001407)
passed all four combinations at `8cee42f`, including the new checks. Consult
[PR #5](https://github.com/fanfan170721/team-project/pull/5) for remote verification
of the subsequent topic, sorting and test changes.

At that November 19 snapshot, the merged UC-09 increment had no CSV file
adapter, Controller/Presenter or UI, and no UC-10 implementation PR had been
observed. The latest integration section above supersedes that implementation
status. Existing tests and package checks do not establish native GUI
verification or full course compliance.

## Historical UC-08 follow-up, 2026-11-17

The following UC-08 details describe that earlier snapshot; their 58-test and
487-line counts are historical. The November 19 totals above supersede them.

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
the repository's pull requests. The original implementation
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
