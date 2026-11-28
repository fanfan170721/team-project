package com.wordtrail.app;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ExportResultsViewModel;
import com.wordtrail.adapter.ViewModels;
import com.wordtrail.usecase.AppException;
import com.wordtrail.view.AppPanel;
import java.awt.Component;
import java.awt.Container;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** UC-09 through the real composition root: accounts, quizzes, JSON storage and the real CSV exporter. */
@Timeout(45)
class ApplicationExportResultsTest {
    private static final String HEADER = "completed_at_utc,topic,correct,total";
    private static final String TOPIC = "Snacks, \"sweet\"";
    private static final String DEFINITION = "A small amount of food eaten between meals.";

    @TempDir Path data;
    @TempDir Path exports;

    private final StepClock clock = new StepClock(Instant.parse("2026-10-22T12:00:00Z"));
    private Application application;
    private AppController controller;

    /** A fixed clock the test moves explicitly, so every completion time is known. */
    private static final class StepClock extends Clock {
        private volatile Instant now;
        StepClock(Instant now) { this.now = now; }
        void set(String instant) { now = Instant.parse(instant); }
        @Override public ZoneId getZone() { return ZoneId.of("America/New_York"); }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
        @Override public Instant instant() { return now; }
    }

    @BeforeEach
    void open() {
        application = new Application(data, term -> {
            throw new AppException("Offline in tests.");
        }, clock);
        controller = application.controller();
    }

    @AfterEach
    void close() throws Exception {
        application.close();
    }

    /** Completes a one-question quiz on the learner's own topic, answering correctly or not. */
    private void completeSnackQuiz(String at, boolean correctly) {
        clock.set(at);
        ViewModels.Quiz quiz = controller.startQuiz(TOPIC, 1);
        int correct = quiz.options.indexOf(DEFINITION);
        assertTrue(correct >= 0, quiz.options.toString());
        assertTrue(controller.answer(correctly ? correct : (correct + 1) % 4).finished);
    }

    /** alice completes two quizzes, the later one first; bob completes one; alice is signed in at the end. */
    private void twoLearnersWithResults() {
        controller.setup("admin", "password123".toCharArray());
        controller.register("alice", "password123".toCharArray());
        assertTrue(controller.addPersonalWord("snack", TOPIC, DEFINITION, "", "").success);
        completeSnackQuiz("2026-10-22T18:30:00Z", true);
        completeSnackQuiz("2026-10-21T09:15:00Z", false);
        controller.register("bob", "password123".toCharArray());
        clock.set("2026-10-23T07:05:00Z");
        controller.startQuiz("Travel", 1);
        assertTrue(controller.answer(0).finished);
        controller.login("alice", "password123".toCharArray());
    }

    private static List<String> csvLines(Path file) throws Exception {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        assertTrue(text.endsWith("\r\n"), "CSV records end with CRLF");
        return List.of(text.split("\r\n"));
    }

    @Test
    void exportsOnlyTheSignedInLearnersResultsInUtcChronologicalOrder() throws Exception {
        twoLearnersWithResults();
        Path aliceFile = exports.resolve("alice.csv");

        ExportResultsViewModel.Result alice = controller.exportResults(aliceFile.toString());

        assertTrue(alice.success);
        assertEquals(2, alice.exportedCount);
        assertEquals(aliceFile.toString(), alice.destination);
        assertEquals("Exported 2 quiz results to " + aliceFile + ".", alice.message);
        assertEquals(List.of(HEADER,
                "2026-10-21T09:15:00Z,\"Snacks, \"\"sweet\"\"\",0,1",
                "2026-10-22T18:30:00Z,\"Snacks, \"\"sweet\"\"\",1,1"), csvLines(aliceFile));

        controller.login("bob", "password123".toCharArray());
        Path bobFile = exports.resolve("bob.csv");
        ExportResultsViewModel.Result bob = controller.exportResults(bobFile.toString());

        assertTrue(bob.success);
        assertEquals(1, bob.exportedCount);
        List<String> bobLines = csvLines(bobFile);
        assertEquals(2, bobLines.size());
        assertTrue(bobLines.get(1).matches("2026-10-23T07:05:00Z,Travel,[01],1"), bobLines.get(1));
        assertFalse(String.join("\n", bobLines).contains("Snacks"), "bob never receives alice's results");
    }

    @Test
    void emptyHistoryExportsOnlyTheHeaderAndReportsZero() throws Exception {
        controller.setup("admin", "password123".toCharArray());
        controller.register("carol", "password123".toCharArray());
        Path target = exports.resolve("carol.csv");

        ExportResultsViewModel.Result result = controller.exportResults(target.toString());

        assertTrue(result.success);
        assertEquals(0, result.exportedCount);
        assertEquals(List.of(HEADER), csvLines(target));
    }

    @Test
    void writeFailureIsReportedAndLeavesLearningProgressUnchanged() throws Exception {
        twoLearnersWithResults();
        Path store = data.resolve("wordtrail.json");
        byte[] storedBefore = Files.readAllBytes(store);
        int quizzesBefore = controller.dashboard().quizzes;
        Path target = exports.resolve("missing-folder").resolve("alice.csv");

        ExportResultsViewModel.Result result = controller.exportResults(target.toString());

        assertFalse(result.success);
        assertEquals("", result.destination);
        assertEquals(0, result.exportedCount);
        assertEquals("Export failed: Could not save the results file. Any existing file was left unchanged.",
                result.message);
        assertFalse(Files.exists(target));
        assertArrayEquals(storedBefore, Files.readAllBytes(store));
        assertEquals(quizzesBefore, controller.dashboard().quizzes);
    }

    @Test
    void signedOutExportFailsWithoutWriting() {
        controller.setup("admin", "password123".toCharArray());
        controller.logout();
        Path target = exports.resolve("nobody.csv");

        ExportResultsViewModel.Result result = controller.exportResults(target.toString());

        assertFalse(result.success);
        assertFalse(Files.exists(target));
    }

    @Test
    void theRunningApplicationShowsTheExportTabToEveryAccount() throws Exception {
        controller.setup("admin", "password123".toCharArray());
        controller.register("alice", "password123".toCharArray());
        controller.logout();
        AppPanel panel = edt(() -> new AppPanel(controller));

        for (String username : List.of("admin", "alice")) {
            signIn(panel, username);
            JTabbedPane tabs = edt(() -> (JTabbedPane) find(panel, "home.tabs"));
            int index = edt(() -> tabs.indexOfTab("Export results"));
            assertEquals(username.equals("admin") ? 6 : 5, index,
                    "the export tab follows the existing tabs, including Admin when present");
            assertEquals(index + 1, edt(() -> tabs.indexOfTab("Review plan")),
                    "the review plan tab follows export without displacing existing tabs");
            assertEquals(edt(tabs::getTabCount) - 1, edt(() -> tabs.indexOfTab("Study")));
            assertEquals(username.equals("admin"), edt(() -> tabs.indexOfTab("Admin") >= 0));
            assertEquals("ExportResultsPanel", edt(() -> tabs.getComponentAt(index).getClass().getSimpleName()));
            edt(() -> { tabs.setSelectedIndex(index); return null; });
            waitIdle(panel);
            assertNotNull(edt(() -> find(panel, "export.choose")));
            assertTrue(edt(() -> ((JTextArea) find(panel, "export.result")).getText()).startsWith("Choose"));
            try (var files = Files.list(exports)) {
                assertEquals(0, files.count(), "showing the tab never writes a file");
            }
            edt(() -> { ((AbstractButton) find(panel, "account.logout")).doClick(0); return null; });
            waitIdle(panel);
        }
    }

    private static void signIn(AppPanel panel, String username) throws Exception {
        edt(() -> {
            ((JTextComponent) find(panel, "auth.username")).setText(username);
            ((JTextComponent) find(panel, "auth.password")).setText("password123");
            ((AbstractButton) find(panel, "auth.submit")).doClick(0);
            return null;
        });
        waitIdle(panel);
        assertNotNull(edt(() -> find(panel, "home.tabs")), "sign-in failed for " + username);
    }

    private static void waitIdle(AppPanel panel) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (edt(panel::isBusy)) {
            if (System.nanoTime() > deadline) { fail("UI did not become idle"); }
            Thread.sleep(10);
        }
        edt(() -> null);
    }

    private static Component find(Component current, String name) {
        if (name.equals(current.getName())) { return current; }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) {
                Component result = find(child, name);
                if (result != null) { return result; }
            }
        }
        return null;
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
