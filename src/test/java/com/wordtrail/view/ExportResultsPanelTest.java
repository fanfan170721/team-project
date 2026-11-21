package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ExportResultsController;
import com.wordtrail.adapter.ExportResultsPresenter;
import com.wordtrail.adapter.ExportResultsViewModel;
import com.wordtrail.adapter.ViewModels;
import com.wordtrail.app.Application;
import com.wordtrail.data.CsvQuizResultsExporter;
import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.ExportResultsResponse;
import com.wordtrail.usecase.ExportResultsRow;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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

/** Clicks the real export button with real UiTasks; only the native dialog answers are scripted. */
@Timeout(30)
class ExportResultsPanelTest {
    private static final byte[] SENTINEL = "keep me, \"unchanged\"\r\n".getBytes(StandardCharsets.UTF_8);

    @TempDir Path folder;

    private final ScriptedDialogs dialogs = new ScriptedDialogs();
    private final List<String> useCaseCalls = new CopyOnWriteArrayList<>();
    private final List<String> statusMessages = new CopyOnWriteArrayList<>();
    private final ExportResultsViewModel viewModel = new ExportResultsViewModel();
    private final ExportResultsPresenter presenter = new ExportResultsPresenter(viewModel);
    /** What the use case does with the destination it receives; replaced per test. */
    private volatile Consumer<String> behaviour;
    private UiTasks tasks;
    private ExportResultsPanel panel;

    private static final class ScriptedDialogs implements SwingExportDestinationChooser.Dialogs {
        volatile File selection;
        volatile int replaceAnswer = JOptionPane.CLOSED_OPTION;
        final List<String> saveDialogs = new CopyOnWriteArrayList<>();
        final List<Path> replaceQuestions = new CopyOnWriteArrayList<>();

        @Override public Optional<File> showSaveDialog(Component parent) {
            assertTrue(SwingUtilities.isEventDispatchThread(), "dialogs must run on the EDT");
            saveDialogs.add(selection == null ? "<cancelled>" : selection.getPath());
            return Optional.ofNullable(selection);
        }

        @Override public int confirmReplace(Component parent, Path target) {
            assertTrue(SwingUtilities.isEventDispatchThread(), "dialogs must run on the EDT");
            replaceQuestions.add(target);
            return replaceAnswer;
        }

        @Override public void showFolderChosen(Component parent, Path target) { fail("unexpected folder"); }
    }

    @BeforeEach
    void open() throws Exception {
        ExportResultsController export = new ExportResultsController(destination -> {
            assertFalse(SwingUtilities.isEventDispatchThread(), "file I/O must not run on the EDT");
            useCaseCalls.add(destination);
            behaviour.accept(destination);
        }, viewModel);
        AppController controller = new AppController(null, null, null, null, null, null, null, null, null, export);
        JPanel root = edt(JPanel::new);
        tasks = new UiTasks(root, statusMessages::add);
        panel = edt(() -> {
            ExportResultsPanel created = new ExportResultsPanel(controller, tasks,
                    new SwingExportDestinationChooser(dialogs));
            root.add(created);
            return created;
        });
    }

    /** Writes a real CSV with the given number of rows, then reports success, like the real interactor. */
    private void exportsRows(int count) {
        behaviour = destination -> {
            List<ExportResultsRow> rows = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                rows.add(new ExportResultsRow("2026-10-22T16:0" + i + ":00Z", "Travel", 1, 1));
            }
            new CsvQuizResultsExporter().export(destination, rows);
            presenter.prepareSuccessView(new ExportResultsResponse(destination, count));
        };
    }

    private void failsWith(String message) {
        behaviour = destination -> presenter.prepareFailView(message);
    }

    private Path sentinel(String name) throws IOException {
        return Files.write(folder.resolve(name), SENTINEL);
    }

    @Test
    void approvedNewFileIsExportedOnceAndShowsDestinationAndCount() throws Exception {
        Path target = folder.resolve("results.csv");
        dialogs.selection = target.toFile();
        exportsRows(2);

        clickExport();

        assertEquals(List.of(target.toString()), useCaseCalls);
        assertEquals(3, Files.readAllLines(target).size());
        assertEquals("Exported 2 quiz results to " + target + ".", resultText());
        assertEquals("Saved to: " + target, label("export.destination"));
        assertEquals("Results exported: 2", label("export.count"));
        assertEquals("Exported 2 quiz results to " + target + ".", lastStatus());
        assertTrue(dialogs.replaceQuestions.isEmpty());
        assertTrue(edt(() -> button().isEnabled()), "controls are restored after the export");
    }

    @Test
    void emptyHistoryShowsAnExplicitZero() throws Exception {
        Path target = folder.resolve("empty.csv");
        dialogs.selection = target.toFile();
        exportsRows(0);

        clickExport();

        assertEquals(List.of("completed_at_utc,topic,correct,total"), Files.readAllLines(target));
        assertEquals("Exported 0 quiz results to " + target + ". The file contains only the header row.",
                resultText());
        assertEquals("Results exported: 0", label("export.count"));
    }

    @Test
    void cancellingTheSaveDialogNeverCallsTheUseCase() throws Exception {
        Path existing = sentinel("results.csv");
        exportsRows(2);
        dialogs.selection = folder.resolve("results.csv").toFile();
        dialogs.replaceAnswer = JOptionPane.YES_OPTION;
        clickExport();
        dialogs.selection = null;

        clickExport();

        assertEquals(1, useCaseCalls.size(), "only the first, approved request exported");
        assertEquals(ExportResultsPanel.CANCELLED, resultText());
        assertEquals(ExportResultsPanel.NO_DESTINATION, label("export.destination"));
        assertEquals("Results exported: 0", label("export.count"));
        assertEquals(ExportResultsPanel.CANCELLED, lastStatus());
        assertEquals(3, Files.readAllLines(existing).size(), "the cancelled request did not touch the file");
    }

    @Test
    void cancellingLeavesASentinelFileByteIdentical() throws Exception {
        Path existing = sentinel("results.csv");
        exportsRows(2);
        dialogs.selection = null;

        clickExport();

        assertTrue(useCaseCalls.isEmpty());
        assertArrayEquals(SENTINEL, Files.readAllBytes(existing));
        assertEquals(ExportResultsPanel.CANCELLED, resultText());
    }

    @Test
    void decliningOrClosingTheReplaceQuestionKeepsTheFileAndReportsNoSuccess() throws Exception {
        Path existing = sentinel("results.csv");
        exportsRows(2);
        dialogs.selection = existing.toFile();
        for (int answer : new int[] {JOptionPane.NO_OPTION, JOptionPane.CLOSED_OPTION}) {
            dialogs.replaceAnswer = answer;

            clickExport();

            assertTrue(useCaseCalls.isEmpty(), "declined overwrite must not call the use case");
            assertArrayEquals(SENTINEL, Files.readAllBytes(existing));
            assertEquals(ExportResultsPanel.CANCELLED, resultText());
            assertFalse(statusMessages.stream().anyMatch(message -> message.startsWith("Exported")));
        }
        assertEquals(List.of(existing.toAbsolutePath(), existing.toAbsolutePath()), dialogs.replaceQuestions);
    }

    @Test
    void confirmedReplacementWritesTheConfirmedCsvFile() throws Exception {
        Path existing = sentinel("report.csv");
        exportsRows(1);
        dialogs.selection = folder.resolve("report").toFile();
        dialogs.replaceAnswer = JOptionPane.YES_OPTION;

        clickExport();

        assertEquals(List.of(existing.toAbsolutePath()), dialogs.replaceQuestions);
        assertEquals(List.of(existing.toAbsolutePath().toString()), useCaseCalls,
                "the confirmed file is exactly the file the use case writes");
        assertEquals(2, Files.readAllLines(existing).size());
        assertEquals("Exported 1 quiz result to " + existing.toAbsolutePath() + ".", resultText());
    }

    @Test
    void failureAfterSuccessShowsOnlyTheError() throws Exception {
        dialogs.selection = folder.resolve("first.csv").toFile();
        exportsRows(2);
        clickExport();
        dialogs.selection = folder.resolve("second.csv").toFile();
        failsWith("Export failed: Could not save the results file. Any existing file was left unchanged.");

        clickExport();

        assertEquals("Export failed: Could not save the results file. Any existing file was left unchanged.",
                resultText());
        assertEquals(ExportResultsPanel.NO_DESTINATION, label("export.destination"));
        assertEquals("Results exported: 0", label("export.count"));
        assertEquals("Export failed: Could not save the results file. Any existing file was left unchanged.",
                lastStatus());
    }

    @Test
    void unexpectedFailureNeverLeavesTheEarlierSuccessVisible() throws Exception {
        dialogs.selection = folder.resolve("first.csv").toFile();
        exportsRows(2);
        clickExport();
        dialogs.selection = folder.resolve("second.csv").toFile();
        behaviour = destination -> { };  // the use case produced no output, so the controller throws

        clickExport();

        assertEquals(ExportResultsPanel.UNEXPECTED, resultText());
        assertEquals(ExportResultsPanel.NO_DESTINATION, label("export.destination"));
        assertEquals("Results exported: 0", label("export.count"));
        assertEquals("An unexpected error occurred. See the console for details.", lastStatus());
        assertTrue(edt(() -> button().isEnabled()));
    }

    @Test
    void refreshClearsTheScreenWithoutOpeningDialogsOrExporting() throws Exception {
        dialogs.selection = folder.resolve("results.csv").toFile();
        exportsRows(2);
        clickExport();

        edt(() -> { panel.refresh(); return null; });
        edt(() -> { panel.refresh(); return null; });
        waitIdle();

        assertEquals(1, dialogs.saveDialogs.size());
        assertEquals(1, useCaseCalls.size());
        assertEquals(ExportResultsPanel.IDLE, resultText());
        assertEquals(ExportResultsPanel.NO_DESTINATION, label("export.destination"));
        assertEquals("Results exported: 0", label("export.count"));
    }

    @Test
    void clickingAgainWhileAnExportRunsStartsNoSecondExport() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        dialogs.selection = folder.resolve("results.csv").toFile();
        behaviour = destination -> {
            started.countDown();
            try {
                assertTrue(release.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            presenter.prepareSuccessView(new ExportResultsResponse(destination, 0));
        };
        edt(() -> { button().doClick(0); return null; });
        assertTrue(started.await(10, TimeUnit.SECONDS));

        assertFalse(edt(() -> button().isEnabled()), "UiTasks disables the button while exporting");
        edt(() -> { button().doClick(0); return null; });
        release.countDown();
        waitIdle();

        assertEquals(1, dialogs.saveDialogs.size());
        assertEquals(1, useCaseCalls.size());

        clickExport();
        assertEquals(2, dialogs.saveDialogs.size(), "a later click starts a fresh request");
        assertEquals(2, useCaseCalls.size());
    }

    @Test
    void theHomeScreenAddsTheTabAndANewAccountSeesNoEarlierExport(@TempDir Path data) throws Exception {
        try (Application application = new Application(data, term -> {
            throw new AppException("Offline in tests.");
        }, Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/New_York")))) {
            AppController controller = application.controller();
            controller.setup("admin", "password123".toCharArray());
            controller.register("alice", "password123".toCharArray());
            JPanel root = edt(JPanel::new);
            UiTasks homeTasks = new UiTasks(root, statusMessages::add);
            Path target = folder.resolve("alice.csv");
            dialogs.selection = target.toFile();
            HomePanel alice = edt(() -> homePanel(root, controller, homeTasks, "alice"));

            selectExportTab(alice);
            waitIdle(homeTasks);
            assertTrue(dialogs.saveDialogs.isEmpty(), "opening the tab never opens a dialog");
            edt(() -> { ((AbstractButton) find(alice, "export.choose")).doClick(0); return null; });
            waitIdle(homeTasks);
            assertEquals("Saved to: " + target, edt(() -> ((JLabel) find(alice, "export.destination")).getText()));
            assertEquals(List.of("completed_at_utc,topic,correct,total"), Files.readAllLines(target));

            controller.register("bob", "password123".toCharArray());
            HomePanel bob = edt(() -> { root.removeAll(); return homePanel(root, controller, homeTasks, "bob"); });
            selectExportTab(bob);
            waitIdle(homeTasks);

            assertEquals(ExportResultsPanel.IDLE, edt(() -> ((JTextArea) find(bob, "export.result")).getText()));
            assertEquals(ExportResultsPanel.NO_DESTINATION,
                    edt(() -> ((JLabel) find(bob, "export.destination")).getText()));
            assertEquals(1, dialogs.saveDialogs.size());
        }
    }

    private HomePanel homePanel(JPanel root, AppController controller, UiTasks homeTasks, String username) {
        HomePanel home = new HomePanel(controller, homeTasks, new ViewModels.Account(username, false), () -> { },
                new SwingExportDestinationChooser(dialogs));
        root.add(home);
        return home;
    }

    private void selectExportTab(HomePanel home) throws Exception {
        edt(() -> {
            JTabbedPane tabs = (JTabbedPane) find(home, "home.tabs");
            int index = tabs.indexOfTab("Export results");
            assertTrue(index >= 0, "Export results tab is missing");
            assertTrue(tabs.getComponentAt(index) instanceof ExportResultsPanel);
            tabs.setSelectedIndex(index);
            return null;
        });
    }

    private AbstractButton button() {
        AbstractButton button = (AbstractButton) find(panel, "export.choose");
        assertNotNull(button, "Missing export button");
        return button;
    }

    private void clickExport() throws Exception {
        edt(() -> { button().doClick(0); return null; });
        waitIdle();
    }

    private void waitIdle() throws Exception { waitIdle(tasks); }

    private static void waitIdle(UiTasks uiTasks) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (edt(uiTasks::isBusy)) {
            if (System.nanoTime() > deadline) { fail("Export did not finish"); }
            Thread.sleep(10);
        }
        edt(() -> null); // let any already queued EDT updates run
    }

    private String resultText() throws Exception {
        return edt(() -> ((JTextArea) find(panel, "export.result")).getText());
    }

    private String label(String name) throws Exception {
        return edt(() -> ((JLabel) find(panel, name)).getText());
    }

    private String lastStatus() {
        return statusMessages.get(statusMessages.size() - 1);
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
