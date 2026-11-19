package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.usecase.port.ProgressRepository;
import com.wordtrail.usecase.port.QuizResultsExporter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExportResultsInteractorTest {

    /** Records what the core handed to the port; optionally fails like a full disk would. */
    private static final class FakeExporter implements QuizResultsExporter {
        final List<String> destinations = new ArrayList<>();
        List<ExportResultsRow> rows;
        RuntimeException failure;

        @Override public void export(String destination, List<ExportResultsRow> exported) {
            destinations.add(destination);
            rows = new ArrayList<>(exported);
            if (failure != null) { throw failure; }
        }
    }

    /** Records every presenter call so tests can prove exactly one outcome was reported. */
    private static final class RecordingPresenter implements ExportResultsOutputBoundary {
        final List<ExportResultsResponse> successes = new ArrayList<>();
        final List<String> failures = new ArrayList<>();

        @Override public void prepareSuccessView(ExportResultsResponse response) { successes.add(response); }
        @Override public void prepareFailView(String errorMessage) { failures.add(errorMessage); }
    }

    private final UseCaseFixture f = new UseCaseFixture();
    private final FakeExporter exporter = new FakeExporter();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final ExportResultsInteractor export =
            new ExportResultsInteractor(f.store, exporter, f.session, presenter);

    private static QuizAttempt attempt(String time, String topic, int correct, int total) {
        return new QuizAttempt(Instant.parse(time), topic, correct, total);
    }

    /** Signs bob in, gives him a result, then leaves alice as the active account. */
    private void aliceAndBobWithHistory(QuizAttempt... aliceAttempts) {
        f.admin();
        f.learner("bob");
        f.learner("alice");
        f.store.progress.put("bob", LearningProgress.empty()
                .withAttempt(attempt("2026-01-01T00:00:00Z", "Bob only", 1, 1)));
        LearningProgress alice = LearningProgress.empty();
        for (QuizAttempt a : aliceAttempts) { alice = alice.withAttempt(a); }
        f.store.progress.put("alice", alice);
    }

    private void assertFailedWithoutSuccess(String expectedMessage) {
        assertEquals(List.of(expectedMessage), presenter.failures);
        assertTrue(presenter.successes.isEmpty());
    }

    @Test void exportsOnlySignedInLearnersResultsOldestFirstInUtc() {
        aliceAndBobWithHistory(
                attempt("2026-10-22T16:30:00Z", "Nature", 2, 3),
                attempt("2026-03-05T09:15:00Z", "Travel, \"daily\"", 4, 5),
                attempt("2026-10-22T16:00:00Z", "Travel", 0, 2));

        export.execute("C:/exports/alice.csv");

        assertEquals(List.of("C:/exports/alice.csv"), exporter.destinations);
        assertEquals(3, exporter.rows.size());
        ExportResultsRow first = exporter.rows.get(0);
        assertEquals("2026-03-05T09:15:00Z", first.getCompletedAtUtc());
        assertEquals("Travel, \"daily\"", first.getTopic());
        assertEquals(4, first.getCorrect());
        assertEquals(5, first.getTotal());
        assertEquals("2026-10-22T16:00:00Z", exporter.rows.get(1).getCompletedAtUtc());
        assertEquals("Travel", exporter.rows.get(1).getTopic());
        assertEquals(0, exporter.rows.get(1).getCorrect());
        assertEquals(2, exporter.rows.get(1).getTotal());
        assertEquals("2026-10-22T16:30:00Z", exporter.rows.get(2).getCompletedAtUtc());
        assertEquals("Nature", exporter.rows.get(2).getTopic());
        assertTrue(exporter.rows.stream().noneMatch(r -> r.getTopic().equals("Bob only")),
                "another account's results must never be exported");

        assertTrue(presenter.failures.isEmpty());
        assertEquals(1, presenter.successes.size());
        assertEquals("C:/exports/alice.csv", presenter.successes.get(0).getDestination());
        assertEquals(3, presenter.successes.get(0).getExportedCount());
    }

    @Test void switchingAccountsExportsTheOtherLearnersResultsOnly() {
        aliceAndBobWithHistory(attempt("2026-02-01T00:00:00Z", "Alice only", 1, 2));
        f.auth.login("bob", "password123".toCharArray());

        export.execute("bob.csv");

        assertEquals(1, exporter.rows.size());
        assertEquals("Bob only", exporter.rows.get(0).getTopic());
        assertEquals("2026-01-01T00:00:00Z", exporter.rows.get(0).getCompletedAtUtc());
        assertEquals(1, presenter.successes.get(0).getExportedCount());
    }

    @Test void exportDoesNotSaveOrChangeProgress() {
        aliceAndBobWithHistory(
                attempt("2026-10-22T16:30:00Z", "Nature", 2, 3),
                attempt("2026-03-05T09:15:00Z", "Travel", 4, 5));
        LearningProgress before = f.store.progress.get("alice");
        List<QuizAttempt> originalOrder = new ArrayList<>(before.getAttempts());
        f.store.failSave = true; // any save attempt would throw and break the success path

        export.execute("alice.csv");

        assertEquals(1, presenter.successes.size());
        assertSame(before, f.store.progress.get("alice"));
        assertEquals(originalOrder, f.store.progress.get("alice").getAttempts(),
                "sorting for export must not reorder stored history");
        assertEquals(1, f.store.progress.get("bob").getAttempts().size());
    }

    @Test void emptyHistoryStillCallsExporterAndReportsZero() {
        aliceAndBobWithHistory();

        export.execute("empty.csv");

        assertEquals(List.of("empty.csv"), exporter.destinations);
        assertTrue(exporter.rows.isEmpty());
        assertTrue(presenter.failures.isEmpty());
        assertEquals("empty.csv", presenter.successes.get(0).getDestination());
        assertEquals(0, presenter.successes.get(0).getExportedCount());
    }

    @Test void signedOutLearnerCannotExport() {
        export.execute("anything.csv");

        assertFailedWithoutSuccess("Please log in first.");
        assertTrue(exporter.destinations.isEmpty());
    }

    @Test void blankOrMissingDestinationIsRejectedBeforeExporting() {
        aliceAndBobWithHistory(attempt("2026-02-01T00:00:00Z", "Travel", 1, 2));

        export.execute("   ");
        export.execute(null);

        assertEquals(List.of("Please choose where to save the exported results.",
                "Please choose where to save the exported results."), presenter.failures);
        assertTrue(presenter.successes.isEmpty());
        assertTrue(exporter.destinations.isEmpty());
    }

    @Test void readFailureReportsFailureAndExportsNothing() {
        f.admin();
        f.learner("alice");
        ProgressRepository broken = new ProgressRepository() {
            @Override public LearningProgress getProgress(String username) {
                throw new AppException("Simulated corrupt progress file.");
            }
            @Override public void saveProgress(String username, LearningProgress progress) {
                fail("export must never save progress");
            }
        };
        ExportResultsInteractor failing = new ExportResultsInteractor(broken, exporter, f.session, presenter);

        failing.execute("alice.csv");

        assertFailedWithoutSuccess("Could not read your quiz results. Nothing was exported.");
        assertTrue(exporter.destinations.isEmpty());
    }

    @Test void exporterFailureIsReportedAndNeverClaimsSuccess() {
        aliceAndBobWithHistory(attempt("2026-02-01T00:00:00Z", "Travel", 1, 2));
        LearningProgress before = f.store.progress.get("alice");
        exporter.failure = new AppException("Disk is full.");

        export.execute("alice.csv");

        assertFailedWithoutSuccess("Export failed: Disk is full.");
        assertEquals(List.of("alice.csv"), exporter.destinations);
        assertSame(before, f.store.progress.get("alice"));
    }
}
