# UI wireframes

These are design sketches accompanying this implementation, not a claim that a
team meeting or earlier design milestone occurred. The actual team can annotate
them during its design discussion.

## Authentication

```text
+----------------------------------------------------------+
| WordTrail                                                |
| Welcome / set up this device's initial administrator      |
| Action       [Log in / Register learner v]                |
| Username     [________________________]                  |
| Password     [************************]                  |
| Confirm      [************************] (creation only)   |
| [Log in / Create learner / Create administrator]          |
| Status / input error                                     |
+----------------------------------------------------------+
```

Setup is used only for a fresh data directory. Regular registration does not offer
a role selector.

## Main window

```text
+------------------------------------------------------------------+
| WordTrail / username (administrator)                     [Log out] |
| Vocabulary | Flashcards | Quiz | Review later | Progress | Admin  |
|                                                                  |
| Page title and short instruction                                 |
|                                                                  |
| [page controls]                                                  |
|                                                                  |
| [page content / forms / chart]                                    |
|                                                                  |
| [page actions]                                                   |
| Status: Ready / Saving... / meaningful error                      |
+------------------------------------------------------------------+
```

Admin is omitted for learners. Tabs preserve an in-progress quiz/deck during one
login. Selecting a tab refreshes its topic lists or persisted data.

## Vocabulary / review list

```text
Topic [All topics v]  Search [____________] [Search] [Refresh]
+------------+------------+----------------------------+--------+
| Word       | Topic      | Definition                 | Saved  |
| climate    | Nature     | ...                        | Yes    |
+------------+------------+----------------------------+--------+
Word / topic
Definition, example, source
[Save / Remove review word]  [Look up selected word online]
```

Review later uses the same view without search controls and shows only saved
words. The split pane can be resized to enlarge the details.

## Flashcards

```text
Topic [All topics v]  [ ] Review list only  [Start / restart]
+--------------------------------------------------------------+
| Card 1 of 4 / Travel                                         |
|                                                              |
| itinerary                                                    |
|                                                              |
| Hidden until revealed: definition / example / source          |
+--------------------------------------------------------------+
[Reveal definition] [Mark studied] [Next card] [Save / Remove]
```

Mark studied is enabled after reveal and disabled after that word is recorded
for today.

## Quiz

```text
Topic [Travel v]  Questions (up to) [5]  [Start / restart]
Question 1 of 4
What does itinerary mean?
( ) Choice 1   [wrapping, scrollable answer text]
( ) Choice 2   [wrapping, scrollable answer text]
( ) Choice 3   [wrapping, scrollable answer text]
( ) Choice 4   [wrapping, scrollable answer text]
Feedback / final score
[Submit answer] [Next question] [Save this word for review]
```

Answer controls lock after submission. The last submission saves the final score.

## Progress / administration

Progress has a summary, fourteen daily bars, and a dated quiz-history table.
An empty history is explicitly described. Admin has two inner tabs: one word form
and one question form. Both forms scroll when the window is small.

Screenshots of the implemented screens are in `docs/screenshots/`. The integration
test renders the same real components into `target/ui-snapshots/`.
