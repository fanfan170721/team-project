# WordTrail implementation plan

The existing AI-assisted Java/Swing baseline implements eight stories. The signed
contract is integrated from `main`. The current delivery work aligns the code,
documentation and validation with the course requirements verified on November 17.

## Planned increments

1. Preserve the baseline history and signed contract in `Emilia/project-delivery`.
2. Add formal specifications, accurate ownership/status and entity documentation.
3. Separate feature controllers/presenters without changing existing behavior.
4. Measure the full Entity/Use Case line coverage; cover real missing failure paths.
5. Run Checkstyle, retain its findings and publish reviewable code and CI evidence.
6. Fuxuan implements MVP CSV export (UC-09); Xinyu implements the daily review
   plan (UC-10), each with their own interfaces, adapters, UI, tests and PR.
7. Review and integrate those PRs, rerun all checks and prepare the group presentation.

Use meaningful commits and actual review evidence. Initial AI-assisted code remains
attributed to its original lead; do not manufacture teammates' contributions or
retroactively claim that the baseline followed a process it did not follow.

## Decisions to validate

- Local JSON meets persistence; Free Dictionary API meets external API usage.
- The supplied course DAO is reference material, not a dependency. Its shared HTTP
  server and cleartext password model are unnecessary for this local application.
- The first-run setup creates an administrator, subsequent registration creates
  learners. Use cases enforce the role even if the GUI hides admin controls.
- UI calls use cases through feature controllers and presenters. Business logic has no
  Swing, JSON or HTTP dependencies. Network and disk work run off the Swing EDT.
- Seed content works offline. API lookup explicitly reports failures and never
  silently presents local content as an API response.
