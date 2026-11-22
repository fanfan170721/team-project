# WordTrail presentation outline and rehearsal checklist

This is a proposed speaking plan, not a slide deck or a record of a rehearsal.
It follows the [course presentation page](https://q.utoronto.ca/courses/444184/pages/final-project-presentation-expectations),
checked on November 21, 2026. That page still links the November 15 **draft**
rubric and suggests about 15 minutes plus up to 5 minutes of questions.
Every member speaks. Recheck the final schedule and rubric before presenting.

## Fifteen-minute run of show

Use brief diagrams and observed results on slides; keep code and detailed notes
available for questions. The estimates below total 15 minutes and include
speaker handoffs. Measure actual timing in rehearsal and shorten if necessary.

| Time | Proposed speaker | Content and concrete evidence |
| --- | --- | --- |
| 0:00–1:00 | Allen | Problem, intended learner and three MVP stories: private vocabulary, CSV quiz-history export and a daily review plan. Name the owners and boundaries; refer to [USE_CASES](USE_CASES.md). |
| 1:00–1:30 | Allen | API: show the dictionary lookup's actual definition and provider URL. Explain primary/backup gateways and the explicit error if both fail. Have the request prepared; never present a stored definition as a successful live call. |
| 1:30–2:00 | Fuxuan | Persistence: accounts and account-specific progress use the real JSON store; CSV is a separate exported artifact. Point to the data directory outside `target` and explain reopen verification. |
| 2:00–3:30 | Xinyu | Whole-system architecture: Swing -> adapters -> use cases/entities; repositories and gateways invert dependencies. Application wires the concrete implementations. Show the [architecture](ARCHITECTURE.md), not a list of every class. |
| 3:30–5:00 | Allen | Walk one UC-08 save through input DTO, interactor, output boundary, presenter, immutable result and the two background-worker stages. Use the [UC-08 diagrams](ALLEN_UC08_WALKTHROUGH.md). |
| 5:00–6:00 | Fuxuan | SOLID example: export policy depends on a `QuizResultsExporter` port; the CSV adapter owns escaping and file replacement. Explain failed-export preservation and the relevant tests. |
| 6:00–7:00 | Xinyu | SOLID example: review-plan policy depends on repository ports and an injected clock. Explain fixed-date boundary tests without changing global time; distinguish planning from recording a review. |
| 7:00–8:00 | Allen | Implemented pattern and extension: dictionary strategies behind `DictionaryGateway`, composed fallback and swapping a provider at composition. Explain the current two-provider limit and a possible extension; do not claim an unimplemented provider/cache exists. |
| 8:00–10:00 | Allen | UC-08 live demo: invalid field retained, successful add/edit, another account cannot see the word, reopen preserves the edit. Use the action portion of the walkthrough, condensed after a timed rehearsal. |
| 10:00–12:00 | Fuxuan | UC-09 live demo: export two rows to a new CSV, show content/destination/count, then cancel another export; switch to the empty-history guest if time allows. |
| 12:00–14:00 | Xinyu | UC-10 live demo: Demo topic has one eligible word and one excluded until tomorrow; invalid limit clears stale output; regenerate successfully. Explain unchanged learning progress. |
| 14:00–15:00 | Allen | Quality and scope: summarize current test/coverage evidence, visible Checkstyle findings, one limitation and the next sensible extension. End on time and leave source/tests ready for questions. |

The separate 3–4 minute UC-08 guide combines explanation and demonstration for
individual practice. Do not deliver that whole script again during the two-minute
team demo slot; its architecture and design material already appears earlier.

## Check every rubric area against something visible

These weights are from the linked draft, not a prediction of the team's grade.
Implementation, diagrams, automated tests, actual delivery and answers are
different kinds of evidence.

| Draft category | Weight | Prepare/show |
| --- | ---: | --- |
| Presentation materials | 15 | Readable, proofread visuals; speaker labels; a dependency diagram and one use-case sequence diagram; current screenshots with captions. |
| Delivery | 15 | Every member speaks; time a complete practice; concise handoffs and a plan for API/UI delays. |
| Demo | 10 | Actual integrated UC-08/09/10 behavior, including failures and persistence; use fresh synthetic data. |
| Questions | 5 | Each member can explain their flow and tradeoffs from the actual source; use the UC-08 Q&A as Allen's practice. |
| Specification | 10 | User stories, inputs, outcomes and validation are consistent with implemented behavior and the README/MVP. |
| Scope | 5 | Explain why these three flows form a usable MVP; separate implemented features from later ideas. |
| API | 5 | A real provider request/result/source, the gateway boundary, failure handling and test doubles distinguished from live evidence. |
| Persistence | 5 | Account-specific saved data survives reopening; CSV export is real; explain the JSON store's limits. |
| Clean Architecture | 10 | Correct dependency directions, composition root and one concrete request/response path; acknowledge hybrid read flows. |
| Overall design | 10 | Two source-based SOLID examples, an implemented pattern beyond merely naming the starter's controller/presenter chain, and a realistic extension seam. |
| Code quality | 10 | Tests, core coverage and Checkstyle reports with honest limitations; readable code and error handling, not just a green badge. |

See [COURSE_REQUIREMENTS](COURSE_REQUIREMENTS.md) and the official
[draft rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1).
The current package contains all three flows on `Emilia/project-delivery`.
Verify the current published commit and CI before selecting presentation evidence;
an older `main` checkout may not contain the integrated version.

## Rehearse using observed results

1. Build and follow [DEMO_PREPARATION](DEMO_PREPARATION.md) on the presentation
   computer. Keep the fresh profile, exported fixtures and built JAR outside any
   cleanup operation. Prepare an unused CSV destination and the source diagrams.
2. Each speaker practices their own flow and explains one error path. Record
   what actually happened, any problem and elapsed time; do not mark a native UI
   or live API check as performed merely because a headless test passed.
3. Run all speakers in order with a timer. Include login, reopen, file selection
   and speaker handoffs. Shorten narration before dropping the important behavior.
4. Practice questions: why this boundary, what happens when storage/API fails,
   why the second account is isolated, how due dates are calculated, and what
   would change with another storage/provider implementation.
5. Before the final run, recheck course timing, rebuild the reviewed revision,
   prepare another new profile for that day's date, and confirm everyone can
   open their own source/test evidence. Individual reflections and peer evaluation
   are separate deliverables based on each person's actual work.

No rehearsal, TA demonstration, slide submission or individual reflection is
claimed complete by this outline.
