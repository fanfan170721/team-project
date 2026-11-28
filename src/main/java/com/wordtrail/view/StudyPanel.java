package com.wordtrail.view;

import com.wordtrail.adapter.StudyController;
import com.wordtrail.adapter.StudyViewModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Guided learning stays separate from free practice, sharing the account and saved progress. */
final class StudyPanel extends JPanel implements Refreshable {
    private final StudyController controller;
    private final UiTasks tasks;
    private final JTextArea summary = Ui.display("");
    private final JPanel content = new JPanel(new BorderLayout(10, 10));
    private final JTextArea mistakes = Ui.display("");
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Completed", "Mode", "Status", "Level", "First correct", "Net new"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable history = new JTable(historyModel);
    private final JSpinner goal = new JSpinner(new SpinnerNumberModel(100, 1, 1000000, 10));
    private final JTabbedPane pages = new JTabbedPane();
    private StudyViewModel model;
    private boolean assessing;
    private final PronunciationControls audio;

    StudyPanel(StudyController controller, UiTasks tasks) { this(controller, tasks, null); }
    StudyPanel(StudyController controller, UiTasks tasks, com.wordtrail.adapter.PronunciationController pronunciation) {
        super(new BorderLayout());
        audio = new PronunciationControls(pronunciation);
        this.controller = controller;
        this.tasks = tasks;
        JPanel page = Ui.page("Guided study", "Learn 20 words. Revisit mistakes. Keep every step.");
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        summary.setName("study.summary");
        summary.setRows(2);
        summary.setFont(summary.getFont().deriveFont(13f));
        body.add(summary, BorderLayout.NORTH);
        content.setOpaque(false);
        pages.setName("study.pages");
        pages.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 13));
        pages.addTab("Current round", content);
        mistakes.setName("study.mistakes");
        pages.addTab("Mistake notebook", Ui.scroll(mistakes));
        JPanel historyPage = new JPanel(new BorderLayout(0, 8));
        history.setName("study.history");
        Ui.styleTable(history);
        javax.swing.JScrollPane historyScroll = Ui.scroll(history);
        historyScroll.setColumnHeaderView(history.getTableHeader());
        historyPage.add(historyScroll);
        JButton replay = Ui.button("study.replay", "Replay selected round");
        replay.addActionListener(event -> {
            int row = history.getSelectedRow();
            if (model == null || row < 0 || !model.history.get(row).completed) {
                tasks.message("Select a completed round in history.");
                return;
            }
            assessing = false;
            pages.setSelectedIndex(0);
            run(() -> controller.replay(model.history.get(row).id));
        });
        historyPage.add(replay, BorderLayout.SOUTH);
        pages.addTab("Round history", historyPage);
        pages.addTab("Word bank", new WordBankPanel(controller, pronunciation));
        pages.addChangeListener(event -> { if (pronunciation != null) { pronunciation.stop(); } });
        body.add(pages, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 5));
        footer.setOpaque(false);
        JButton start = Ui.button("study.start", "Start / resume round");
        start.addActionListener(event -> {
            assessing = false;
            pages.setSelectedIndex(0);
            run(controller::start);
        });
        JButton placement = Ui.button("study.assess", "Retake placement");
        placement.addActionListener(event -> {
            assessing = true;
            pages.setSelectedIndex(0);
            run(controller::assess);
        });
        JButton setGoal = Ui.button("study.goal.save", "Set new-word goal");
        goal.setName("study.goal");
        setGoal.addActionListener(event -> {
            try {
                goal.commitEdit();
                run(() -> controller.goal((Integer) goal.getValue()));
            } catch (java.text.ParseException failure) {
                tasks.message("Enter a whole-number goal from 1 to 1000000.");
            }
        });
        footer.add(Ui.row(start, placement, goal, setGoal), BorderLayout.NORTH);
        body.add(footer, BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER);
        add(page);
    }

    @Override public void refresh() { run(controller::show); }

    private void run(Supplier<StudyViewModel> action) {
        tasks.run("Saving guided study", action, this::render);
    }

    private void render(StudyViewModel value) {
        model = value;
        summary.setText(value.summary);
        tasks.message(value.feedback.isEmpty() ? "Guided study ready." : value.feedback);
        mistakes.setText(value.mistakes);
        mistakes.setCaretPosition(0);
        goal.setValue(value.goal);
        historyModel.setRowCount(0);
        value.history.forEach(row -> historyModel.addRow(row.cells));
        content.removeAll();
        StudyViewModel.RoundView round = assessing ? value.assessment : value.round;
        audio.word(round == null ? "" : round.word);
        if (round == null) {
            content.add(Ui.display("Start a 20-word round, or take a sample-level placement.\n"
                    + "Your progress is saved after every action. Completed learning joins your library and results."));
        } else {
            content.add(Ui.muted(round.heading, 13), BorderLayout.NORTH);
            if (round.phase.equals("OVERVIEW")) { overview(round); }
            else if (round.phase.equals("COMPLETED")) { completed(round); }
            else { question(round); }
        }
        revalidate();
        repaint();
    }

    private void overview(StudyViewModel.RoundView round) {
        DefaultTableModel rows = new DefaultTableModel(new String[]{"Already known", "Word", "Definition"}, 0) {
            @Override public Class<?> getColumnClass(int column) { return column == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int row, int column) { return column == 0; }
            @Override public void setValueAt(Object value, int row, int column) {
                run(() -> controller.known(round.token, row, Boolean.TRUE.equals(value)));
            }
        };
        round.overview.forEach(rows::addRow);
        JTable table = new JTable(rows);
        table.setName("study.overview");
        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(600);
        javax.swing.JScrollPane overviewScroll = Ui.scroll(table);
        overviewScroll.setColumnHeaderView(table.getTableHeader());
        content.add(overviewScroll, BorderLayout.CENTER);
        JButton begin = Ui.primaryButton("study.advance", "Learn these words");
        begin.addActionListener(event -> run(() -> controller.advance(round.token)));
        content.add(begin, BorderLayout.SOUTH);
    }

    private void question(StudyViewModel.RoundView round) {
        JPanel card = Ui.surface(new BorderLayout(8, 6), 8);
        JTextArea term = Ui.display(round.word);
        term.setName("study.word");
        term.setFont(term.getFont().deriveFont(java.awt.Font.BOLD, 25));
        term.setRows(1);
        JPanel wordHeader = new JPanel(new BorderLayout(8, 0));
        wordHeader.setOpaque(false);
        wordHeader.add(Ui.scroll(term), BorderLayout.CENTER);
        audio.setPreferredSize(new java.awt.Dimension(400, 30));
        wordHeader.add(audio, BorderLayout.EAST);
        card.add(wordHeader, BorderLayout.NORTH);
        if (round.phase.equals("TEST")) {
            JPanel options = new JPanel(new GridLayout(2, 2, 8, 6));
            options.setOpaque(false);
            ButtonGroup group = new ButtonGroup();
            JRadioButton[] choices = new JRadioButton[4];
            for (int index = 0; index < 4; index++) {
                choices[index] = new JRadioButton(String.valueOf((char) ('A' + index)));
                choices[index].setName("study.choice" + index);
                choices[index].setOpaque(false);
                choices[index].getAccessibleContext().setAccessibleDescription(round.options.get(index));
                group.add(choices[index]);
                JPanel answer = Ui.surface(new BorderLayout(5, 0), 4);
                answer.add(choices[index], BorderLayout.WEST);
                JTextArea text = Ui.display(round.options.get(index));
                text.setRows(2);
                text.setName("study.choiceText" + index);
                answer.add(Ui.scroll(text));
                options.add(answer);
            }
            card.add(options);
            JButton submit = Ui.primaryButton("study.submit", "Submit answer");
            submit.addActionListener(event -> {
                for (int index = 0; index < 4; index++) {
                    if (choices[index].isSelected()) {
                        int selected = index;
                        run(() -> controller.answer(round.token, selected, assessing));
                        return;
                    }
                }
                tasks.message("Select an answer, or choose I don't know.");
            });
            JButton unknown = Ui.button("study.unknown", "I don't know");
            unknown.addActionListener(event -> run(() -> controller.answer(round.token, 4, assessing)));
            card.add(Ui.row(submit, unknown), BorderLayout.SOUTH);
        } else {
            JTextArea meaning = Ui.display(round.definition);
            meaning.setName("study.meaning");
            card.add(Ui.scroll(meaning));
            JButton next = Ui.primaryButton("study.advance", "Next learning step");
            next.addActionListener(event -> run(() -> controller.advance(round.token)));
            card.add(next, BorderLayout.SOUTH);
        }
        content.add(card);
    }

    private void completed(StudyViewModel.RoundView round) {
        content.add(Ui.scroll(Ui.display(round.result)));
        if (assessing) {
            JPanel actions = new JPanel(new GridLayout(0, 1, 0, 8));
            actions.setOpaque(false);
            actions.add(Ui.display("Applying this result replaces unfinished learning without completion credit.\n"
                    + "Completed history, mastery and your goal stay intact. The English bank spans 15 uncalibrated learning levels."));
            JButton adopt = Ui.primaryButton("study.adopt", "Apply level and start fresh 20 words");
            adopt.setEnabled(!round.adopted);
            adopt.addActionListener(event -> {
                tasks.run("Applying placement", () -> controller.adopt(round.token), value -> {
                    assessing = false;
                    render(value);
                });
            });
            actions.add(adopt);
            content.add(actions, BorderLayout.SOUTH);
        }
    }
}
