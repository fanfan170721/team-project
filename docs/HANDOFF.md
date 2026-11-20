# Continue on another computer

Snapshot: November 20, 2026, America/Toronto. Recheck GitHub before acting on
review, branch or teammate status; these can change after this note.

## Open the correct code

The shared feature base is **`Emilia/project-delivery`** at `62adf65`, containing
the merged PR #3 and PR #4. Public documentation/build maintenance is on
`Emilia/shared-project-maintenance`, for review into that base. PR #2 was merged
on November 20: `main` at `064dc56` now contains the runnable application, with
the same file tree as delivery at `62adf65`. Keep the delivery branch as the base
for teammates' existing work until the next integrations are coordinated.

The follow-up branch `Emilia/m2-demo-safety` adds protection against deleting
demo profiles during a launcher rebuild, on top of PR #5 at `f6a4ee2`. Keep
PR #5 unchanged while its teammate review is in progress. Persistent demo data
belongs in `.wordtrail/demo`, outside the build output directory.

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
| Application baseline | [PR #2](https://github.com/fanfan170721/team-project/pull/2) merged into `main` as `064dc56` on November 20; delivery remains at `62adf65`, with an identical file tree |
| Allen's UC-08 personal words and definitions | [PR #3](https://github.com/fanfan170721/team-project/pull/3) approved by Xinyu and merged as `d0f9f7f` on November 19 |
| Fuxuan's UC-09 quiz-results export | [PR #4](https://github.com/fanfan170721/team-project/pull/4), core and eight tests, approved by Allen and merged as `62adf65` on November 19; no CSV adapter or UI yet |
| Xinyu's UC-10 due-review plan | No own feature branch/PR observed; Xinyu has submitted approvals on PR #2 and PR #3 |
| Subsequent teammate progress | No new remote feature branch, PR, issue or blocker comment observed; unpushed local work and blockers are unknown |
| Shared maintenance | [PR #5](https://github.com/fanfan170721/team-project/pull/5) at `f6a4ee2` has eight successful CI checks; no submitted review observed at this snapshot |
| Repository tracking submission | Public repository URL submitted to MarkUs on November 20 at 4:56 p.m. EST; the system confirmed all required files submitted |

UC-08 includes input DTOs, an interactor, an output boundary, a dedicated
controller/presenter/view model and Swing save/edit behavior. Failed saves retain
input; successful saves select and scroll to the saved word. The original
implementation history remains attributed to Allen's work.

Current local verification: **74 tests passed**, Entity/Use Case line
coverage **574/574 (100%)**, and **586 Checkstyle findings** across 104 Java files.
The maintenance branch also fixes real `All topics` names being treated as no
filter, and recently-reviewed ordering during a repeated daylight-saving hour.
The offline package check passed using the executable JAR only, exercising seed
data, application wiring, the JSON dependency and persistence across reopening.
The [maintenance push CI](https://github.com/fanfan170721/team-project/actions/runs/44643837835)
and [PR CI](https://github.com/fanfan170721/team-project/actions/runs/44643846486)
passed Windows/Linux on Java 11/17 at `f6a4ee2`, including package and launcher
checks. Review remains outstanding. On November 20, the follow-up branch passed
the same 74 Java tests and the offline package check; a silent live lookup also
succeeded through the primary dictionary provider.
See [VERIFICATION.md](VERIFICATION.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## November 20 milestone

M2 requires each member's substantial PR, review of another member, and runnable
work on their assigned use case that they can demonstrate and discuss with the
TA. The team also needs a real API call and one complete end-to-end use case.
All MVP features integrated is the November 27 milestone. Submitting the repo
link does not by itself satisfy the individual M2 requirements.

- Allen: UC-08 is merged, and the review of Fuxuan's PR #4 is recorded. Packaged
  persistence and the live API call were verified on November 20.
- Fuxuan: UC-09 core and eight tests are merged. A substantive peer review is
  still needed; PR #5 is ready for that review. CSV/UI are later increments.
- Xinyu: prior reviews are recorded; a substantial UC-10 implementation PR and
  runnable progress are still unverified. Continue existing local work.

Use the short [M2 demonstration checklist](DEMO.md) to prepare actual runnable
evidence. A prepared checklist is not a record of a completed demonstration.

## Next steps, in order

1. Recheck online activity before declaring an assigned task complete. The user
   reports no direct teammate progress replies yet; no new push alone cannot
   distinguish not started, local work in progress, or a blocker.
2. **Keep CSV work on hold pending Fuxuan's reply.** A possible handoff of the
   file adapter and its tests to Allen has been discussed, not confirmed. Do not
   duplicate or overwrite Fuxuan's unpushed implementation. UC-09 leadership and
   its specific Controller/Presenter/UI remain with Fuxuan.
3. Xinyu continues the UC-10 core task from her existing progress. Once code is
   pushed, review it and assign the next bounded step. Do not restart unseen local work.
4. Fuxuan still needs a substantive review of another member's code. He can review
   Allen's [PR #5](https://github.com/fanfan170721/team-project/pull/5), focusing on
   the topic-name collision and chronological review ordering regression fixes.
5. Review PR #5 before integrating it; then retarget [PR #6](https://github.com/fanfan170721/team-project/pull/6)
   from the maintenance branch to delivery for its own review and integration.
   PR #2 already publishes the reviewed baseline on `main`; there is no course
   requirement to hold a runnable baseline until the entire MVP is complete.
   Later delivery increments still need review before reaching `main`.

The published teammate instructions are:

- [First-session setup](START_HERE.zh-CN.md)
- [Fuxuan's first task](FIRST_TASK_FUXUAN.zh-CN.md), branch `Emilia/xfx-export-results`
- [Xinyu's first task](FIRST_TASK_XINYU.zh-CN.md), branch `Emilia/xinyu-due-review`
- [Full task plan](TEAM_TASKS.zh-CN.md)

Both teammate branches start from and submit PRs into `Emilia/project-delivery`.
Do not change their base to Allen's branch midway through their current task
without coordinating. Their first task implements the core only, not the complete UI.

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
