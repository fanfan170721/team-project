# WordTrail implementation plan

The existing AI-assisted Java/Swing baseline implements eight stories. The signed
contract is integrated from `main`. The current delivery work aligns the code,
documentation and validation with the course requirements verified on November 17.

## Status on November 20, 2026

- The signed contract is merged through PR #1. Application delivery PR #2 merged
  into `main` as `064dc56`, with the same tree as reviewed/tested `62adf65`.
  `main` now contains the runnable baseline, including PR #3 and PR #4.
  Teammates continue using `Emilia/project-delivery` as their current work base.
- Maintenance PR #5 still awaits review; PR #6 is a separate follow-up based on
  PR #5. Neither is included in the merged baseline.
- Formal specifications, feature controllers/presenters, coverage measurement
  and reproducible quality reports are present in `Emilia/project-delivery`.
- Allen's complete UC-08 flow is merged through PR #3 (`d0f9f7f`).
- Fuxuan's UC-09 core and eight interactor tests are merged through PR #4
  (`62adf65`); the concrete CSV adapter and controller/presenter/UI are unfinished.
- The combined delivery passed 66 tests with 535/535 Entity/Use Case lines covered.
  CI for `62adf65` passed all Windows/Linux and Java 11/17 combinations.
  The latest clean local check retained 677 Checkstyle findings across 101 Java
  files and passed an offline packaged-JAR wiring, content and persistence check.
  New CI checks require a subsequent remote run.
- Xinyu approved PR #2 and PR #3, but no UC-10 implementation branch or PR was
  observed. Fuxuan's code peer review remains outstanding.

## Remaining increments

1. Allen maintains shared documentation, build/test evidence and integration.
   Keep measured Checkstyle findings visible; a passing build is not style clearance.
2. Resolve the proposed handoff of remaining CSV work to Allen with Fuxuan.
   It is awaiting his reply, so the designated lead remains Fuxuan. Record any
   agreed change truthfully and preserve each member's required complete use case.
3. After that reply, complete UC-09's CSV adapter, specific controller/presenter,
   file-selection UI, safe cancellation/overwrite behavior and acceptance tests.
4. Xinyu implements UC-10's interactor/interfaces, controller/presenter, UI and
   tests, with her own meaningful code PR. Existing approvals do not replace code work.
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
