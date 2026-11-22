# Presentation and demonstration guide

Target the course's current roughly 15-minute presentation plus up to 5 minutes
of questions. Every member must speak. Label each slide with its speaker and
check the eventual schedule; the November 15 rubric is still labelled a draft.
The official slide template is guidance, not a requirement to present all 16 pages.

## Prepare a reproducible demonstration

For a prepared synthetic profile, use [Demo preparation](DEMO_PREPARATION.md).
It creates accounts and UC-08/09/10 fixtures without opening the UI or replacing
existing data. See also [Allen's UC-08 walkthrough](ALLEN_UC08_WALKTHROUGH.md)
and the [15-minute presentation outline](PRESENTATION_OUTLINE.md).

Keep reusable demo data in `.wordtrail/demo`, outside the build directory.
`mvn clean` deletes `target`, including any old `target/presentation-data`.
If that old directory contains a demo you want to keep, close the application
and manually back up the entire directory outside `target`; verify the backup,
then manually move the original profile directory to an unused `.wordtrail/demo`
before rebuilding. If that destination exists, choose another unused directory
outside `target` and use it with `--data-dir`; do not overwrite an existing profile.
The launcher blocks rebuilding while `wordtrail.json` remains under `target`,
so making a backup alone does not clear the guard. No automatic data move is performed.

```sh
mvn verify
java -jar target/wordtrail.jar --data-dir .wordtrail/demo
```

Use a new demo directory when a fresh dataset is needed. Choose non-personal demo
credentials, create the demo accounts before presenting, and prepare a few saved
words and completed quizzes. Do not spend the presentation waiting for account
setup or dependency downloads. A recording is allowed, but keep a working program
ready for immediate live follow-up questions.

## November 20: M2 TA progress demonstration

The [course timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2)
requires each member to have a substantial submitted PR, a review of another
member's PR, and runnable code related to their assigned use case that they can
demonstrate and discuss. The team needs a real API call and at least one complete
end-to-end use case. All MVP use cases implemented and integrated is the
**November 27 M3** target; M2 does not require every member's full UI flow to be
finished. M1 nevertheless includes initial UI and interactor development.

Use these focused rehearsals with dependencies installed and demo data prepared.
The preparation durations below are team estimates, not course-imposed coding limits:

| Member / estimated preparation | Demonstration and current evidence |
| --- | --- |
| Allen / 20 minutes | Run UC-08: add a personal word, edit its definition, check account isolation, close and reopen `.wordtrail/demo` to show persistence. Perform a live lookup and show its provider URL. [PR #3](https://github.com/fanfan170721/team-project/pull/3) contains the merged flow and Xinyu's approval; Allen's review is on [PR #4](https://github.com/fanfan170721/team-project/pull/4). |
| Fuxuan / 15 minutes | Run the eight `ExportResultsInteractorTest` cases and 11 `CsvQuizResultsExporterTest` cases. Explain account isolation, chronological rows, CSV escaping and a failed atomic replacement that preserves an existing file. [PR #4](https://github.com/fanfan170721/team-project/pull/4) and [PR #8](https://github.com/fanfan170721/team-project/pull/8) contain this merged progress; the export UI is still unfinished. |
| Xinyu / 20 minutes | Run the 13 `ReviewPlanInteractorTest` cases merged through [PR #7](https://github.com/fanfan170721/team-project/pull/7), then explain a fixed-date due/not-due example and why planning leaves study counters unchanged. Her approvals on PR #2/#3 provide peer-review evidence; the review-plan UI is still unfinished. |

From the project root, Fuxuan can run the core and CSV tests:

```sh
mvn "-Dtest=ExportResultsInteractorTest,CsvQuizResultsExporterTest" test
```

Xinyu can run her core tests:

```sh
mvn "-Dtest=ReviewPlanInteractorTest" test
```

These focused test runs are not the full verification/coverage check. Use `mvn verify`
for that. Allen can also show the real API diagnostic without opening another UI:

```sh
java -jar target/wordtrail.jar --check-api hello
```

The primary-provider diagnostic succeeded on November 20 at 17:12 EST.
Report the actual provider/result; if lookup fails, keep that failure visible and
do not claim a successful live call. Tests are useful runnable evidence when the
member can explain the behavior; a green test count alone does not establish TA acceptance.
Fuxuan left an actual review comment on [PR #5](https://github.com/fanfan170721/team-project/pull/5)
after merging it; this was not an Approve review event before the merge. No CSV
handoff is needed. Both use cases still need controller/presenter/UI and wiring.
Each member's own TA demonstration and explanation remain unverified.
If preparation takes longer, report the real stopping point and continue as needed.
These estimates do not guarantee completion; the presentation timing below is a
separate constraint from development or preparation time.

## Suggested 15-minute order

| Time | Content | Evidence |
| --- | --- | --- |
| 1:00 | MVP/user stories | Explain the learner's goals without implementation jargon; identify the three leads |
| 0:30 | External API | Name the actual dictionary provider and show a successful attributed lookup |
| 0:30 | Persistence | Identify saved data; demonstrate save/reopen/load during the demo |
| 6:00 | Design | Architecture diagram; one use-case walkthrough; two concrete SOLID examples; an added design pattern and extensibility |
| 6:00 | Functional demonstration | Show each member's implemented MVP use case and the supporting learning loop |
| 1:00 | Code quality | Actual PR review, tests, complete core-layer coverage report, Checkstyle findings and how they are handled |

This is an example schedule. Rehearse it and redistribute time as needed while
keeping all required evidence and all speakers. Avoid reading paragraphs from slides.

## Existing baseline demonstration

1. As a learner, add a personal word and edit its definition. Show that another
   learner does not see this private entry. Explain the difference between the
   shared word bank and a personal definition.
2. Filter vocabulary and perform a live dictionary lookup. Point out the provider
   URL and the separate saved meaning. If the service fails, explain the explicit
   error and continue with offline learning; never label local content as a live result.
3. Save a word, open a review-only flashcard deck, reveal its meaning and complete
   a review. Revisit it and complete another review; contrast total reviews with
   daily distinct-word counts.
4. Complete a short quiz and show the saved result. Close and reopen using the
   same data directory, then sign in and show that the result and review list remain.

## Pending MVP demonstrations

These complete UI flows are **not available yet**, although UC-09's core/CSV
adapter and UC-10's core are merged. Their leads must complete and test the
controller/presenter/UI and wiring before the final integrated demonstration.

- **Fuxuan / UC-09:** export the account's quiz results; inspect the CSV and result
  count. Demonstrate cancellation or overwrite refusal, plus an empty history.
- **Xinyu / UC-10:** generate today's due-review list; explain the local-date interval
  rule and show that generating a plan does not increase the study count.

**Zidong / UC-08 is already implemented:** explain the personal vocabulary path,
failed-save input retention and privacy behavior using the existing demonstration.

Assign the actual speakers and rehearse together once these features exist. A
written script does not establish that rehearsal or individual preparation happened.

## Design evidence to prepare

- Show an accurate overall architecture diagram with dependency direction.
- Walk through one member's use case with before/after views, relevant UML classes,
  its Interactor and the flow through the specific Controller/Presenter.
- Explain at least two SOLID principles using actual classes, rather than definitions alone.
- Discuss an implemented pattern beyond the starter, such as dictionary adapter
  composition or injected repository/service strategies, and a concrete extension.
- Explain the implemented hybrid response flow: UC-08 and the UC-09/UC-10 cores use output
  boundaries; the other existing use cases return results through controllers to
  presenters. Show how source dependencies point inward and discuss the tradeoffs.

## Questions to practise

- Why JSON? It is sufficient for a classroom desktop app; whole-file rewriting
  will not scale like a transactional database. A new adapter can support SQLite.
- What survives restart and what is private? Demonstrate the actual account data.
- What happens when saving fails or the API is unavailable? Show preserved state
  and meaningful failure feedback, backed by tests.
- Why do study totals and review counts differ? One counts distinct words per day;
  the other counts intentionally completed card visits.
- What did each member actually implement and review? Point to their real PRs.

The team still needs its presentation material, real rehearsal and individual
reflections. Keep the evidence current after the remaining MVP integrations.
