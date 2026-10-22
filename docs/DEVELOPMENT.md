# Development and team workflow

## Local checks

```sh
mvn verify
git diff --check
java -jar target/wordtrail.jar --check-api hello
java -jar target/wordtrail.jar --data-dir target/demo-data
```

Unit tests never need the public API. The HTTP adapter tests start a local server.
Swing tests run on the EDT in headless mode, drive real buttons, and render
screenshots. A CI workflow is prepared for Java 11 and 17 on Windows and Linux.
It has not been uploaded or executed remotely. This working copy is local only:
do not push to the original repository, publish elsewhere, or create a remote PR.

On Windows, `run.cmd` or `run.ps1` starts the application from the project directory.
The PowerShell launcher rebuilds when source files are newer than the JAR.
`run.ps1 -Build` forces verification; `run.ps1 -Check` checks the entry point without
a GUI or public API request.

Do not commit `target/`, `.idea/`, `.wordtrail/`, passwords, local account data or
API credentials. A `.gitattributes` file normalizes source line endings.

## Feature workflow

1. Agree on a small user story or extension and assign one lead owner plus a reviewer.
2. Update the acceptance criteria and UI sketch.
3. Start from the desired local base. Keep the current `main` unchanged for comparison.
4. Create a feature branch, e.g. `git switch -c codex/review-search`.
5. Implement the use case through its input/output boundaries; keep Swing and
   infrastructure details out of the core.
6. Add tests for observable behavior, invalid input and meaningful failure paths.
7. Run checks, inspect the diff and make focused commits with meaningful messages.
8. Review locally with `git diff main...HEAD` and the prepared review summary.
9. Record actual feedback locally and address it in new commits. Do not publish or merge without a new user request.
10. Update the demo guide and assign the next story. Do not rewrite shared history.

The current implementation uses `codex/vocabulary-learning-app` and staged feature
commits. It is an initial implementation spanning the requested stories. Do not
split or reauthor it later to imply different teammates wrote code they did not
write. Future extensions can use their own local branches and focused commits.

The personal-vocabulary/review-count/lookup extension is on the separate local
branch `codex/personal-vocabulary-review`. The initial branch is preserved as its
base. Use `git diff codex/vocabulary-learning-app...HEAD` to inspect only the extension.

## Suggested real ownership discussion

Use the README story table to assign responsibilities based on the actual team
size. One member can own more than one related story. Each member should lead a
meaningful use-case extension and its tests, explain its design, and review someone
else's work. Put actual names in the table only after agreement.

Keep meeting notes, estimates, delays and review decisions truthful and local.
`TeamContract.md` is intentionally left for the real team to discuss and sign.
The automated implementation does not satisfy attendance, respectful teamwork,
peer evaluation, team meetings or individual contribution assessment.

## Review prompts

- Can a learner reach admin operations through any controller path?
- Can another account resume someone else's state?
- Does a failed save leave memory consistent with disk?
- Are answers hidden until submission, and scores recorded once?
- Do network errors leave local study usable?
- Are views limited to display models and controller calls?
- Can the author explain the design and alternatives?

## Local review materials

The summary is `docs/PR_DESCRIPTION.md`; the file name indicates its review format,
not a published pull request. View the complete implementation and history locally:

```sh
git diff main...HEAD
git log --oneline main..HEAD
```

An earlier push failed because GitHub authentication was unavailable, so no branch
was uploaded. The user then explicitly required local-only delivery. Do not retry
publishing. The course's remote PR/team-review requirement remains unperformed;
local review documents do not claim to satisfy that historical process.
