WordTrail adds a runnable Java 11 / Swing vocabulary-learning application to the
Maven skeleton. Learners can register and sign in, browse and search vocabulary,
study flashcards, take four-choice quizzes, save review words and inspect their
progress. A first-run administrator can add vocabulary and quiz questions.
Account-specific progress persists in local JSON snapshots.

Personal vocabulary and definition overrides belong to the signed-in account.
Flashcards and new generated quizzes use that account's content, while an active
quiz retains its starting snapshot. Review counts track completed card visits,
daily study totals count distinct words, and vocabulary tables support review
count and recent-review ordering. Live dictionary lookup uses Free Dictionary
API with Datamuse as a fallback, retains the responding provider's source, and
keeps online results separate from saved definitions.

Entities and use cases are separated from Swing, HTTP and JSON adapters. The
application wires input interfaces/interactors, repository and service ports,
eight specific controller/presenter pairs and plain view models through a composition root. Salted
PBKDF2 hashes protect stored passwords, admin mutations check the role in the use
case, and account changes invalidate active decks and quizzes.

Architecture limitation: interactors return results to the controller, which
invokes the presenter. The implementation does not provide a separate
`OutputBoundary` callback interface for each use case. This design and the other
course deliverables still require review against the course criteria; the code
and passing tests alone do not establish full course compliance.

The delivery adds formal use-case specifications, accurate MVP assignments,
dedicated feature adapters and reproducible quality reports. The existing history
is retained rather than rewritten to imply additional authors.

Validation actually performed on November 17, 2026 in the delivery worktree:

- `mvn -B --no-transfer-progress verify`: **47 tests passed**, zero failures,
  errors or skips; the executable `target/wordtrail.jar` was built.
- Tests included two architecture checks, four headless Swing workflow tests,
  business rules, persistence and local HTTP/test-double dictionary cases.
- JaCoCo: **445/445 lines, 100%** across all Entity and Use Case classes, including ports.
- Checkstyle: 87 Java files checked; **661 findings retained**, with a non-blocking report.
- Final packaged-JAR diagnostics: `--help` passed and `--check-api hello`
  returned a live definition through
  the primary provider, with source
  `https://api.dictionaryapi.dev/api/v2/entries/en/hello`.

The subsequent [GitHub Actions run for e0214f3](https://github.com/fanfan170721/team-project/actions/runs/44185870260)
passed all four jobs on Ubuntu/Windows with Java 11/17. No native GUI or
teammate-machine IDE setup was manually verified. More detail is in
`docs/VERIFICATION.md`.

This is an AI-assisted initial implementation requested by the user, with its
original authorship history retained. It is not evidence that other teammates
implemented these use cases. The team-contract PR #1 has been merged; this
application code PR provides the baseline for review. **UC-09 CSV export and UC-10
daily review planning remain pending MVP work** for the designated teammates.
Those features, their implementation PRs and the group's presentation are not
claimed complete by this change. Ownership, review and individual contributions
must be recorded according to the team's actual work.
