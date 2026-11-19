# Application delivery PR #2 description

[PR #2](https://github.com/fanfan170721/team-project/pull/2) adds a runnable Java 11 /
Swing vocabulary-learning application to the Maven skeleton. This description
reflects the combined delivery at `62adf65`, checked on November 19, 2026. The PR
remains a draft from `Emilia/project-delivery` to `main`; `main` at `dbde42d`
contains the template and signed contract, not the application.

Learners can register and sign in, browse and search vocabulary, study flashcards,
take four-choice quizzes, save review words and inspect progress. A first-run
administrator can add vocabulary and quiz questions. Account-specific progress
persists in local JSON snapshots.

Personal vocabulary and definition overrides belong to the signed-in account.
Flashcards and new generated quizzes use that account's content, while an active
quiz retains its starting snapshot. Review counts track completed card visits;
daily study totals count distinct words. Vocabulary tables support review-count
and recent-review ordering. Live lookup uses Free Dictionary API with Datamuse
as a fallback, retains the responding provider's source and keeps online results
separate from saved definitions.

Entities and use cases are separated from Swing, HTTP and JSON adapters. The
composition root wires interfaces, interactors, repository/service ports and
specific controllers/presenters. Salted PBKDF2 hashes protect stored passwords,
admin mutations check the role in the use case, and account changes invalidate
active decks and quizzes. Allen's UC-08 edit flow now uses immutable input DTOs,
an explicit output boundary and a presenter for saved success or expected failure.
Other baseline flows return results to a controller that invokes the presenter;
do not describe every use case as using an output callback.

The delivery includes formal specifications, MVP ownership, architecture and
quality evidence. Original implementation history remains attributable to Allen
with AI assistance. Later contributions are recorded separately:

- PR #1 merged the contract signed by all three members into `main`.
- [PR #3](https://github.com/fanfan170721/team-project/pull/3) merged Allen's complete
  UC-08 personal-vocabulary flow into delivery (`d0f9f7f`).
- [PR #4](https://github.com/fanfan170721/team-project/pull/4) merged Fuxuan's UC-09
  interactor, input/output boundaries, row/response DTOs, exporter port and eight
  tests into delivery (`62adf65`). Tests use an exporter double; no concrete CSV
  adapter, controller, presenter or export UI is implemented yet.
- Xinyu approved PR #2 and PR #3. No UC-10 implementation branch or PR was observed
  at the latest check. Fuxuan's code peer review remains outstanding.

Validation on November 19 against the combined application code from `62adf65`:

- A clean verification passed **66 JUnit tests**, with zero failures, errors or
  skips, including business rules, account isolation,
  persistence, architecture, presenters, local HTTP/dictionary doubles and the
  headless Swing workflow. The UC-09 tests verify core behavior, not CSV files/UI.
- JaCoCo measured **535/535 executable Entity/Use Case lines covered (100%)**.
  This is line coverage, not a claim of complete branch coverage or course compliance.
- GitHub Actions at `62adf65` passed all four Windows/Linux and Java 11/17
  combinations. Newly added CI checks still require a subsequent remote run.
- Checkstyle at that base checked **101 Java files** and retained **677 findings** in its
  non-blocking report. A passing build does not mean the code has no style warnings.
- The offline packaged-JAR check passed application wiring, bundled words/questions,
  the JSON dependency and account/personal-word persistence after reopening.
  It uses no UI or network and is separate from the headless Swing workflow.
- The packaged application's live `--check-api hello` diagnostic succeeded against
  the primary provider on November 17. Automated tests do not require public API access.

The complete UC-09 CSV flow and UC-10 daily review plan remain committed MVP work.

The separate shared-maintenance branch adds launcher/build checks and one
startup-failure subprocess regression: 67 JUnit tests, 535/535 core lines and
102 Java files with 676 Checkstyle findings. These changes are not yet merged into
PR #2's delivery branch; inspect their own PR and remote checks before integration.

A proposed handoff of the remaining CSV work to Allen awaits Fuxuan's reply; no
ownership transfer is claimed. Each member still needs a complete personal use
case and genuine review/contribution evidence. Final integration, final checks,
presentation and individual submissions are not complete. See
`DELIVERY_CHECKLIST.md` and `VERIFICATION.md` for the remaining work and evidence.
