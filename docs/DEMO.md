# Presentation and demonstration guide

## Preparation

Run `mvn verify` and use a fresh folder for a repeatable demo:

```sh
java -jar target/wordtrail.jar --data-dir target/presentation-data
```

Choose your own admin credentials. Do not display a personal password. Existing
demo folders retain their records, so use a new path when you want a fresh setup.

## Five-minute demo

1. **Accounts (30 seconds).** Set up an admin, log out, register a learner. Explain
   that vocabulary is shared and progress belongs to the logged-in account.
2. **Vocabulary and API (45 seconds).** Filter to Nature and search climate. Select
   the word to show the local definition. Use online lookup and identify the API
   URL in the source. If offline, show the explicit error and continue locally.
3. **Review and flashcards (45 seconds).** Save climate. Open Review later, then
   Flashcards with review-only enabled. Reveal, mark studied, and explain why
   reveal alone does not count as study.
4. **Quiz (45 seconds).** Choose Nature and two questions. Answer and show feedback.
   Complete the quiz to persist the score; explain that an abandoned quiz is excluded.
5. **Progress and persistence (45 seconds).** Show today's bar, distinct word count,
   streak, weighted accuracy and quiz history. Close, relaunch with the same data
   folder and log in to show the records survive.
6. **Admin (45 seconds).** Log in as admin. Add `resilient` under `Mindset` with
   definition `Able to recover after difficulty.` Add a linked question with four
   different choices, with that definition as the correct answer. Show that Mindset
   now appears in vocabulary and quiz topic selectors.
7. **Design (45 seconds).** Show the architecture diagram and one boundary,
   interactor, adapter and test. Discuss one tradeoff and one extension.

## Design questions to prepare

- Why Swing? It meets the desktop requirement and has no web server to deploy.
- Why JSON? It is transparent and sufficient for a small local app. SQLite would
  improve scale and transactional querying.
- Why another API instead of copying the attached course DAO? A dictionary directly
  supports vocabulary learning; local persistence is sufficient for this app.
- Where are SOLID and patterns used? See `ARCHITECTURE.md` for specific classes.
- How are failures tested? Show duplicate quiz submission, final-save retry,
  corrupted JSON preservation, and HTTP error tests.
- What would you improve? Spaced repetition, word editing, richer question banks,
  accessibility/localization, and remote sync through new repository adapters.

## Evidence to show

For this local-only delivery, show the local commit history, test results and
review notes. No remote PR discussion or CI result exists. The initial AI-assisted
implementation and screenshots are useful software evidence, but do not substitute
for team members' own contributions or design explanations.
