# Course delivery checklist

Requirements verified from the course's [Team Project directory](https://q.utoronto.ca/courses/444184/pages/team-project)
on November 17, 2026. The private source archive is kept outside this repository.
The November 15 presentation rubric remains labelled a draft; check later announcements
for a final schedule or revised rubric. Checkmarks below require actual evidence.

Implementation and collaboration status below was checked on November 21, 2026.
The runnable application is now on `main` at `064dc56`, merged through
[PR #2](https://github.com/fanfan170721/team-project/pull/2). Its file tree equals
delivery at `62adf65`. The current work base, `Emilia/project-delivery` at
`3f4b4f3`, additionally contains PR #5, UC-10 core PR #7, CSV adapter PR #8,
demo-safety PR #6 and the complete UC-09/UC-10 flows from PR #9/#11. PR #10
includes both flows and package verification, and still requires peer review.
These later increments are not yet on `main`.

## Team and specification

- [x] Shared repository and signed team contract merged through [PR #1](https://github.com/fanfan170721/team-project/pull/1).
- [x] Quercus lists Fuxuan Xu, Xinyu Huang and Zidong Yuan together in
  `[TUT5101] Group 31 - BA3195`; all three are in LEC5101/TUT5101 (checked November 19).
- [x] Project name/domain, stories, formal use cases, entities and API description documented.
- [x] MVP work and designated leads recorded; pending implementations explicitly labelled.
- [x] Public repository link submitted to MarkUs on November 20 at 4:56 p.m. EST;
  the system confirmed all required files submitted. This is the repository
  tracking submission, not the final presentation/reflection submission.
- [x] Every member has implemented a complete MVP use case across interactor/interfaces,
  a specific controller/presenter, triggering UI and resulting UI updates.
  UC-08/#3, UC-09/#9 and UC-10/#11 are reviewed, tested and merged into delivery.
- [x] Every member has a substantial code PR, a real review record on another
  member's work and runnable code related to their own use case.
- [x] Every member has a merged PR demonstrating meaningful own code contributions.
- [ ] Each member personally demonstrates and explains their runnable work to the TA.

| Member | Verified contribution/review evidence | Remaining individual work |
| --- | --- | --- |
| Allen | Complete UC-08 merged through PR #3 (`d0f9f7f`); reviewed Fuxuan's PR #4 | Retain actual implementation and review evidence for submission |
| Fuxuan | Complete UC-09 flow merged through PR #9 (`f968b44`), extending core #4 and CSV #8; formally approved PR #6 at `c1bfcae` | Personally demonstrate and explain the flow; retain actual evidence |
| Xinyu | UC-10 core #7 and complete UI/adapters/wiring/acceptance tests #11 (`3f4b4f3`) reviewed and merged; approved PR #2 and PR #3 | Personally demonstrate and explain the flow; retain actual evidence |

A merged core-only PR or a review approval does not establish a complete personal
MVP use case. Fuxuan implemented the CSV adapter himself; no handoff to Allen is
needed. PR #6 received Fuxuan's formal approval and merged as `0ed4c82`.
His earlier PR #5 comment must not be described as approval before that merge.
PR #10's current package-verification and handoff increment still needs peer review.

## Software and validation

- [x] Java/Swing application with an external dictionary API and JSON persistence exists.
- [x] API successfully called in the packaged application on November 17.
- [x] Primary dictionary API succeeded again in a silent packaged diagnostic on November 20.
- [x] Existing tests exercise the complete learning loop, account isolation and restart recovery.
- [x] Combined delivery at `62adf65`: 66 passing tests and measured Entity/Use Case
  line coverage 535/535 (100%); coverage gate passed.
- [x] CI at `62adf65` passed all four Windows/Linux and Java 11/17 jobs.
- [x] Updated PR #6 working-tree verification: 100 passing JUnit tests, including
  merged PR #7/#8 and the typed-topic/review-time follow-up fixes.
- [x] Current complete Entity/Use Case line coverage: 633/633 (100%).
- [x] November 21 integrated PR #10 working-tree verification: 147 JUnit tests,
  zero failures/errors/skips; executable JAR built at 15:00 EST.
- [x] Latest integrated Checkstyle report: 623 findings across 130 Java source files; a passing build
  is not a statement that the source is free of style findings.
- [x] Launcher regression checks: 152 assertions passed on Windows PowerShell 5.1
  and PowerShell 7, including protection of profiles placed in build output.
- [x] Offline packaged-JAR check passed wiring, bundled content, JSON dependency
  and account/personal-word persistence after reopening, without UI or network.
- [x] PR #5 at `f6a4ee2` passed eight push/PR checks across Windows/Linux and
  Java 11/17. Subsequent changes need their own checks and peer review.
- [x] Updated PR #6 received formal peer approval and passed its remote checks before merge.
- [x] PR #9's reviewed `69b9ee9` integration passed 135 tests and eight online checks before merge.
- [x] Combined local MVP includes UC-09 CSV export and UC-10 daily review plan,
  with all interactor tests passing and Entity/Use Case coverage 633/633 (100%).
- [x] PR #11's reviewed `5a512ae` integration passed all eight online checks and merged as `3f4b4f3`.
- [x] Updated PR #10 packaged-flow probe and 23 regression assertions passed locally.
- [ ] Updated PR #10 package-verification increment passes current checks, receives peer review and merges.
- [ ] Reviewed MVP delivery is published to `main` and checked there.
- [ ] Final Checkstyle run is retained and findings reviewed; all warnings need not be fixed
  to meet the rubric's tool-use condition.
- [ ] Final presentation build, style report and PR/CI evidence correspond to the published revision.

## Presentation and individual submission

- [ ] Prepare a roughly 15-minute presentation and up to 5-minute Q&A; every member speaks.
- [ ] Cover all 11 rubric categories; show actual saving/loading, API use, architecture,
  at least two SOLID examples, an appropriate added pattern and extensibility.
- [ ] Use a representative PR and Checkstyle evidence to explain code quality.
- [ ] Prepare demo data and a working executable; rehearse transitions and the complete demo.
- [ ] Each member writes their own truthful reflection, at most one page, for MarkUs by December 8.
- [ ] Each member submits the peer evaluation by December 8 once it is available.

Milestones: M2 November 20 (individual PR/review/runnable work), M3 November 27
(at least one unit test per interactor and integrated MVP), M4 December 4
(100% Entity/Use Case line coverage and rehearsed presentation). Presentations
are during December 7/8 labs, subject to the course's detailed schedule.

The initial implementation is work led by Zidong Yuan. Additional
members' implementation and review work must remain attributable to them;
the existence of a finished baseline cannot replace those course requirements.
