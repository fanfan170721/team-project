# Development and team workflow

## Project language

Use English for code, comments, UI text, project documentation, review descriptions
and test reports. Chinese is allowed only for teammate communication in these files:

- `docs/TEAM_TASKS.zh-CN.md`
- `docs/FIRST_TASK_FUXUAN.zh-CN.md`
- `docs/FIRST_TASK_XINYU.zh-CN.md`
- `docs/START_HERE.zh-CN.md`

## Local checks

```sh
mvn clean verify
git diff --check
java "-Djava.awt.headless=true" -jar target/wordtrail.jar --help
java "-Djava.awt.headless=true" --class-path target/wordtrail.jar tools/VerifyPackage.java target/package-check
```

Unit tests never need the public API. HTTP tests start a local server. Swing tests
run on the EDT in headless mode, drive real buttons and render screenshots. CI is
configured for Java 11 and 17 on Windows and Linux; consult the actual PR checks
for remote results.

The package check runs against the executable JAR rather than Maven's test
classpath. It creates an isolated temporary profile, verifies the packaged seed
data and JSON dependency, saves a personal word and reopens the application to
check persistence. It deletes its own test profile and writes
`target/package-check/result.txt`. It does not open a native window or access the
public API. Keep the `-Djava.awt.headless=true` argument quoted in PowerShell.

On Windows, run `powershell -NoProfile -File tools/Test-Launcher.ps1` to check
launcher rebuild decisions with isolated fake Java/Maven commands. The harness
does not open the application or alter the real project sources.

Manual native GUI and public API checks are separate, opt-in checks:

```sh
java -jar target/wordtrail.jar --check-api hello
java -jar target/wordtrail.jar --data-dir .wordtrail/demo
```

`mvn verify` also generates JaCoCo coverage and a Checkstyle report. Inspect
`target/site/jacoco-course-layers/index.html` for the complete Entity and Use Case
layers, and `target/site/checkstyle.html` for style findings. Coverage is measured,
not inferred from the number of passing tests. Checkstyle findings are reported
without suppressing them merely to make the build look clean.

On Windows, `run.cmd` or `run.ps1` starts the application from the project directory.
The PowerShell launcher compares build-input fingerprints so additions, edits
and deletions trigger a clean build. Its fingerprint lives under ignored `target/`.
The saved fingerprint also includes the JAR hash, detecting external builds that
replace the executable without updating the launcher's input record.
`run.ps1 -Build` forces `clean verify`; `run.ps1 -Check` checks the entry point without
a GUI or public API request.

Keep persistent profiles outside `target/`, which `clean` removes. Before
rebuilding, the launcher checks for `wordtrail.json` beneath `target/` and stops
if it finds stored data or cannot safely inspect the directory. Normal launches
also reject an explicit `-DataDirectory` inside `target/`, even before it exists.
The launcher does not move or delete profiles. Close WordTrail, make and check a
backup, then move an old demo profile out of `target/` before rebuilding. Direct `mvn clean` commands
and direct JAR launches bypass this launcher protection.

Do not commit `target/`, `.idea/`, `.wordtrail/`, passwords, local account data or
API credentials. A `.gitattributes` file normalizes source line endings.

## Feature workflow

1. Use the README's assigned MVP use cases; identify a reviewer for each increment.
2. Update the acceptance criteria and UI sketch.
3. Fetch the remote and inspect any existing local changes. The current integration
   base is `origin/Emilia/project-delivery`; `main` contains the reviewed runnable
   baseline after PR #2 merged on November 20. Keep existing teammate work on its
   agreed delivery base until further integrations are coordinated.
4. Start a new feature branch from that base, e.g.
   `git switch -c Emilia/review-search origin/Emilia/project-delivery`.
5. Implement the use case through its input/output boundaries; keep Swing and
   infrastructure details out of the core.
6. Add tests for observable behavior, invalid input and meaningful failure paths.
7. Run checks, inspect the diff and make focused commits with meaningful messages.
8. Inspect the diff and open a PR. Explain observable behavior, design and real tests.
9. Have a teammate review it. Record actual feedback and address it in new commits.
   Inspect checks and unresolved comments before merging; do not record approvals
   that did not happen.
10. Update the demo guide and assign the next story. Do not rewrite shared history.

The initial implementation exists in `codex/vocabulary-learning-app` and
`codex/personal-vocabulary-review`. `Emilia/project-delivery` preserves that
AI-assisted history and integrates the signed contract from `main`. Do not split
or reauthor existing commits to imply different teammates wrote them. Additional
MVP work uses the actual contributor's own branch and focused commits.

Those `codex/` branches are historical local work. Current online contributions
include [PR #3](https://github.com/fanfan170721/team-project/pull/3), Allen's UC-08;
[PR #4](https://github.com/fanfan170721/team-project/pull/4), Fuxuan's UC-09 core;
[PR #7](https://github.com/fanfan170721/team-project/pull/7), Xinyu's UC-10 core;
and [PR #8](https://github.com/fanfan170721/team-project/pull/8), Fuxuan's CSV adapter.
All are merged into `Emilia/project-delivery`. Consult `HANDOFF.md` before
starting new work; do not create a new teammate feature from an old local snapshot.

## Individual implementation work

The README assigns new MVP work to each member. `TEAM_TASKS.zh-CN.md` gives exact
steps for completing the two partially implemented use cases. Each member leads their own interactor and
interfaces, controller/presenter, UI, tests and review. The assignment is not
evidence of completion; link real PRs and explain actual contributions as they occur.

Keep meeting notes, estimates, delays and review decisions truthful.
`TeamContract.md` is signed and merged through PR #1.
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

## Review materials

The implementation summary is `docs/PR_DESCRIPTION.md`. View the current diff and
preserved history locally:

```sh
git diff main...HEAD
git log --oneline main..HEAD
```

The original local delivery and this integration are distinct stages. The current
task prepares the project for the team's GitHub workflow. Contract review does not
replace code review or the other members' substantive implementation PRs.
