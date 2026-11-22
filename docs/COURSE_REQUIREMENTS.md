# CSC207 project requirements

Source audit: **November 17, 2026, Toronto time**; CSC207H1F 20269, Quercus course 444184.
The syllabus AI policy was additionally checked on **November 19, 2026**.
The Project Expectations, Timeline and README/MVP pages were rechecked on
**November 20, 2026** for task scope and working-time guidance.
This is an English paraphrase of the verified requirements notes, not a copy of course pages or slides.
Check the official sources for later announcements. The November 15 presentation rubric was still
labeled **draft**, and the exact presentation schedule and duration were awaiting confirmation.
This document records requirements, not current project completion; inspect the repository and PRs for evidence.

## Team setup and development process

- Members must share the same TUT and join the same project group through Quercus People/Groups.
- Share a team GitHub repository and add all members as collaborators; an existing repository may be used.
- The team contract must address communication, response times, conflict handling and escalation to course staff, with observable expectations.
- Develop the contract on a branch, obtain the whole team's agreement and each member's signature, then merge it through a PR.
- Use PRs and reviews throughout planning, implementation, testing and improvement. A contract PR and one final code dump do not establish continuous collaboration.
- Attend labs and agreed meetings, communicate delays and absences, contribute regularly and treat teammates respectfully. Staff consider lab interactions, peer evaluations and Git records.
- The checked project pages expect progress outside labs and weekly participation;
  they do not specify a minimum or maximum duration for an individual coding session.
  Task-duration estimates are team planning choices, not professor-prescribed limits.

## Software and architecture

- Build a **Java application with a Swing GUI**. JavaFX requires course approval; a web application is not an approved replacement.
- Actually use at least one external API. A Java wrapper library is permitted.
- Persist application data using files, a database or an API. The supplied DAO is an example, not a mandatory implementation.
- Apply Clean Architecture, SOLID and suitable design patterns; explain design decisions and any tradeoffs with evidence.
- Provide working software and demonstrate behavior, design, future improvements and effective development practices.
- The reviewed pages do not require an interface literally named `OutputBoundary`. They do require use-case-specific Controllers and Presenters; course examples use input/output boundaries and DTOs. Assess dependency direction and responsibilities, not class names alone.

## Each member's implementation and evidence

- Each person implements a substantial, complete user story and leads at least one use case **inside the committed MVP**.
- The lead writes most of that use case's Interactor and related interfaces, its specific Controller and Presenter, the UI that triggers it, and the UI updates after execution.
- Coordinate shared code with the other leads. Do not take over a teammate's use case or rewrite their contribution without discussion.
- Each member needs substantial code PRs and merged contribution evidence, reviews another member's PR, and can demonstrate and explain their own use case.
- Assignments, signatures, formatting changes, isolated test additions or documentation alone are not substitutes for implementation.
- A feature used to establish individual MVP leadership cannot simultaneously be classified as optional future work.
- Every member speaks in the final presentation and completes their own reflection and peer evaluation.
- The [course syllabus](https://q.utoronto.ca/courses/444184/assignments/syllabus), checked November 19, permits generative AI throughout the course. It recommends using these tools to support learning, cautions against relying on them to complete the work, and leaves each student responsible for their own learning. Keep authorship, tests and reviews truthful; tool use does not replace individual participation.

## README and specification

The supplied README template is optional; the required information is not:

1. Project/team name, team information, domain and a short application description.
2. Substantial user stories, aiming for one per member, and the use cases that implement them.
3. The minimum MVP set, a named lead for each MVP use case, and a separate list of later work.
4. At least two domain entity types, including their fields and relevant methods; text or UML is acceptable.
5. API documentation links, intended use and whether each listed API has actually been called successfully.
6. Updates as implementation progresses. Updating documentation with feature PRs is the recommended workflow.

Use-case specifications identify the name, primary actor, preconditions, trigger, successful outcome,
numbered main scenario and alternative/error paths associated with the relevant steps. Describe what
the user and system do, rather than classes, database calls or implementation mechanics.
PlantUML and sequence diagrams are recommendations, not exclusive tools; presentation grading does require useful architecture evidence.

## Milestones and submissions

All dates below refer to **2026**. These pages describe project milestones; they did not establish a late-submission policy.

| Stage | Target | Expected evidence |
| --- | --- | --- |
| M1: planning and initial development | Friday, November 13 | Shared repository; complete README; blueprint and MVP ownership; core entities implemented; each member's UI and Interactor started. |
| M2: individual progress and prototype | Friday, November 20 | Each member has a substantial PR, reviews another member's PR and has runnable use-case progress. The team can call its API and demonstrate one complete end-to-end use case. |
| M3: integrated MVP | Friday, November 27 | At least one unit test per Interactor; all MVP use cases implemented and integrated; seek final TA feedback. |
| M4: polish and rehearsal | Friday, December 4 | **100% line coverage in Entity and Use Case layers**, remaining bugs addressed, demonstrable software and a rehearsed presentation. |
| Final presentation | December 7/8 labs | Demonstrate the MVP, give every member a speaking role and answer design/implementation questions; exact order to be announced. |
| Individual reflection | End of Tuesday, December 8 | Each member submits at most one page on MarkUs describing their actual contribution, with relevant GitHub evidence and context if needed. |
| Peer evaluation | End of Tuesday, December 8 | Each member completes the evaluation; the course will announce availability and details. |

Passing tests is not the same as measuring coverage. Do not infer 100% coverage from a test count.
Do not invent additional word limits, file formats, presentation slots or late penalties that have not been announced.

## Presentation

- The current expectation is about 15 minutes plus up to 5 minutes for questions, subject to final scheduling.
- Every member must speak. The draft rubric's Excellent delivery level additionally requires completion within 15 minutes.
- The audience includes the team's TA and another staff member; show evidence for every grading category.
- Adapt the supplied slide template rather than reproducing all 16 pages. Identify the speaker on each slide.
- Suggested timing: user stories 1 minute, API 30 seconds, persistence 30 seconds, design 6 minutes, demo 6 minutes and code quality 1 minute. These are planning suggestions, not separate mandatory time limits.
- Explain at least two substantive SOLID examples, implemented patterns and architecture diagrams. A use-case walkthrough should connect its story, before/after UI, classes, Interactor and control flow.
- A recorded demo is allowed, but working software must remain available for immediate follow-up questions. Prepare data and rehearse.
- The template's login walkthrough illustrates an acceptable example, not a guarantee of top marks.

## November 15 draft rubric: 100 points

Higher performance criteria below should not be misrepresented as the minimum passing threshold.

| Category | Points | Main evidence and stronger performance |
| --- | ---: | --- |
| Presentation Materials | 15 | Complete, readable and accurate slides; diagrams/screenshots that clarify the application and design. |
| Presentation Delivery | 15 | Clear explanations, pacing, transitions and preparation; Excellent or above requires all members to speak and completion within 15 minutes. |
| Demo | 10 | Prepared environment and an explained end-to-end demonstration that connects to member stories and design. |
| Question Answering | 5 | Correct, clear and concise answers showing knowledge of the implementation and course concepts. |
| Specification | 10 | Clear stories, use cases and MVP scope that explain behavior rather than implementation. |
| Project Scope | 5 | A substantial story for each member, appropriate scope and limited overlap. |
| API Usage | 5 | Actual API integration and an explanation of its purpose and suitability. |
| Data Persistence | 5 | Demonstrable saved data, including clear saving and loading behavior for stronger marks. |
| Clean Architecture | 10 | Effective architecture diagrams, dependency-rule evidence and justified tradeoffs. |
| Overall Design | 10 | Excellent includes two detailed SOLID examples, an appropriate implemented pattern beyond the starter and an extensibility example. Exceptional adds a suitable pattern not taught in class. |
| Code Quality | 10 | Evidence of quality checks before merging. Excellent or above requires Checkstyle or a similar tool; eliminating every style warning is not required. Strong evidence can include representative PRs. |

## Individual grade factor

The individual grade is the team grade multiplied by an individual factor starting at 1.
At most one teamwork deduction and one code-contribution deduction apply; the result cannot fall below 0.
Factors above 1 are reserved for exceptional instructor-determined circumstances.

- Minor teamwork issue: subtract 0.05, for example repeated unexplained absences, weak participation, missing reviews or poor presentation preparation.
- Major teamwork issue: subtract 0.1, for example missed commitments, repeated contract violations or persistent non-participation.
- Significant code concern: subtract 0.2 when merged PRs contain no substantial implementation of the person's own use case, **or** when one member's over-contribution undermines collaboration.
- No significant code contribution: subtract 1, such as no merged PRs or no meaningful progress on the assigned use case.
- The reflection can supply context and links to actual work when a deduction may apply. Do not fabricate contribution, review or meeting records.

## Official sources

- [Course syllabus, including the AI policy](https://q.utoronto.ca/courses/444184/assignments/syllabus)
- [Team Project hub](https://q.utoronto.ca/courses/444184/pages/team-project)
- [Project Overview](https://q.utoronto.ca/courses/444184/pages/project-overview)
- [Team Formation](https://q.utoronto.ca/courses/444184/pages/team-formation)
- [Team Contract](https://q.utoronto.ca/courses/444184/pages/team-contract)
- [Project Expectations](https://q.utoronto.ca/courses/444184/pages/project-expectations)
- [Developing Project User Stories](https://q.utoronto.ca/courses/444184/pages/developing-project-user-stories)
- [Project README and MVP](https://q.utoronto.ca/courses/444184/pages/project-readme-and-minimum-viable-product-mvp)
- [Project Timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2)
- [Final Presentation Expectations](https://q.utoronto.ca/courses/444184/pages/final-project-presentation-expectations)
- [Individual Scale Factor](https://q.utoronto.ca/courses/444184/pages/project-individual-scale-factor)
- [November 15 rubric](https://q.utoronto.ca/courses/444184/files/46148571?wrap=1)
- [Presentation template](https://q.utoronto.ca/courses/444184/files/46149397?wrap=1)
- [README template](https://q.utoronto.ca/courses/444184/files/45644698?wrap=1)
- [Official use-case format](https://github.com/CSC207-UofT/207-course-notes/blob/main/08-program-specification.md#84-use-cases)

The Quercus sources may require course login. Raw course-page archives and slide copies are not included in this repository.
