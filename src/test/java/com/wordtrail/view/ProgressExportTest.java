package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.app.Application;
import com.wordtrail.usecase.AppException;
import java.awt.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** UC-09 end to end: Progress button, dialogs, controller, interactor and the real CSV exporter. */
@Timeout(30)
class ProgressExportTest {
    private static final String HEADER = "completed_at_utc,topic,correct,total";

    @TempDir Path dataDirectory;
    @TempDir Path exports;

    private Application application;
    private AppController controller;
    private final FakeDialogs dialogs = new FakeDialogs();
    private final List<String> statusMessages = new CopyOnWriteArrayList<>(); // written on the EDT
    private UiTasks tasks;
    private ProgressPanel panel;

    /** Scripted answers to the save dialog and the overwrite question. */
    private static final class FakeDialogs implements ExportDialogs {
        String destination;
        boolean allowOverwrite;
        final List<String> overwritePrompts = new ArrayList<>();

        @Override public String chooseDestination(Component parent) { return destination; }
        @Override public boolean confirmOverwrite(Component parent, String path) {
            overwritePrompts.add(path);
            return allowOverwrite;
        }
    }

    @BeforeEach
    void open() throws Exception {
        application = new Application(dataDirectory, term -> {
            throw new AppException("Offline in tests.");
        }, Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/New_York")));
        controller = application.controller();
        JPanel root = edt(JPanel::new);
        tasks = new UiTasks(root, statusMessages::add);
        panel = edt(() -> {
            ProgressPanel created = new ProgressPanel(controller, tasks, dialogs);
            root.add(created);
            return created;
        });
    }

    @AfterEach
    void close() throws IOException {
        application.close();
    }

    private void completeQuizzes(int count) {
        for (int i = 0; i < count; i++) {
            controller.startQuiz("Travel", 1);
            assertTrue(controller.answer(0).finished);
        }
    }

    /** bob has one completed quiz, alice two; alice stays signed in. */
    private void twoLearners() {
        controller.setup("admin", "password123".toCharArray());
        controller.register("bob", "password123".toCharArray());
        completeQuizzes(1);
        controller.register("alice", "password123".toCharArray());
        completeQuizzes(2);
    }

    private void clickExport() throws Exception {
        edt(() -> {
            AbstractButton button = (AbstractButton) find(panel, "progress.export");
            assertNotNull(button, "Missing export button");
            button.doClick(0);
            return null;
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (edt(tasks::isBusy)) {
            if (System.nanoTime() > deadline) { fail("Export did not finish: " + statusMessages); }
            Thread.sleep(10);
        }
    }

    private String exportStatus() throws Exception {
        return edt(() -> ((JLabel) find(panel, "progress.exportStatus")).getText());
    }

    private List<String> csvLines(Path file) throws IOException {
        return List.of(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\r\n"));
    }

    private Set<String> exportedFiles() throws IOException {
        try (Stream<Path> files = Files.list(exports)) {
            return files.map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        }
    }

    @Test
    void exportsOnlyTheSignedInLearnersResultsAndReportsWhereAndHowMany() throws Exception {
        twoLearners();
        Path target = exports.resolve("alice.csv");
        dialogs.destination = target.toString();

        clickExport();

        List<String> lines = csvLines(target);
        assertEquals(3, lines.size(), "header plus alice's two results, never bob's");
        assertEquals(HEADER, lines.get(0));
        assertTrue(lines.get(1).matches("2026-10-22T16:00:00Z,Travel,[01],1"), lines.get(1));
        assertTrue(lines.get(2).matches("2026-10-22T16:00:00Z,Travel,[01],1"), lines.get(2));
        String expected = "Exported 2 quiz results to " + target + ".";
        assertEquals(expected, exportStatus());
        assertEquals(expected, statusMessages.get(statusMessages.size() - 1));
        assertTrue(dialogs.overwritePrompts.isEmpty(), "a new file needs no overwrite confirmation");

        controller.login("bob", "password123".toCharArray());
        dialogs.destination = exports.resolve("bob.csv").toString();
        clickExport();
        assertEquals(2, csvLines(exports.resolve("bob.csv")).size());
        assertEquals("Exported 1 quiz result to " + exports.resolve("bob.csv") + ".", exportStatus());
    }

    @Test
    void emptyHistoryExportsHeaderAndReportsZero() throws Exception {
        controller.setup("admin", "password123".toCharArray());
        controller.register("carol", "password123".toCharArray());
        Path target = exports.resolve("empty.csv");
        dialogs.destination = target.toString();

        clickExport();

        assertEquals(List.of(HEADER), csvLines(target));
        assertEquals("Exported 0 quiz results to " + target + ". The file contains only the header row.",
                exportStatus());
    }

    @Test
    void cancellingTheSaveDialogWritesNothing() throws Exception {
        twoLearners();
        dialogs.destination = null;

        clickExport();

        assertEquals("Export cancelled. No file was written.", exportStatus());
        assertTrue(exportedFiles().isEmpty());
        assertFalse(statusMessages.stream().anyMatch(m -> m.startsWith("Exported")));
    }

    @Test
    void decliningOverwriteKeepsTheExistingFile() throws Exception {
        twoLearners();
        Path target = exports.resolve("results.csv");
        Files.writeString(target, "keep me\n");
        dialogs.destination = target.toString();
        dialogs.allowOverwrite = false;

        clickExport();

        assertEquals(List.of(target.toString()), dialogs.overwritePrompts);
        assertEquals("keep me\n", Files.readString(target));
        assertEquals("Export cancelled. The existing file was not replaced.", exportStatus());
        assertEquals(Set.of("results.csv"), exportedFiles());
    }

    @Test
    void confirmingOverwriteReplacesTheExistingFile() throws Exception {
        twoLearners();
        Path target = exports.resolve("results.csv");
        Files.writeString(target, "old export\n");
        dialogs.destination = target.toString();
        dialogs.allowOverwrite = true;

        clickExport();

        assertEquals(List.of(target.toString()), dialogs.overwritePrompts);
        assertEquals(3, csvLines(target).size());
        assertEquals("Exported 2 quiz results to " + target + ".", exportStatus());
        assertEquals(Set.of("results.csv"), exportedFiles(), "no temporary file is left behind");
    }

    @Test
    void writeFailureIsShownAndNeverReportedAsSuccess() throws Exception {
        twoLearners();
        Path target = exports.resolve("missing-folder").resolve("results.csv");
        dialogs.destination = target.toString();

        clickExport();

        assertEquals("Export failed: Could not save the results file. Any existing file was left unchanged.",
                exportStatus());
        assertFalse(statusMessages.stream().anyMatch(m -> m.startsWith("Exported")));
        assertTrue(exportedFiles().isEmpty());
    }

    @Test
    void addsCsvExtensionWhenTheLearnerOmitsIt() throws Exception {
        twoLearners();
        dialogs.destination = exports.resolve("my-results").toString();

        clickExport();

        assertEquals(Set.of("my-results.csv"), exportedFiles());
        assertEquals("Exported 2 quiz results to " + exports.resolve("my-results.csv") + ".", exportStatus());
        assertEquals("report.CSV", ProgressPanel.withCsvExtension("report.CSV"));
    }

    private static Component find(Component current, String name) {
        if (name.equals(current.getName())) { return current; }
        if (current instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) current).getComponents()) {
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
