# Course delivery checklist

Requirements verified from the course's [Team Project directory](https://q.utoronto.ca/courses/444184/pages/team-project)
on November 17, 2026. The private source archive is kept outside this repository.
The November 15 presentation rubric remains labelled a draft; check later announcements
for a final schedule or revised rubric. Checkmarks below require actual evidence.

## Team and specification

- [x] Shared repository and signed team contract merged through [PR #1](https://github.com/fanfan170721/team-project/pull/1).
- [ ] Verify all members belong to the same Quercus group/TUT.
- [x] Project name/domain, stories, formal use cases, entities and API description documented.
- [x] MVP work and designated leads recorded; pending implementations explicitly labelled.
- [ ] Every member has implemented a complete MVP use case across interactor/interfaces,
  a specific controller/presenter, triggering UI and resulting UI updates.
- [ ] Every member has a substantial code PR, review of another member and runnable own use case.
- [ ] Every member has a merged PR demonstrating meaningful own code contributions.

## Software and validation

- [x] Java/Swing application with an external dictionary API and JSON persistence exists.
- [x] API successfully called in the packaged application on November 17.
- [x] Existing tests exercise the complete learning loop, account isolation and restart recovery.
- [x] Current baseline: 47 passing tests; measured Entity/Use Case line coverage
  445/445 (100%); Checkstyle report retained with 661 findings; coverage gate tested.
- [ ] Final integrated MVP includes UC-09 CSV export and UC-10 daily review plan.
- [ ] Final build and every interactor's tests pass after the pending MVP integrations.
- [ ] Final Entity/Use Case layers have measured 100% line coverage, including new use cases.
- [ ] Final Checkstyle run is retained and findings reviewed; all warnings need not be fixed
  to meet the rubric's tool-use condition.
- [ ] Pull-request review and CI evidence correspond to the code being merged.

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

The initial implementation is AI-assisted work led by Zidong Yuan. Additional
members' future implementation and review work must remain attributable to them;
the existence of a finished baseline cannot replace those course requirements.
