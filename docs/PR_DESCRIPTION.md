# Application delivery PR #2 description

**Historical PR #2 description.** Current delivery is `3f4b4f3` after PR #6,
the full export flow #9 and the full review-plan flow #11 merged. All three MVP
flows are implemented; PR #10's package-verification/handoff update still awaits
peer review. See [HANDOFF.md](HANDOFF.md) for current status. The dated snapshots
below describe earlier revisions and are not current pending-work assignments.

[PR #2](https://github.com/fanfan170721/team-project/pull/2) added a runnable Java 11 /
Swing vocabulary-learning application to the Maven skeleton. This description
reflects the combined delivery at `62adf65`, checked on November 19, 2026. The PR
merged from `Emilia/project-delivery` into `main` on November 20 as `064dc56`.
The merge tree matches reviewed/tested `62adf65`, including PR #3 and PR #4;
`main` now contains the runnable application. Teammates continue using
`Emilia/project-delivery` as their current work base.

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
  tests into delivery (`62adf65`). At that snapshot, the tests used an exporter
  double; the concrete CSV adapter and UI flow were not part of PR #2.
- Xinyu approved PR #2 and PR #3. Later implementation and review evidence is
  recorded in the progress update below.

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

## Subsequent progress on November 20

Delivery is now at `cebde6d`, beyond the baseline on `main`:

- Fuxuan merged maintenance [PR #5](https://github.com/fanfan170721/team-project/pull/5)
  as `ba26bfb`, then left an actual review comment. This was not an Approve review
  event before the merge.
- Xinyu's [PR #7](https://github.com/fanfan170721/team-project/pull/7) merged the
  UC-10 core and 13 tests as `e93804d` after review.
- Fuxuan's [PR #8](https://github.com/fanfan170721/team-project/pull/8) merged the
  CSV adapter and 11 tests as `cebde6d` after review. No CSV handoff to Allen is needed.
- [PR #6](https://github.com/fanfan170721/team-project/pull/6) now targets delivery.
  Its updated working tree includes those increments, demo-data protection and
  typed-topic/review-time follow-up fixes. Local verification passed **100 tests**,
  **633/633 core lines covered**, and an offline packaged-JAR check. Checkstyle
  retains **588 findings**; the project has **113 Java source files**. These local
  results do not replace current remote checks or the outstanding peer approval.

All members now have substantial merged code, real peer-review records and
runnable progress. UC-09 and UC-10 still need controller/presenter/UI and wiring;
their complete flows remain committed MVP work. Each member's own TA demonstration
and explanation have not been verified. Final integration, final checks,
presentation and individual submissions remain incomplete. See
`DELIVERY_CHECKLIST.md` and `VERIFICATION.md` for the remaining work and evidence.
