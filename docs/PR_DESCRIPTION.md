Local review summary only. No remote PR has been created; this project must remain local.

WordTrail adds a complete Java 11 / Swing vocabulary learning application to the
existing Maven skeleton. Users can create accounts, browse topics, study
flashcards, take multiple-choice quizzes, save review words and inspect persisted
learning history. A first-run administrator can add vocabulary and quiz questions
through forms.

The application separates entities, eight use-case boundaries/interactors,
repository and service ports, controllers/presenters, infrastructure adapters and
Swing views. It uses the Free Dictionary API for live definitions and atomic local
JSON snapshots for persistence. Passwords are salted PBKDF2 hashes, ordinary
registration creates learners, and admin mutations enforce authorization in the
use case. A session version invalidates decks/quizzes across account changes.

The personal-vocabulary extension lets learners add private words and override
their own definitions, with those definitions used in flashcards and generated
quizzes. Each completed card visit records a review count and last-review instant;
tables support numeric count ordering and recent-first ordering. Daily word totals
remain deduplicated. Primary dictionary HTTP 530 failures now try Datamuse as another
live provider, preserve its source and keep saved definitions separate from online results.

Validation: `mvn verify` passes 38 tests, including a real Swing button workflow
covering all seven stories and restart persistence; local HTTP tests cover API
responses/failures. The executable JAR includes dependencies and seed content.
CI is configured for Windows/Linux and Java 11/17 but has not been published or run
remotely. The README includes actual component screenshots, setup
instructions, architecture notes, acceptance criteria and a demo guide.

The packaged JAR's live diagnostic also successfully fetched definitions for
`climate` and `accommodation` through Datamuse after primary HTTP 530 failures on
2026-10-22. Existing version-1 saved data is compatible with optional new fields.

This is an AI-assisted initial implementation requested by the user. Story owner
names and the existing team contract are left for the real team to agree. Human
review and individual team contributions remain to be completed.
