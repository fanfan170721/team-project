# Continue on another computer

Snapshot: November 17, 2026, America/Toronto. Recheck GitHub before acting on
review, branch or teammate status; these can change after this note.

## Open the correct code

The current personal feature is on **`Emilia/allen-personal-vocabulary`**, not
`main`. The integrated baseline is on `Emilia/project-delivery`. At this snapshot,
`main` contains the merged team contract and original template, not the application.

Install Git, JDK 11 or 17 and Maven 3.9+, then use a new directory:

```sh
git clone --branch Emilia/allen-personal-vocabulary https://github.com/fanfan170721/team-project.git team-project-allen
cd team-project-allen
mvn verify
java -jar target/wordtrail.jar
```

In IntelliJ, open `pom.xml` as a Maven project and select the JDK. There is no
Maven wrapper in this repository. Work from the project root. See
[QUICKSTART.md](QUICKSTART.md) for the UI and launchers.

## Current team progress

| Work | Evidence / status at this snapshot |
| --- | --- |
| Signed team contract | [PR #1](https://github.com/fanfan170721/team-project/pull/1) merged; all three names present |
| Application baseline | [PR #2](https://github.com/fanfan170721/team-project/pull/2), draft and unmerged; `Emilia/project-delivery` at `6763eff` |
| Allen's UC-08 personal words and definitions | [PR #3](https://github.com/fanfan170721/team-project/pull/3), open and ready for review; implementation commit `4867865` |
| PR #3 review | No submitted reviews yet when checked; documentation-only handoff commits may follow the implementation commit |
| Fuxuan's UC-09 quiz-results export | First core implementation task sent; no teammate feature branch or PR observed yet |
| Xinyu's UC-10 due-review plan | First core implementation task sent; no teammate feature branch or PR observed yet |

UC-08 includes input DTOs, an interactor, an output boundary, a dedicated
controller/presenter/view model and Swing save/edit behavior. Failed saves retain
input; successful saves select and scroll to the saved word. The original
implementation history remains attributed to Allen's AI-assisted work.

Verification at implementation commit `4867865`: **58 tests passed**, Entity/Use
Case line coverage **487/487 (100%)**, and **666 Checkstyle findings** retained.
The [PR CI run](https://github.com/fanfan170721/team-project/actions/runs/44190680563)
passed Windows/Linux on Java 11/17. Check later commits' checks independently.
See [VERIFICATION.md](VERIFICATION.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## Next steps, in order

1. Recheck PR #3 reviews and open teammate PRs. A copy-ready group review message
   was drafted for Allen; its delivery and the reviews were not yet confirmed.
2. Fuxuan's review focus: save-before-success, failure callbacks and no partial
   changes in `PersonalVocabularyInteractor` and its tests. Xinyu's review focus:
   retained editor input, saved-word selection/scrolling and the Swing regression.
   Reviewers should state what they actually inspected and whether they ran locally.
3. Address actual feedback, then merge reviewed code into its intended base.
   PR #3 targets `Emilia/project-delivery`; PR #2 targets `main`. This snapshot is
   not an instruction to merge without checking current reviews and CI.
4. Review the teammates' first core PRs, then give each one next implementation
   task of at most 30 minutes for their own adapters, UI and integration.
5. Allen must also participate in reviewing another member's substantive code PR.
   Integrate the complete UC-09/10 features and rerun the full final checks.

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
  Quercus group/TUT membership remains unverified; final presentation, reflection
  and peer evaluations remain outstanding.
- Use English for project code, UI and documentation; Chinese is reserved for
  teammate communication. Communicate with Allen concisely in Chinese, one small
  actionable step at a time. Each human task should fit within 30 minutes.
- AI assistance is allowed in the team's stated workflow. Contributions,
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
