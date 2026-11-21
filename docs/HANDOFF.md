# Continue on another computer

Snapshot: November 20, 2026, America/Toronto. Recheck GitHub before acting on
review, branch or teammate status; these can change after this note.

## Open the correct code

The shared feature base is **`Emilia/project-delivery`** at `cebde6d`, containing
PR #3/#4, maintenance PR #5, Xinyu's UC-10 core PR #7 and Fuxuan's CSV adapter
PR #8. PR #2 was merged
on November 20: `main` at `064dc56` now contains the runnable application, with
the same file tree as delivery at `62adf65`. Keep the delivery branch as the base
for teammates' existing work until the next integrations are coordinated.

The follow-up branch `Emilia/m2-demo-safety` contains PR #6, now targeting
delivery. It adds demo-data protection, review follow-ups and updated documentation;
peer approval remains outstanding. Persistent demo data belongs in `.wordtrail/demo`,
outside the build output directory.

Install Git, JDK 11 or 17 and Maven 3.9+, then use a new directory:

```sh
git clone --branch Emilia/m2-demo-safety https://github.com/fanfan170721/team-project.git team-project-allen
cd team-project-allen
mvn clean verify
java "-Djava.awt.headless=true" --class-path target/wordtrail.jar tools/VerifyPackage.java target/package-check
java -jar target/wordtrail.jar
```

In IntelliJ, open `pom.xml` as a Maven project and select the JDK. There is no
Maven wrapper in this repository. Work from the project root. See
[QUICKSTART.md](QUICKSTART.md) for the UI and launchers.

## Current team progress

| Work | Evidence / status at this snapshot |
| --- | --- |
| Signed team contract | [PR #1](https://github.com/fanfan170721/team-project/pull/1) merged; all three names present |
| Application baseline | [PR #2](https://github.com/fanfan170721/team-project/pull/2) merged into `main` as `064dc56` on November 20; its tree matches the earlier delivery at `62adf65`, not the later increments |
| Allen's UC-08 personal words and definitions | [PR #3](https://github.com/fanfan170721/team-project/pull/3) approved by Xinyu and merged as `d0f9f7f` on November 19 |
| Fuxuan's UC-09 quiz-results export | Core and eight tests merged through [PR #4](https://github.com/fanfan170721/team-project/pull/4); CSV adapter and 11 tests reviewed and merged through [PR #8](https://github.com/fanfan170721/team-project/pull/8) as `cebde6d` |
| Xinyu's UC-10 due-review plan | Core and 13 tests reviewed and merged through [PR #7](https://github.com/fanfan170721/team-project/pull/7) as `e93804d`; Xinyu also approved PR #2/#3 |
| Remaining teammate implementation | UC-09 and UC-10 still need dedicated Controller/Presenter/ViewModel, UI and application wiring; unpushed local progress is unknown |
| Shared maintenance | Fuxuan merged [PR #5](https://github.com/fanfan170721/team-project/pull/5) as `ba26bfb`, then left an actual review comment; there was no Approve review event before the merge |
| Review follow-up | [PR #6](https://github.com/fanfan170721/team-project/pull/6) targets delivery and still awaits peer approval; its fixes and documentation need review on the updated version |
| Repository tracking submission | Public repository URL submitted to MarkUs on November 20 at 4:56 p.m. EST; the system confirmed all required files submitted |

UC-08 includes input DTOs, an interactor, an output boundary, a dedicated
controller/presenter/view model and Swing save/edit behavior. Failed saves retain
input; successful saves select and scroll to the saved word. The original
implementation history remains attributed to Allen's work.

Current local verification of PR #6's working tree: **100 tests passed** with no
failures, errors or skips; Entity/Use Case line coverage is **633/633 (100%)**.
The current Checkstyle report retains **588 findings**; there are 113 Java source
files. Cached report file entries are not the project's complete source count.
The maintenance branch also fixes real `All topics` names being treated as no
filter, and recently-reviewed ordering during a repeated daylight-saving hour.
The offline package check passed using the executable JAR only, exercising seed
data, application wiring, the JSON dependency and persistence across reopening.
The [maintenance push CI](https://github.com/fanfan170721/team-project/actions/runs/44643837835)
and [PR CI](https://github.com/fanfan170721/team-project/actions/runs/44643846486)
passed Windows/Linux on Java 11/17 at `f6a4ee2`, including package and launcher
checks. Fuxuan's review comment came after he merged PR #5. On November 20 at
20:13 EST, the follow-up working tree passed the 100-test verification and the
offline package check, including merged PR #7/#8 plus typed-topic and review-time
equality fixes. A silent live lookup succeeded earlier that day through the
primary dictionary provider. Updated PR #6 still needs its own remote checks and
peer approval; earlier CI results do not verify these latest changes.
See [VERIFICATION.md](VERIFICATION.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## November 20 milestone

M2 requires each member's substantial PR, review of another member, and runnable
work on their assigned use case that they can demonstrate and discuss with the
TA. The team also needs a real API call and one complete end-to-end use case.
All MVP features integrated is the November 27 milestone. Submitting the repo
link does not by itself satisfy the individual M2 requirements.

- Allen: UC-08 is merged, and the review of Fuxuan's PR #4 is recorded. Packaged
  persistence and the live API call were verified on November 20.
- Fuxuan: UC-09 core and CSV adapter are merged; the PR #5 review comment is
  recorded after the merge, not as approval before it. The UI flow is unfinished.
- Xinyu: UC-10 core and 13 tests are merged; approvals on PR #2/#3 are recorded.
  The review-plan UI flow is unfinished.

All three members have substantial merged code, actual peer-review records and
runnable progress. Their own TA demonstrations and explanations have not been
verified; these records do not establish completion of every course requirement.

Use the short [M2 demonstration checklist](DEMO.md) to prepare actual runnable
evidence. A prepared checklist is not a record of a completed demonstration.

## Next steps, in order

1. Recheck online and local progress before assigning work; do not recreate the
   merged cores or CSV adapter, or overwrite unpushed changes.
2. Fuxuan continues UC-09 with its dedicated Controller/Presenter/ViewModel and
   behavior tests, then the UI and wiring in a later short session. He already
   implemented CSV, so no handoff to Allen is needed.
3. Xinyu continues UC-10 with its dedicated Controller/Presenter/ViewModel and
   behavior tests, then the UI and wiring. Keep each working session within 30 minutes.
4. Each member prepares to run and explain their current code using the M2 demo
   checklist. Distinguish preparation from an actually completed TA demonstration.
5. Obtain peer review of updated PR #6 before integration. It already targets
   delivery. PR #2 publishes the baseline on `main`; later reviewed delivery
   increments can reach `main` without waiting for the entire MVP to be complete.

The published teammate instructions are:

- [First-session setup](START_HERE.zh-CN.md)
- [Fuxuan's first task](FIRST_TASK_FUXUAN.zh-CN.md), completed core reference
- [Xinyu's first task](FIRST_TASK_XINYU.zh-CN.md), completed core reference
- [Full task plan](TEAM_TASKS.zh-CN.md)

Further teammate branches start from and submit PRs into `Emilia/project-delivery`.
Coordinate before changing an existing task's base. The first core tasks are
merged; both complete UI flows remain unfinished.

## Requirements and working preferences

- Read [COURSE_REQUIREMENTS.md](COURSE_REQUIREMENTS.md) and
  [DELIVERY_CHECKLIST.md](DELIVERY_CHECKLIST.md) before planning the next work.
  Quercus lists all three members in `[TUT5101] Group 31 - BA3195` and
  LEC5101/TUT5101 (checked November 19). Final presentation, reflection and peer
  evaluations remain outstanding.
- Use English for project code, UI and documentation; Chinese is reserved for
  teammate communication. Communicate with Allen concisely in Chinese, one small
  actionable step at a time. Each human task should fit within 30 minutes.
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
