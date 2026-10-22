# WordTrail implementation plan

The existing repository is a Java 11 Maven skeleton. Build a small Swing vocabulary
learning application with all seven requested stories. Preserve the team's contract
template; only the actual team can agree to and sign that document.

## Planned increments

1. Domain entities, repository ports, atomic JSON persistence and password hashing.
2. Seven use cases, authentication/authorization, dictionary integration and tests.
3. Swing screens, controllers, presenters and an executable JAR.
4. Integration checks, CI, architecture notes, wireframes and demonstration guide.

Use the feature branch `codex/vocabulary-learning-app` and meaningful commits.
Do not manufacture team contributions or review approvals. Keep the PR description
and checklist as local review materials. The user's final instruction is local-only
delivery: do not push any branch or create a remote PR, even if authentication becomes available.

## Decisions to validate

- Local JSON meets persistence; Free Dictionary API meets external API usage.
- The supplied course DAO is reference material, not a dependency. Its shared HTTP
  server and cleartext password model are unnecessary for this local application.
- The first-run setup creates an administrator, subsequent registration creates
  learners. Use cases enforce the role even if the GUI hides admin controls.
- UI calls use cases through controllers and presenters. Business logic has no
  Swing, JSON or HTTP dependencies. Network and disk work run off the Swing EDT.
- Seed content works offline. API lookup explicitly reports failures and never
  silently presents local content as an API response.
