package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class ProgressPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JTextArea summary = Ui.display("");
    private final ProgressChart chart = new ProgressChart();
    private final DefaultTableModel history = new DefaultTableModel(new String[]{"Completed (local time)", "Topic", "Score", "Accuracy"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    ProgressPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks;
        JPanel page = Ui.page("Your progress", "Completed reviews accumulate; the daily chart counts distinct words each day.");
        JPanel body = new JPanel(new BorderLayout(10, 12));
        JPanel stats = new JPanel(new BorderLayout());
        summary.setName("progress.summary"); summary.setRows(3);
        summary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        JButton refresh = Ui.button("progress.refresh", "Refresh progress");
        stats.add(summary, BorderLayout.CENTER); stats.add(Ui.row(refresh), BorderLayout.EAST);
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
