# Packaged application verification

This check exercises the executable JAR as a separate application dependency.
It complements the Maven tests: a successful test build alone does not prove that
the packaged classes, resources and JSON dependency work together.

## Run the check

Use a JDK with Java 11 or later and run these commands from the project root:

```sh
mvn clean verify
java tools/CheckPackage.java target/wordtrail.jar target/package-check
```

Keep real profiles outside `target/`, which `mvn clean` deletes. The check creates
its own temporary profile under the output directory and removes that profile
after the probe. It does not use an existing learner account, open a native window
or contact the public dictionary API.

The wrapper accepts a JAR, an output directory and an optional probe source path:

```text
java tools/CheckPackage.java <jar> <output-directory> [probe-source]
```

The default probe is `tools/VerifyPackage.java`. The optional source argument
supports regression checks; normal verification uses the default. The wrapper
starts a separate headless Java process with only the selected JAR on the
application classpath, rather than Maven's compiled classes or test dependencies.

## What the default probe verifies

- `Application` and its controllers can initialize an isolated profile.
- Packaged vocabulary and quiz questions can be loaded.
- Initial account setup, a completed seed quiz, a personal vocabulary save and a
  personal-definition edit succeed.
- A JSON data file is created using the dependency packaged in the JAR.
- UC-10 produces a topic-filtered review plan containing the saved personal word
  and its edited definition, with the expected due date.
- UC-09 writes a real UTF-8 CSV with the expected header, UTC timestamp, topic,
  score and quiz count. Its reported destination matches the written file.
- Closing and reopening `Application` preserves the account, personal edit and
  completed quiz; the new export is byte-identical to the first export.
- A second account sees neither the first account's personal word nor its quiz
  result; its plan is empty for that private topic and its CSV has only a header.
- Generating each plan and exporting results leaves the JSON profile bytes
  unchanged. All CSV fixtures are inside the isolated profile and removed with it.

The probe supplies a fixed clock and a dictionary gateway that fails if called.
This is a small combined packaging smoke check for UC-08/09/10, not a replacement
for their detailed behavior and Swing tests. It does not test a real server,
native rendering, save/overwrite dialogs, pronunciation or every use case. Those
require their own tests or separately reported manual checks.

## Read the evidence

The output directory contains two companion evidence files:

| File | Meaning |
| --- | --- |
| `result.txt` | Human-readable outcome for the current invocation. |
| `verification.properties` | Machine-readable status, before/after JAR SHA-256 checksums, reported source revision, Java/OS details, time and child-process exit information. |

The wrapper replaces earlier success evidence before compiling the probe. This
matters because a missing packaged class can prevent Java from entering the
probe's `main` method; a reset inside that method alone would leave an old report.

An exclusive operating-system file lock serializes access to each output
directory. The wrapper acquires `.package-check.lock` before touching either
evidence file and holds it through all success/failure writes. A simultaneous
wrapper using the same directory exits nonzero without starting its probe or
changing the active wrapper's evidence. Use separate output directories for
parallel checks, or retry after the active invocation finishes. The empty lock
file remains intentionally; do not delete or replace it while a check is running.
Closing the process releases the operating-system lock, so a retained lock file
does not by itself mean another check is active.

Treat only a completed `PASS` for the selected JAR as a successful check.
`NOT_RUN` is incomplete and `FAIL` records an unsuccessful invocation. A successful
child exit alone is insufficient: the probe must produce its success report, and
the JAR must retain the same checksum throughout the check. Inspect the wrapper's
exit status as well as both output files; a missing report is not a pass. Files
left by an active or later invocation do not turn a rejected invocation into a
success. The output files are a shared latest-run snapshot, not a run history.

The revision is taken from `WORDTRAIL_REVISION`, then `GITHUB_SHA`, or recorded as
unknown when neither is supplied. This is reported provenance, not proof that a
local checkout is clean or that a JAR was built from that exact revision. The
checksums identify the bytes checked and detect changes during the check; they
are not a signature or a security guarantee.

Keep both evidence files with the corresponding JAR when sharing build results.
The CI `wordtrail-jar` artifact contains `wordtrail.jar` and the two files under
`package-check/`. Rebuilds need a new check; previous
evidence does not apply automatically to a replacement JAR.

`VerifyPackage.java` remains the low-level probe. Running it directly can help
diagnosis, but bypasses the wrapper's pre-compilation reset and artifact metadata.
Its report alone is not the authoritative current package-verification record.
The low-level probe does not acquire the wrapper's lock; do not run it directly
against an output directory owned by a wrapper. The optional probe source is
trusted executable code, not an isolation or security boundary.

## Regression checks

After building the JAR, run:

```sh
java tools/TestPackageCheck.java target/wordtrail.jar target/package-check-regressions
```

This source-file harness checks a real successful package, then removes
`Application.class` from a separate JAR copy to reproduce failure before the
probe's `main` can run. It verifies that the same output directory loses the old
PASS, that a valid later check recovers, and that missing input, a child without a
result, and a JAR changed during checking all fail. A marker-coordinated pair of
processes reproduces a silent child overlapping a valid probe: the contender must
fail without changing the owner's report/metadata, the silent owner must fail,
and a later valid check must recover. This failed before the locking fix and
passes with it. The original JAR's checksum must stay unchanged. There are now
31 process-level assertions, separate from JUnit totals.

Fixtures and child logs remain under the chosen output directory for diagnosis.
Use an ignored build directory, not a personal data directory. CI runs the harness
on Windows/Linux with Java 11/17 and retains its outcome and logs in the test
artifact; deliberately damaged JAR copies are not included in the downloadable
application artifact.
