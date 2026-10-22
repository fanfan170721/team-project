WordTrail adds a complete Java 11 / Swing vocabulary learning application to the
existing Maven skeleton. Users can create accounts, browse topics, study
flashcards, take multiple-choice quizzes, save review words and inspect persisted
learning history. A first-run administrator can add vocabulary and quiz questions
through forms.

The application separates entities, seven use-case boundaries/interactors,
repository and service ports, controllers/presenters, infrastructure adapters and
Swing views. It uses the Free Dictionary API for live definitions and atomic local
JSON snapshots for persistence. Passwords are salted PBKDF2 hashes, ordinary
registration creates learners, and admin mutations enforce authorization in the
use case. A session version invalidates decks/quizzes across account changes.

Validation: `mvn verify` passes 25 tests, including a real Swing button workflow
covering all seven stories and restart persistence; local HTTP tests cover API
responses/failures. The executable JAR includes dependencies and seed content.
CI is configured for Windows/Linux and Java 11/17. See the published Actions runs
for remote results. The README includes actual component screenshots, setup
instructions, architecture notes, acceptance criteria and a demo guide.

The packaged JAR's live diagnostic also successfully fetched a definition from
`https://api.dictionaryapi.dev/api/v2/entries/en/hello` on 2026-10-22.

This is an AI-assisted initial implementation requested by the user. Story owner
names and the existing team contract are left for the real team to agree. Human
review and individual team contributions remain to be completed.
