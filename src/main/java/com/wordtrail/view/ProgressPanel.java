package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class ProgressPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final ExportDialogs dialogs;
    private final JLabel exportStatus = Ui.label(" ", 14);
    private final JTextArea summary = Ui.display("");
    private final ProgressChart chart = new ProgressChart();
    private final DefaultTableModel history = new DefaultTableModel(new String[]{"Completed (local time)", "Topic", "Score", "Accuracy"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    ProgressPanel(AppController controller, UiTasks tasks) {
        this(controller, tasks, new ExportDialogs.Swing());
    }

    ProgressPanel(AppController controller, UiTasks tasks, ExportDialogs dialogs) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks; this.dialogs = dialogs;
        JPanel page = Ui.page("Your progress", "Completed reviews accumulate; the daily chart counts distinct words each day.");
        JPanel body = new JPanel(new BorderLayout(10, 12));
        JPanel stats = new JPanel(new BorderLayout());
        summary.setName("progress.summary"); summary.setRows(3);
        summary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        JButton refresh = Ui.button("progress.refresh", "Refresh progress");
        JButton export = Ui.button("progress.export", "Export results (CSV)");
        exportStatus.setName("progress.exportStatus");
        stats.add(summary, BorderLayout.CENTER); stats.add(Ui.row(refresh, export), BorderLayout.EAST);
        stats.add(exportStatus, BorderLayout.SOUTH);
        body.add(stats, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(8, 12));
        middle.add(chart, BorderLayout.NORTH);
        JTable table = new JTable(history);
        table.setName("progress.history"); table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        JPanel attempts = new JPanel(new BorderLayout(4, 6));
        attempts.add(Ui.label("Completed quizzes — newest first", 16), BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setColumnHeaderView(table.getTableHeader());
        attempts.add(tableScroll, BorderLayout.CENTER);
        middle.add(attempts, BorderLayout.CENTER);
        body.add(middle, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER); add(page);
        refresh.addActionListener(event -> refresh());
        export.addActionListener(event -> exportResults());
    }

    /** UC-09: choose a destination, confirm any replacement, then export only this account's results. */
    private void exportResults() {
        if (tasks.isBusy()) { return; }
        String chosen = dialogs.chooseDestination(this);
        if (chosen == null) {
            showExport(false, "Export cancelled. No file was written.");
            return;
        }
        String destination = withCsvExtension(chosen);
        if (new File(destination).exists() && !dialogs.confirmOverwrite(this, destination)) {
            showExport(false, "Export cancelled. The existing file was not replaced.");
            return;
        }
        tasks.run("Exporting quiz results", () -> controller.exportResults(destination),
                result -> showExport(result.success, result.message));
    }

    private void showExport(boolean success, String message) {
        exportStatus.setForeground(success ? Ui.ACCENT : Ui.INK);
        exportStatus.setText(message);
        tasks.message(message);
    }

    static String withCsvExtension(String path) {
        return path.toLowerCase(Locale.ROOT).endsWith(".csv") ? path : path + ".csv";
    }

    @Override public void refresh() { tasks.run("Loading progress", controller::dashboard, this::render); }

    private void render(ViewModels.Dashboard dashboard) {
        summary.setText("Words studied: " + dashboard.studied + " / " + dashboard.totalWords
                + "     Review later: " + dashboard.saved + "     Study streak: " + dashboard.streak + " day(s)"
                + "\nCompleted reviews: " + dashboard.reviews
                + "\nCompleted quizzes: " + dashboard.quizzes + "     Overall answer accuracy: "
                + (dashboard.quizzes == 0 ? "No completed quizzes yet" : dashboard.accuracy + "%"));
        chart.setDays(dashboard.dailyRows);
        history.setRowCount(0);
        dashboard.quizRows.forEach(history::addRow);
    }
}
