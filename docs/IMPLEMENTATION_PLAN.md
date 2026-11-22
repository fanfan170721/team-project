# WordTrail implementation plan

The Java/Swing baseline implements eight stories; Fuxuan's export flow and
Xinyu's review-plan flow complete the ten-story integrated application. The signed
contract is integrated from `main`. The current delivery work aligns the code,
documentation and validation with the course requirements verified on November 17.

## Publication preparation snapshot on November 22, 2026

- The signed contract is merged through PR #1. Application delivery PR #2 merged
  into `main` as `064dc56`, with the same tree as reviewed/tested `62adf65`.
  `main` now contains the runnable baseline, including PR #3 and PR #4.
  Teammates continue using `Emilia/project-delivery` as their current work base.
- Fuxuan merged maintenance PR #5 as `ba26bfb`, then left an actual review comment;
  there was no Approve review event before that merge. He later formally approved
  PR #6 at `c1bfcae`, which merged as `0ed4c82`. These changes are not included in `main`.
- Formal specifications, feature controllers/presenters, coverage measurement
  and reproducible quality reports are present in `Emilia/project-delivery`.
- Allen's complete UC-08 flow is merged through PR #3 (`d0f9f7f`).
- Fuxuan's UC-09 core and eight interactor tests are merged through PR #4
  (`62adf65`); his CSV adapter and 11 tests are merged through PR #8 (`cebde6d`).
  Xinyu's UC-10 core and 13 tests are merged through PR #7 (`e93804d`).
  PR #9 completes UC-09's adapters/UI/wiring and acceptance tests, and merged as
  `f968b44` after renewed review and eight successful checks. PR #11 completes
  UC-10's adapters/UI/wiring and acceptance tests; its combined integration
  received renewed review, passed eight online checks and merged as `3f4b4f3`.
- The PR #10 working tree included both full flows and passed 147 tests on
  November 21 at 15:00 EST, with 633/633 Entity/Use Case lines covered and the
  executable JAR built. Checkstyle retains 623 findings across 130 Java source
  files. The package probe and 23 regression assertions passed. The probe
  exercises all three MVP flows; its wrapper records current outcome and JAR
  hash evidence. Xinyu subsequently found a concurrent-report bug; the fix at
  `87d903e` passed 31 package assertions, 147 tests and eight online checks.
  Her follow-up approval preceded PR #10's merge as `924c56f` on November 22.
- The publication candidate `Emilia/publish-reviewed-mvp` has the same application
  source as delivery `924c56f` and updates status documentation. Its fresh local
  verification passed 147 tests, 633/633 core lines and 31 package assertions.
  Publication to `main` still requires current CI and peer review at this snapshot.
- PR #12 separately proposes demo-data tools and presentation guides; it is
  pending teammate review and is not included in the publication candidate.
- All three members have substantial merged contributions and actual peer-review
  records. Their personal TA demonstrations and explanations remain unverified.

## Remaining increments

1. Allen maintains shared documentation, build/test evidence and integration.
   Keep measured Checkstyle findings visible; a passing build is not style clearance.
2. Continue from delivery `924c56f`, which contains all three complete MVP flows.
   Do not repeat completed implementations.
3. Review the publication candidate and PR #12's separate demo-preparation
   increment; Allen addresses concrete feedback and integrates after review.
4. Each member prepares to demonstrate and explain their full use case using
   the integrated build. Automated tests do not establish a completed TA demo.
5. Complete outstanding peer reviews and check each member's full-use-case
   evidence before claiming the individual requirements are satisfied.
6. Review and test each further increment before integrating it into `main`.
   The baseline is already merged; further reviewed progress can be integrated
   incrementally without waiting for the entire MVP to be complete.
7. Prepare the group presentation, rehearse the complete demo and finish each
   member's reflection and peer evaluation by the course deadlines.

Use meaningful commits and actual review evidence. Initial AI-assisted code remains
attributed to its original lead; do not manufacture teammates' contributions or
retroactively claim that the baseline followed a process it did not follow.

## Decisions to validate

- Local JSON meets persistence; Free Dictionary API meets external API usage.
- The supplied course DAO is reference material, not a dependency. Its shared HTTP
  server and cleartext password model are unnecessary for this local application.
- The first-run setup creates an administrator, subsequent registration creates
  learners. Use cases enforce the role even if the GUI hides admin controls.
- UI calls use cases through feature controllers and presenters. Business logic has no
  Swing, JSON or HTTP dependencies. Network and disk work run off the Swing EDT.
- Seed content works offline. API lookup explicitly reports failures and never
  silently presents local content as an API response.
