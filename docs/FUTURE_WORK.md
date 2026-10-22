# Future extensions

These are proposals, not currently implemented features.

| Extension | Why | Likely design |
| --- | --- | --- |
| Spaced repetition | Review at useful intervals | Scheduling policy interface with a simple Leitner strategy first |
| Archive words / edit more fields | Extend the implemented personal-definition editor | Stable IDs, content commands and archived status |
| More quiz types | Test recognition and recall differently | Question strategy or polymorphic question entities |
| SQLite storage | Avoid rewriting growing histories | Implement existing repository ports with transactions |
| Remote sync | Share accounts/progress across devices | Authenticated HTTPS service and conflict policy |
| Dictionary caching | Reduce repeated network calls | Gateway decorator with expiry and explicit cache status |
| Backup provider key configuration | Datamuse announces key requirements in 2027 | Follow the provider's updated key contract, keeping keys out of saved source URLs |
| Password recovery | Restore access after forgotten passwords | Requires trusted recovery channel / real server identity |
| Accessibility and localization | Support more users | Resource bundles, keyboard shortcuts and screen-reader testing |
| Export progress | Share a learning summary | Export use case and CSV/file adapter |
| Output boundaries per story | Isolate growing presentation needs | Dedicated response DTOs and presenter output ports |

The current snapshot intentionally keeps the UI and storage small enough for
students to understand and modify.
