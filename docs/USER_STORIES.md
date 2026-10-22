# User stories and acceptance criteria

All lead owners are **unassigned until the actual team confirms them**. A story can
be used as a starting point for a teammate's extension and review. Do not record
AI-generated commits as proof that several people independently wrote these stories.

## US-01 — Accounts

As a user, I want to create an account and log in so my progress and saved words
are stored.

- On the first run, set up an administrator with matching passwords.
- On later runs, log in or register a learner; usernames are case insensitive.
- Reject invalid usernames, short passwords, duplicate usernames and incorrect credentials.
- Clear the session on logout. A new login does not resume a previous quiz/deck.
- Persist accounts as salted password hashes, and isolate learning records by account.

Flow: open app → select action → enter username/password (confirm when creating)
→ submit → vocabulary screen. Errors appear in the status area and remain on the form.

Tests: `AuthInteractorTest`, `Pbkdf2PasswordHasherTest`, `SwingWorkflowTest`,
`JsonFileStoreTest`. Lead owner: team to assign.

## US-02 — Topic vocabulary

As a learner, I want to view vocabulary grouped by topic so I can study in context.

- Show all words initially; filter by topic using a selector.
- Search term or definition without case sensitivity.
- Selecting a row shows the definition, example and source.
- Show a useful empty state for unmatched searches.
- Online lookup invokes the external dictionary and clearly reports service failures.

Flow: Vocabulary → select topic / enter search → select row → read details or
request online definition. Lookup alone does not overwrite saved vocabulary.

Tests: `LearningUseCasesTest`, `DictionaryApiClientTest`, `SwingWorkflowTest`.
Lead owner: team to assign.

## US-03 — Flashcards

As a learner, I want to study word definitions using flashcards to memorize them.

- Start a randomized deck from a topic or personal review list.
- Initially show only the word; reveal shows the definition and example.
- Require reveal before marking studied.
- Count a word once per local day and once in the all-time distinct total.
- Separately count each intentionally completed card visit, even on the same day;
  reject duplicate completion of one visit and allow retry after a failed write.
- Move to the next card; the deck loops after the last card.
- Report empty decks without damaging progress.

Flow: Flashcards → choose topic/review filter → start → recall → reveal → mark
studied → next. Saving/removing a review word is also available on a card.

Tests: `LearningUseCasesTest`, `AppPresenterTest`, `SwingWorkflowTest`.
Lead owner: team to assign.

## US-04 — Multiple-choice quiz

As a learner, I want to take a multiple-choice quiz to test my vocabulary knowledge.

- Choose a topic and 1–20 questions; use fewer when the topic has fewer questions.
- Shuffle question order and answer order; every question has four distinct choices.
- Do not reveal the correct answer before submission.
- Reject missing choices, duplicate answers and advancing before answering.
- Show answer feedback and a final score.
- Save exactly one result on completion; abandoned quizzes do not count.
- Allow retry if the final disk write fails, without double-counting.

Flow: Quiz → select topic/count → start → choose answer → submit → read feedback
→ next → final score saved. Restart abandons the previous unfinished quiz.

Tests: `QuizInteractorTest`, `SwingWorkflowTest`. Lead owner: team to assign.

## US-05 — Review later

As a learner, I want to save difficult words to a personal review list.

- Save from vocabulary, a flashcard, or the current quiz question.
- Repeated saves do not duplicate entries.
- Remove a word from vocabulary, flashcards or the review list.
- Other accounts cannot see this account's saved list.
- Restore the list after restart and allow a review-only flashcard deck.

Tests: `LearningUseCasesTest`, `JsonFileStoreTest`, `SwingWorkflowTest`.
Lead owner: team to assign.

## US-06 — Progress over time

As a learner, I want to track my progress so I can see improvement.

- Show distinct studied words versus current vocabulary size, and saved word count.
- Show total completed reviews; vocabulary and review tables show per-word counts
  and last-review times with numeric count sorting and recent-first sorting.
- Show a 14-day chart, including zeroes for inactive days.
- Show a consecutive daily study streak, allowing a streak ending yesterday.
- Show dated quiz results, newest first, and weighted overall answer accuracy.
- Display a clear state when there are no completed quizzes.
- Restore progress after restart; each account has its own history.

Tests: `LearningUseCasesTest`, `AppPresenterTest`, `SwingWorkflowTest`.
Lead owner: team to assign.

## US-08 — Personal vocabulary and definitions (requested extension)

As a learner, I want to add my own vocabulary and revise my definitions without
changing another learner's content.

- Vocabulary provides Add my word and Edit my definition with an inline Save/Cancel form.
- New words have a topic, required definition, optional example and source.
- Changes are private to the authenticated account and persist after restart.
- Reject empty fields, duplicate word/topic combinations and unrelated private word IDs.
- Browsing, search, review lists, flashcards, word totals and new quizzes use personal content.
- Generate questions for personal/overridden words with three distinct distractors,
  shuffle choices and retain correct scoring. Existing active quizzes keep their snapshot.
- Online lookup shows an actual provider URL and the unchanged saved definition.
  If the primary fails (including HTTP 530), try the live backup. If both fail,
  keep local content usable and report the failure.

Tests: `PersonalVocabularyInteractorTest`, `LearningUseCasesTest`, `QuizInteractorTest`,
`JsonFileStoreTest`, `ResilientDictionaryGatewayTest`, `SwingWorkflowTest`.
Lead owner: team to assign.

## US-07 — Admin content forms

As a content admin, I want to add new vocabulary words and quizzes through a form.

- Only the administrator sees Admin; the use case independently enforces the role.
- Add a word with term, existing/new topic, definition, optional example and source.
- Optionally populate definition/example/source from the dictionary, then edit before saving.
- Reject empty required fields, oversized text and duplicate terms within one topic.
- Link a new question to an existing word; require a prompt, four distinct choices
  and a valid correct-answer selection.
- Added content survives restart and appears in vocabulary and topic quizzes.
- Preserve existing topic capitalization when a matching topic is entered.

Tests: `ContentInteractorTest`, `JsonFileStoreTest`, `SwingWorkflowTest`.
Lead owner: team to assign.
