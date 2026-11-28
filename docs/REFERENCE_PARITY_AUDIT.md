# Reference comparison — November 27, 2026

Compared the team implementation at `ae03434`, the Java reference and the Python
application in `Vocabulary-learning-software`. This is a product comparison,
not a statement that every reference feature was part of the team's original MVP.
UC-09 CSV export and UC-10 due-review planning have their own merged implementation
histories. Most missing learning rules predate those contributions.

| Area | Team baseline | Reference behavior / required correction |
| --- | --- | --- |
| Accounts and private content | Implemented, tested | Retain account isolation; Python is a single local profile |
| Library / personal definitions | Implemented, tested | Retain private edits and lookup that cannot overwrite saved meanings |
| Flashcards | Reveal/review counts implemented | Not equivalent to a complete 20-word learning round |
| Quiz | 1–20 questions, final score only | Add a separate fixed 20-word learning loop with explicit unknown answers |
| Interrupted learning | Active quiz is only in memory | Persist questions, options, answer history and current step after every action |
| Error recovery | No wrong-only retest | Relearn/retest only incorrect or unknown words until all are correct |
| Distractors | Generates from visible definitions | Retests must retain the original four-option question snapshots |
| Already-known words | Missing | Mark in overview; all 20 become mastered but only genuinely new words advance goals |
| Mastery / goals | Study counts, not mastery | Separate unique mastery, net-new learning, practice totals and goal baseline |
| Mistake notebook | Manual saved list only | Keep error/unknown counts after eventual correction |
| History replay | Scores only | Preserve original questions; replay cannot inflate mastery or practice totals |
| Placement / reassessment | Missing | Resume assessment; explicitly adopt result and immediately replace unfinished round |
| Vocabulary scale | 16 sample words | Java reference has 60 English words / 3 sample levels; Python has 1080 lemmas / 15 levels |
| Pronunciation | Missing | Reference prefers recorded audio, explicitly labels local fallback, never auto-plays |
| Progress | Distinct study days, streak, quiz history | Preserve these; expose guided-study mastery and goal measures separately |
| CSV export | Implemented UC-09 | Preserve account filtering, cancellation, escaping and safe replacement |
| Due-review plan | Implemented UC-10 | Preserve ordering, date boundaries, private definitions and no-write generation |
| Backup / restore | Manual data-folder backup | Python has validated import/export; CSV scores are not a full backup |
| Onboarding | Account creation | Python has level/self-assessment and goal onboarding |
| Rewards / motion | Missing | Java has skippable animation; Python also has practice achievements |
| Presentation | Library/learning UI updates in PRs #15/#16 | Remaining screens and navigation still differ from the reference home/module layout |
| Architecture / tests | Java/Swing, API, persistence, dedicated MVP boundaries | Keep dependency direction, genuine review and existing coverage gates |

## Delivery order

1. Restore a coherent guided-study loop, durable state, honest counting, mistakes,
   replay, goals and immediate reassessment replacement as one reviewed increment.
2. Add recorded pronunciation and explicit audio controls using the Java reference
   behavior; validate with silent audio doubles before any user-triggered playback.
3. Curate the larger English content bank and stronger assessment rules. The Python
   bank contains Chinese definitions; copying it verbatim would violate the team's
   English-only project requirement. Sample levels must not be advertised as CEFR
   levels, exam coverage or a calibrated vocabulary-size estimate.
4. Add validated backup/restore, onboarding, achievements and remaining UI polish.

The Python roadmap also lists unimplemented reading/writing/AI/3D features. They
are not completed reference behavior and are not claimed as delivered here.

## Implemented on the preview branch — November 28

The first delivery above is implemented: durable 20-word rounds, known-word
selection, learn/test/wrong-only review, persistent mistakes, mastery/net-new
goals, non-inflating history replay and immediate placement adoption. Completion
connects to the existing private library, progress, Review later and CSV results.
See [the use cases and verified behavior](GUIDED_STUDY.md). Pronunciation, expanded
English content, stronger assessment, backup/restore and rewards remain open.

Repeated `W` strings found in the earlier UI preview were long-text stress
fixtures, not curriculum. The checked original local profile contained no such
word or topic on November 28. The two placeholder screenshots have been removed
from the user-facing preview; the regression tests remain. No existing learning
feature or teammate-owned MVP use case is removed in this increment.
