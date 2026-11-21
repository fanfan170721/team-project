package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ExportResultsViewModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** UC-09 screen: the learner chooses a file and exports their own completed quiz results as CSV. */
final class ExportResultsPanel extends JPanel implements Refreshable {
    static final String IDLE = "Choose \"Export results (CSV)\" to save your completed quiz results.";
    static final String CANCELLED = "Export cancelled. No file was written or replaced.";
    static final String UNEXPECTED = "The export did not finish. No export was completed; see the message below.";
    static final String NO_DESTINATION = "Saved to: -";

    private final AppController controller;
    private final UiTasks tasks;
    private final ExportDestinationChooser chooser;
    private final JTextArea result = Ui.display(IDLE);
    private final JLabel destination = Ui.label(NO_DESTINATION, 14);
    private final JLabel count = Ui.label(countText(0), 14);

    ExportResultsPanel(AppController controller, UiTasks tasks, ExportDestinationChooser chooser) {
        super(new BorderLayout());
        this.controller = controller;
        this.tasks = tasks;
        this.chooser = chooser;
        JPanel page = Ui.page("Export results",
                "Save your own completed quizzes (UTC time, topic, correct, total) as a UTF-8 CSV file.");
        JButton export = Ui.button("export.choose", "Export results (CSV)...");
        result.setName("export.result");
        result.setRows(3);
        destination.setName("export.destination");
        count.setName("export.count");
        JPanel details = new JPanel(new GridLayout(0, 1, 0, 6));
        details.add(Ui.row(export));
        details.add(result);
        details.add(destination);
        details.add(count);
        JPanel body = new JPanel(new BorderLayout());
        body.add(details, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);
        add(page);
        export.addActionListener(event -> chooseAndExport());
    }

    /** Clears this screen only; it never opens a dialog or exports anything. */
    @Override
    public void refresh() {
        if (!tasks.isBusy()) {
            showState(IDLE, NO_DESTINATION, 0);
        }
    }

    private void chooseAndExport() {
        if (tasks.isBusy()) {
            return;
        }
        showState("Choosing where to save...", NO_DESTINATION, 0);
        Optional<Path> chosen = chooser.choose(this);
        if (chosen.isEmpty()) {
            showState(CANCELLED, NO_DESTINATION, 0);
            tasks.message(CANCELLED);
            return;
        }
        String target = chosen.get().toString();
        showState("Exporting to " + target + "...", NO_DESTINATION, 0);
        tasks.run("Exporting quiz results", () -> {
            try {
                return controller.exportResults(target);
            } catch (RuntimeException e) {
                // Runs before UiTasks reports the failure, so the pending text never looks like success.
                SwingUtilities.invokeLater(() -> showState(UNEXPECTED, NO_DESTINATION, 0));
                throw e;
            }
        }, this::show);
    }

    private void show(ExportResultsViewModel.Result outcome) {
        if (outcome.success) {
            showState(outcome.message, "Saved to: " + outcome.destination, outcome.exportedCount);
        } else {
            showState(outcome.message, NO_DESTINATION, 0);
        }
        tasks.message(outcome.message);
    }

    private void showState(String message, String destinationText, int exported) {
        result.setText(message);
        destination.setText(destinationText);
        count.setText(countText(exported));
    }

    private static String countText(int exported) {
        return "Results exported: " + exported;
    }
}
