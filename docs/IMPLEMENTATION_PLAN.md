# WordTrail implementation plan

The existing AI-assisted Java/Swing baseline implements eight stories. The signed
contract is integrated from `main`. The current delivery work aligns the code,
documentation and validation with the course requirements verified on November 17.

## Status on November 20, 2026

- The signed contract is merged through PR #1. Application delivery PR #2 merged
  into `main` as `064dc56`, with the same tree as reviewed/tested `62adf65`.
  `main` now contains the runnable baseline, including PR #3 and PR #4.
  Teammates continue using `Emilia/project-delivery` as their current work base.
- Fuxuan merged maintenance PR #5 as `ba26bfb`, then left an actual review comment;
  there was no Approve review event before that merge. PR #6 now targets delivery
  and awaits peer approval. These changes are not included in the `main` baseline.
- Formal specifications, feature controllers/presenters, coverage measurement
  and reproducible quality reports are present in `Emilia/project-delivery`.
- Allen's complete UC-08 flow is merged through PR #3 (`d0f9f7f`).
- Fuxuan's UC-09 core and eight interactor tests are merged through PR #4
  (`62adf65`); his CSV adapter and 11 tests are merged through PR #8 (`cebde6d`).
  Xinyu's UC-10 core and 13 tests are merged through PR #7 (`e93804d`).
  Delivery is now at `cebde6d`; both use cases still need controller/presenter/UI
  and wiring to become complete application flows.
- The updated PR #6 working tree, including merged PR #7/#8 and follow-up fixes,
  passed 100 tests with 633/633 Entity/Use Case lines covered and an offline
  packaged-JAR wiring/content/persistence check. Checkstyle retains 588 findings;
  the project has 113 Java source files, independently of cached report entries.
  Updated PR #6 still requires its own remote checks and peer approval.
- All three members have substantial merged contributions and actual peer-review
  records. Their personal TA demonstrations and explanations remain unverified.

## Remaining increments

1. Allen maintains shared documentation, build/test evidence and integration.
   Keep measured Checkstyle findings visible; a passing build is not style clearance.
2. Fuxuan continues from his merged core and CSV adapter; no CSV handoff to Allen
   is needed. Add the specific controller/presenter and their behavior tests.
3. Complete UC-09's file-selection UI, safe cancellation/overwrite interaction,
   application wiring and full-flow acceptance tests.
4. Xinyu continues from her merged UC-10 core with controller/presenter, UI,
   wiring and tests through further focused PRs.
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
