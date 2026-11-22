# Continue on another computer

Snapshot: November 22, 2026, America/Toronto. Recheck GitHub before acting on
review, branch or teammate status; these can change after this note.

## Open the correct code

The shared feature base is **`Emilia/project-delivery`** at `924c56f`, containing
PR #3/#4, maintenance PR #5, Xinyu's UC-10 core PR #7, Fuxuan's CSV adapter
PR #8, demo-safety PR #6, Fuxuan's complete export-flow PR #9 and Xinyu's complete
review-plan flow PR #11, plus reviewed package-verification PR #10. The publication
candidate is **`Emilia/publish-reviewed-mvp`** and has the same application source
as this delivery revision, with updated handoff documents. At preparation time,
`main` remained at `064dc56`, the earlier runnable baseline merged through PR #2
on November 20 (the file tree of delivery at `62adf65`). Check the publication PR
before treating `main` as current. Keep the delivery branch as the base for
teammates' existing work until a base change is coordinated.

PR #6 merged as `0ed4c82` after Fuxuan's formal approval of `c1bfcae`. PR #9
merged as `f968b44` after renewed review of its dedicated export screen and the
`69b9ee9` integration. Xinyu's complete review-plan flow in PR #11 at
`5a512ae` received renewed review and passed eight online checks before merging
as `3f4b4f3`. PR #10 (`Emilia/package-verification-evidence`) received Xinyu's
follow-up approval at `87d903e` after its concurrency fix, passed all eight online
checks and merged as `924c56f` on November 22.
Persistent demo data belongs in `.wordtrail/demo`, outside the build output directory.

[PR #12](https://github.com/fanfan170721/team-project/pull/12) contains proposed
synthetic demo preparation and English presentation guides. It still needs
teammate review and is not included in delivery or this publication candidate.

Install Git, JDK 11 or 17 and Maven 3.9+, then use a new directory. The commands
below follow the current delivery branch, not the historical maintenance branch:

```sh
git clone --branch Emilia/project-delivery https://github.com/fanfan170721/team-project.git team-project-allen
cd team-project-allen
mvn clean verify
java -jar target/wordtrail.jar --data-dir .wordtrail/demo
```

In IntelliJ, open `pom.xml` as a Maven project and select the JDK. There is no
Maven wrapper in this repository. Work from the project root. See
[QUICKSTART.md](QUICKSTART.md) for the UI and launchers.

The `tools/CheckPackage.java` entry point and its packaged UC-08/09/10 checks are
now on delivery through PR #10. From the project root after building, run:

```sh
java tools/CheckPackage.java target/wordtrail.jar target/package-check
java tools/TestPackageCheck.java target/wordtrail.jar target/package-check-regressions
```

See [PACKAGE_VERIFICATION.md](PACKAGE_VERIFICATION.md). Concurrent checks need
separate output directories; a competing wrapper exits without modifying the
active check's evidence. The 31 process assertions are separate from JUnit totals.

## Current team progress

| Work | Evidence / status at this snapshot |
| --- | --- |
| Signed team contract | [PR #1](https://github.com/fanfan170721/team-project/pull/1) merged; all three names present |
| Application baseline | [PR #2](https://github.com/fanfan170721/team-project/pull/2) merged into `main` as `064dc56` on November 20; its tree matches the earlier delivery at `62adf65`, not the later increments |
| Allen's UC-08 personal words and definitions | [PR #3](https://github.com/fanfan170721/team-project/pull/3) approved by Xinyu and merged as `d0f9f7f` on November 19 |
| Fuxuan's UC-09 quiz-results export | Core [PR #4](https://github.com/fanfan170721/team-project/pull/4), CSV adapter [PR #8](https://github.com/fanfan170721/team-project/pull/8) and dedicated adapters/UI/wiring/acceptance tests [PR #9](https://github.com/fanfan170721/team-project/pull/9) reviewed and merged; complete flow available in delivery |
| Xinyu's UC-10 due-review plan | Core and 13 tests merged through [PR #7](https://github.com/fanfan170721/team-project/pull/7); complete adapters/UI/wiring/acceptance tests reviewed and merged through [PR #11](https://github.com/fanfan170721/team-project/pull/11) as `3f4b4f3`; Xinyu also approved PR #2/#3 |
| Remaining shared integration | All three complete MVP flows and PR #10 are merged into delivery; validate and publish the candidate `Emilia/publish-reviewed-mvp` to `main` through review |
| Shared maintenance | Fuxuan merged [PR #5](https://github.com/fanfan170721/team-project/pull/5) as `ba26bfb`, then left an actual review comment; there was no Approve review event before the merge |
| Review follow-up | [PR #6](https://github.com/fanfan170721/team-project/pull/6) approved by Fuxuan at `c1bfcae` and merged as `0ed4c82`; [PR #10](https://github.com/fanfan170721/team-project/pull/10) approved by Xinyu at `87d903e` and merged as `924c56f` |
| Demo/presentation preparation | [PR #12](https://github.com/fanfan170721/team-project/pull/12) is a separate pending increment; teammate review and actual rehearsal remain outstanding |
| Repository tracking submission | Public repository URL submitted to MarkUs on November 20 at 4:56 p.m. EST; the system confirmed all required files submitted |

UC-08 includes input DTOs, an interactor, an output boundary, a dedicated
controller/presenter/view model and Swing save/edit behavior. Failed saves retain
input; successful saves select and scroll to the saved word. The original
implementation history remains attributed to Allen's work.

The publication worktree's fresh verification on **November 22 at 13:15 EST**
passed **147 JUnit tests**, **633/633 core lines** and **31 package-check process
assertions**; the new executable passed the offline packaged-flow probe.
Checkstyle reports 623 findings. Application source, build configuration and
tools match delivery `924c56f`. See [VERIFICATION.md](VERIFICATION.md) for the
artifact hash and publication/CI status.

Historical snapshot: on November 21 at 15:00 EST, the integrated PR #10 working tree passed **147 tests**
with no failures, errors or skips and built the executable JAR. Entity/Use Case
line coverage is **633/633 (100%)**; Checkstyle retains **623 findings** across
130 Java source files. The
reviewed PR #9 integration previously passed 135 tests and eight online checks.
PR #11 integration passed eight online checks before merge. The updated package
probe and 23 package-check regression assertions passed locally at that snapshot.
After Xinyu identified a concurrent-report bug, the fix passed 31 process assertions
and eight online checks. Her [follow-up review at `87d903e`](https://github.com/fanfan170721/team-project/pull/10#pullrequestreview-6009455370)
confirmed the 147-test build, full core coverage and packaged probe before merge.
Fresh publication-candidate verification is recorded separately in
[VERIFICATION.md](VERIFICATION.md); these earlier measurements do not by themselves
validate a changed revision.

The shared package probe now exercises personal-word persistence, review-plan
generation without changing progress, account-isolated quiz CSV export and
reopening the store using only the packaged JAR. Its wrapper invalidates stale
reports and records the JAR hash and reported revision; the metadata is not a
signature. Consult the current report and [PACKAGE_VERIFICATION.md](PACKAGE_VERIFICATION.md)
for results and limits. A silent live API lookup succeeded on November 20;
automated package tests use an offline dictionary.
See [VERIFICATION.md](VERIFICATION.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## November 20 milestone

M2 requires each member's substantial PR, review of another member, and runnable
work on their assigned use case that they can demonstrate and discuss with the
TA. The team also needs a real API call and one complete end-to-end use case.
All MVP features integrated is the November 27 milestone. Submitting the repo
link does not by itself satisfy the individual M2 requirements.

- Allen: UC-08 is merged, and the review of Fuxuan's PR #4 is recorded. Packaged
  persistence and the live API call were verified on November 20.
- Fuxuan: the complete UC-09 flow is merged through PR #9; formal approval of
  PR #6 is recorded on the reviewed revision. The PR #5 comment remains a
  post-merge comment, not approval before that earlier merge.
- Xinyu: UC-10 core and 13 tests are merged; approvals on PR #2/#3 are recorded.
  The complete review-plan flow is reviewed, tested and merged through PR #11.

All three members have substantial merged code, actual peer-review records and
runnable progress. Their own TA demonstrations and explanations have not been
verified; these records do not establish completion of every course requirement.

Use the short [M2 demonstration checklist](DEMO.md) to prepare actual runnable
evidence. A prepared checklist is not a record of a completed demonstration.

## Next steps, in order

1. Recheck online and local progress before assigning work; do not recreate the
   merged cores or CSV adapter, or overwrite unpushed changes.
2. Use the merged delivery containing all three full MVP flows; do not recreate
   the teammates' completed features or reopen their finished implementation tasks.
3. Review the publication candidate, verify its source matches reviewed delivery,
   and inspect its own test/package evidence and CI before merging to `main`.
4. Review PR #12's independent demo-data and presentation preparation. Address
   concrete feedback before integration; do not claim it is already on delivery.
5. Each member runs and explains their own flow, then the team rehearses the full
   presentation. Keep actual TA demonstrations, reflections and peer evaluations
   distinct from repository publication and prepared scripts.

The published teammate instructions are:

- [Current status and historical assignment entry point](START_HERE.zh-CN.md)
- [Fuxuan's completed export-flow assignment](FIRST_TASK_FUXUAN.zh-CN.md)
- [Xinyu's submitted review-plan assignment](FIRST_TASK_XINYU.zh-CN.md)
- [Historical task plan](TEAM_TASKS.zh-CN.md)

Further teammate branches start from and submit PRs into `Emilia/project-delivery`.
Coordinate before changing an existing task's base. The first core tasks are
merged and both complete UI flows are implemented; do not repeat these tasks.

## Requirements and working preferences

- Read [COURSE_REQUIREMENTS.md](COURSE_REQUIREMENTS.md) and
  [DELIVERY_CHECKLIST.md](DELIVERY_CHECKLIST.md) before planning the next work.
  Quercus lists all three members in `[TUT5101] Group 31 - BA3195` and
  LEC5101/TUT5101 (checked November 19). Final presentation, reflection and peer
  evaluations remain outstanding.
- Use English for project code, UI and documentation; Chinese is reserved for
  teammate communication. Communicate with Allen concisely in Chinese with
  concrete instructions. On November 20, Allen replaced the earlier 30-minute
  cap with larger complete-feature assignments, estimated at 2–3 hours per member.
  Do not impose artificial waiting or commit timing; report actual work.
- The course syllabus allows generative AI throughout the course. Contributions,
  authorship, reviews and statements about test execution must reflect real work.
  Do not implement teammates' entire assigned use cases in their identities.
- Do not initiate audible playback or sound tests without Allen's explicit
  request. Keep automated tests silent.
- The old local `team-project` folder was intentionally preserved. Its only
  uncommitted difference is an obsolete unsigned contract draft; the signed
  contract is already online. Do not overwrite the signed version with that draft.

## Separate Java reference application

The recent placement-refresh and recorded-pronunciation changes are in a separate
[reference repository](https://github.com/fanfan170721/vocabulary-learning-java-reference/tree/Emilia/reference-reassessment-audio),
branch `Emilia/reference-reassessment-audio`, implementation commit `946e04f`.
Its own `docs/HANDOFF.md` explains how to continue. Those changes have **not** been
merged into this team project and do not replace members' team contributions.

## Data outside Git

Git synchronizes source, documentation and task progress, not personal learning
profiles. `.wordtrail/wordtrail.json`, IDE settings and generated `target/` files
are ignored. Close the old app before privately copying a learning profile; do
not publish it in Git. Generated builds and reports can be recreated with Maven.

The original private Quercus text/slides archive stays on the old computer under
`Documents/Codex/CSC207-project-requirements/2026-11-17`. This repository contains
an English requirements summary and official links, not a republication of the
instructor's source materials. The course pages may require a fresh login.
