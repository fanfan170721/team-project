# Prepare a repeatable WordTrail demonstration

The helper creates **synthetic local accounts and activity**, using the packaged
application's public controllers. It does not create Git contributions, record a
real rehearsal, call an API, open a window or play audio. Existing profiles are
never reused or reset by this helper.

## Prepare once before each rehearsal

Use Java 11+ and Maven. From the project root in PowerShell:

```powershell
mvn verify
New-Item -ItemType Directory -Path .wordtrail -Force | Out-Null
java --class-path target/wordtrail.jar tools/PrepareDemo.java .wordtrail/rehearsal-01
```

The last component (`rehearsal-01`) must **not exist**, even as an empty directory.
The parent must already exist. For another rehearsal choose `rehearsal-02` or
another unused name. Do not remove real data to get past a refusal. Preparation
also rejects `target` descendants and linked/redirected parent directories.
If preparation fails after creating a directory, keep it for diagnosis and use
a new name on the next attempt; a partial directory is not reported as a PASS.

Successful preparation prints `PASS` after reopening the store and checking the
fixture. Read `.wordtrail/rehearsal-01/DEMO_DATA.txt` for the preparation date,
time zone, expected state and launch command. Data under `.wordtrail` is ignored
by Git. `mvn clean` removes build outputs in `target`, not this demo directory.

All three accounts use the public demo-only password **`WordTrailDemo123!`**.
Do not reuse it for a real account.

| Account | Prepared state |
| --- | --- |
| `demo_admin` | Administrator account, with the normal shared seed vocabulary; no personal Demo words or completed quizzes. |
| `demo_allen` | Learner; two private words in `Demo`, an edited definition, one saved word, one flashcard review and two completed 1/1 quizzes. |
| `demo_guest` | Learner with shared seed vocabulary; no personal Demo words, completed quizzes or Demo review-plan rows. |

The two private words are `demoanchor` (unreviewed and saved for later) and
`demopractice` (reviewed once on the preparation date). The helper checks that
only `demoanchor` is eligible in the Demo plan on that date and that its edited
definition is used. It exports `demo-results.csv` with two rows and
`guest-results.csv` with only the header, and verifies that planning/exporting
did not mutate the stored profile. These are generated fixtures, not study logs
from a real learner.

**The running app uses its real local date.** `demopractice` becomes due the day
after preparation. Regenerate a new profile on the rehearsal day, on the same
computer/time zone used for the demonstration. The helper's fixed noon clock
keeps generated activity on one reference date; it does not change the computer
clock or freeze time in the running application.

## Open only when ready to rehearse

```powershell
java -jar target/wordtrail.jar --data-dir .wordtrail/rehearsal-01
```

This command opens WordTrail. Close the application before reopening the same
data directory; its store allows only one running instance per directory.

| Feature | Clicks and expected result |
| --- | --- |
| Allen / UC-08 | Log in as `demo_allen`. Follow the [UC-08 walkthrough](ALLEN_UC08_WALKTHROUGH.md) to add and edit `serendipity` in `My words`, retain input on validation failure, switch accounts and reopen. Use `My words` so this action does not alter the prepared Demo plan. |
| Fuxuan / UC-09 | As `demo_allen`, open **Export results**, then **Export results (CSV)...**. Choose a new file such as `live-export.csv` inside the demo directory. Expect **Results exported: 2**, the selected destination, and a UTF-8 CSV with one header and two quiz rows. Cancel a second export and show the cancellation result. Do not overwrite a personal file. |
| Xinyu / UC-10 | As `demo_allen`, open **Review plan**, choose **Demo**, leave the limit at 10 and click **Generate plan**. On the preparation date expect one row, `demoanchor`, with its edited meaning, today's due date and a reason. Enter 0 in the limit and generate: expect validation and no stale plan. Restore 10 and regenerate. |
| Account isolation | Log out and log in as `demo_guest`. Searching for `demoanchor` returns no matching word; **Demo** is absent from this account's topic selector. Export to a new file: expect 0 exported results and only the CSV header. The guest still has ordinary shared seed words, so an **All topics** plan need not be empty. |

Generating a plan and exporting a CSV are read-only with respect to learning
progress. Studying cards or completing more quizzes changes the profile; that
is expected application behavior. Prepare another new directory for a clean run.

## Verify the helper without opening the UI

```powershell
java tools/TestDemoPreparation.java target/wordtrail.jar
```

The regression harness invokes the real helper in a child Java process. It
checks fresh creation and the helper's built-in persistence/isolation checks,
CSV row counts, rejection of existing data with byte-for-byte preservation,
empty directories, ordinary files, missing parents, build-output paths and a
symbolic-link parent. The link case is explicitly skipped if the host cannot
create symbolic links. It cleans up only its newly generated OS temporary
directory. These assertions are separate from the Maven/JUnit test count.

The helper does not validate native file dialogs, live API availability or
presentation delivery. Use the [presentation outline](PRESENTATION_OUTLINE.md)
and record actual rehearsal results separately.
