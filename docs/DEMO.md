# Presentation and demonstration guide

Target the course's current roughly 15-minute presentation plus up to 5 minutes
of questions. Every member must speak. Label each slide with its speaker and
check the eventual schedule; the November 15 rubric is still labelled a draft.
The official slide template is guidance, not a requirement to present all 16 pages.

## Prepare a reproducible demonstration

```sh
mvn verify
java -jar target/wordtrail.jar --data-dir target/presentation-data
```

Use a new demo directory when a fresh dataset is needed. Choose non-personal demo
credentials, create the demo accounts before presenting, and prepare a few saved
words and completed quizzes. Do not spend the presentation waiting for account
setup or dependency downloads. A recording is allowed, but keep a working program
ready for immediate live follow-up questions.

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

These steps are **not available in the baseline yet**. Their designated leads
must implement, test and explain them before the final integrated demonstration.

- **Fuxuan / UC-09:** export the account's quiz results; inspect the CSV and result
  count. Demonstrate cancellation or overwrite refusal, plus an empty history.
- **Xinyu / UC-10:** generate today's due-review list; explain the local-date interval
  rule and show that generating a plan does not increase the study count.
- **Zidong / UC-08:** explain the personal vocabulary path and privacy behavior.

Assign the actual speakers and rehearse together once these features exist. A
written script does not establish that rehearsal or individual preparation happened.

## Design evidence to prepare

- Show an accurate overall architecture diagram with dependency direction.
- Walk through one member's use case with before/after views, relevant UML classes,
  its Interactor and the flow through the specific Controller/Presenter.
- Explain at least two SOLID principles using actual classes, rather than definitions alone.
- Discuss an implemented pattern beyond the starter, such as dictionary adapter
  composition or injected repository/service strategies, and a concrete extension.
- Be candid that existing interactors use return-value responses; show how source
  dependencies still point inward. Discuss remaining tradeoffs instead of claiming
  a particular class name proves Clean Architecture.

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