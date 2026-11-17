# Use-case specification

These specifications describe user-observable behavior. Implementation classes
belong in `ARCHITECTURE.md`. UC-01 through UC-08 are the existing AI-assisted
baseline led by Zidong Yuan. UC-09 and UC-10 are assigned, **unimplemented MVP
work** for Fuxuan Xu and Xinyu Huang respectively; no personal contribution is
claimed until the work and review actually occur.

## UC-01: Sign in to my learning account

- **Primary actor:** Registered learner or administrator.
- **Preconditions:** The application is open and the actor has an account.
- **Trigger:** The actor requests access using a username and password.
- **Success outcome:** The actor accesses their own saved vocabulary and history.
- **Main success scenario:**
  1. The actor supplies account credentials.
  2. The system checks them against the stored account.
  3. The system begins a session and displays that account's learning area.
- **Extensions:** 2a. Invalid credentials: explain the failure and remain signed
  out. 2b. Account data cannot be read: explain the problem and preserve it.
  3a. A previous user was active: discard that user's unfinished session before
  showing the new account.

First-run administrator setup, ordinary learner registration and sign-out are
implemented supporting account operations. Sign-in itself does not create an account.

## UC-02: Find a word and inspect its meaning

- **Primary actor:** Signed-in learner.
- **Preconditions:** Vocabulary is available to the account.
- **Trigger:** The learner requests vocabulary matching a topic or search text.
- **Success outcome:** The learner sees matching words, a chosen meaning and its source.
- **Main success scenario:**
  1. The learner specifies a topic and/or search text.
  2. The system lists matching words visible to that account.
  3. The learner selects a word.
  4. The system shows the saved definition, example and source.
- **Extensions:** 2a. No matches: show an empty result. 4a. The learner requests an
  online definition: show the external provider's result and attribution separately
  from the saved meaning. 4a1. Both providers fail: explain the failure and leave
  local vocabulary unchanged and usable.

## UC-03: Complete a flashcard review

- **Primary actor:** Signed-in learner.
- **Preconditions:** At least one word is available in the selected study set.
- **Trigger:** The learner begins reviewing a selected topic or saved-word set.
- **Success outcome:** A deliberately completed review is recorded for the account.
- **Main success scenario:**
  1. The learner selects a study set.
  2. The system presents a word with its meaning hidden.
  3. The learner recalls the meaning, then requests its reveal.
  4. The system shows the meaning and example.
  5. The learner marks this review complete.
  6. The system saves the review and offers another card.
- **Extensions:** 1a. Empty set: explain and do not change progress. 5a. Meaning
  has not been revealed: require that step first. 5b. Same visit already counted:
  prevent a duplicate. 6a. Saving fails: report it and allow a safe retry. A new
  visit to the word can count another review; daily distinct-word totals stay deduplicated.

## UC-04: Complete a vocabulary quiz

- **Primary actor:** Signed-in learner.
- **Preconditions:** The chosen topic has eligible questions.
- **Trigger:** The learner requests a quiz of 1–20 questions.
- **Success outcome:** The learner receives feedback and one saved final result.
- **Main success scenario:**
  1. The learner chooses a topic and requested question count.
  2. The system presents a randomized question with four distinct choices.
  3. The learner selects and submits an answer.
  4. The system explains whether it is correct.
  5. The learner continues; steps 2–4 repeat for remaining questions.
  6. The system saves the final result and displays the score.
- **Extensions:** 1a. Invalid count or no eligible questions: explain without
  starting. 2a. Fewer questions exist: use the available number. 3a. No choice or
  a repeated submission: reject without advancing. 5a. Abandonment: do not save a
  completed result. 6a. Save fails: allow retry without recording twice.

## UC-05: Maintain a private review list

- **Primary actor:** Signed-in learner.
- **Preconditions:** The chosen word is visible to the account.
- **Trigger:** The learner asks to save a difficult word for later review.
- **Success outcome:** The word appears once in the account's persistent review list.
- **Main success scenario:**
  1. The learner identifies a word and requests to save it.
  2. The system records its membership in that account's review list.
  3. The system displays the updated list, available for later flashcard study.
- **Extensions:** 1a. The learner requests removal: remove the membership.
  2a. Already saved: preserve a single entry. 2b. Save fails: report failure and
  keep the previous list. Other accounts cannot inspect this private list.

## UC-06: Inspect learning progress

- **Primary actor:** Signed-in learner.
- **Preconditions:** The account exists; an empty history is valid.
- **Trigger:** The learner asks to see learning progress.
- **Success outcome:** The learner sees an accurate summary of their own history.
- **Main success scenario:**
  1. The learner requests the progress view.
  2. The system summarizes distinct studied words, total reviews, saved words,
     recent daily study, streak and completed quiz results.
  3. The learner inspects the summary and newest-first quiz history.
- **Extensions:** 2a. No study or quiz history: show zeros and an explicit empty
  state. 2b. Different quiz lengths: calculate overall accuracy using all answered
  questions, not an unweighted average of quiz percentages. 2c. Reading history
  fails: explain the problem rather than displaying invented progress.

## UC-07: Add learning content

- **Primary actor:** Administrator.
- **Preconditions:** The actor is signed in with the administrator role.
- **Trigger:** The administrator requests a new shared vocabulary entry.
- **Success outcome:** Valid new content is available to learners after restart.
- **Main success scenario:**
  1. The administrator supplies a term, topic and definition, with optional example/source.
  2. The system validates and stores the word.
  3. The administrator optionally supplies a linked question and four distinct choices.
  4. The system stores valid question content and makes it available for quizzes.
- **Extensions:** 1a. Online lookup supplies a suggestion: the administrator can
  edit it before saving. 2a. Missing, oversized or duplicate input: explain and
  save nothing. 3a. No question requested: finish with the saved word. 4a. Invalid
  choices or answer index: reject the question. Any unauthorized attempt is rejected.

## UC-08: Maintain my personal vocabulary (MVP; Zidong Yuan)

- **Primary actor:** Signed-in learner.
- **Preconditions:** The account exists. Editing requires a word visible to that account.
- **Trigger:** The learner asks to add a personal word or revise a personal definition.
- **Success outcome:** The account's vocabulary reflects the change and survives restart,
  without changing another learner's vocabulary.
- **Main success scenario:**
  1. The learner supplies a word, topic and meaning, with optional example/source.
  2. The system validates the entry and saves it to the learner's personal vocabulary.
  3. The system shows the new entry in browsing and makes it available for learning.
- **Extensions:** 1a. The learner edits a visible word's meaning: save a private
  definition override. 1b. The learner cancels: change nothing. 2a. Empty, duplicate
  or unauthorized data: explain and preserve existing vocabulary. 2b. Save fails:
  report failure without publishing a partial change. 3a. A quiz was already in
  progress: keep that quiz's original question snapshot; new quizzes use the new meaning.

## UC-09: Export my quiz results (MVP; Fuxuan Xu; pending)

- **Primary actor:** Signed-in learner.
- **Preconditions:** The account is active; a history with no completed quizzes is valid.
- **Trigger:** The learner requests a portable copy of completed quiz results.
- **Success outcome:** A UTF-8 CSV contains only that account's completed results.
- **Main success scenario:**
  1. The learner selects a destination and confirms the export.
  2. The system obtains the learner's completed results in chronological order.
  3. The system writes completion time, topic, correct answers and total questions.
  4. The system reports where the file was saved and how many results it contains.
- **Extensions:** 1a. Cancel: write nothing. 1b. Existing file: obtain overwrite
  confirmation before replacing it. 2a. Empty history: export a header and report
  zero results. 3a. Commas, quotes or line breaks in a topic: preserve them with
  valid CSV quoting. 3b. Writing fails: show failure, never success; preserve existing
  files when the write cannot complete. Another account's results are never exported.

Acceptance includes empty history, account isolation, chronological sorting, CSV
escaping, cancellation, overwrite refusal and write failure. This is planned
behavior and does not yet have implementation or completed tests.

## UC-10: Generate today's review plan (MVP; Xinyu Huang; pending)

- **Primary actor:** Signed-in learner.
- **Preconditions:** The account is active; its visible vocabulary and review history can be read.
- **Trigger:** The learner requests a review plan for a topic and a limit of 1–20 words.
- **Success outcome:** The learner sees due words and why they are due; generating
  the plan does not mark any word as studied.
- **Main success scenario:**
  1. The learner specifies the topic and desired maximum number of words.
  2. The system evaluates visible words using the current local date and review history.
  3. The system selects never-reviewed words first, then the earliest due words,
     with alphabetical tie-breaking, up to the limit.
  4. The system shows the words, personal meanings, due dates and selection reasons.
- **Extensions:** 1a. Invalid limit: explain and generate no plan. 2a. A word was
  never reviewed: it is eligible today. 2b. A word was reviewed once/twice/at least
  three times: it becomes due 1/3/7 local calendar days after its last review.
  3a. No words are due: show an explicit empty result. 4a. A personal meaning exists:
  use it; another account's private words are excluded.

Acceptance includes fixed-date due/not-due boundaries, topic and limit behavior,
private-word isolation, personal meanings and unchanged study counters. This is
a simple disclosed scheduling policy, not a claim of a mature adaptive algorithm.
