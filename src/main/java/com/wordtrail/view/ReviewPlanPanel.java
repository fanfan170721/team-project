package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ReviewPlanViewModel;
import java.awt.BorderLayout;
import java.text.ParseException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class ReviewPlanPanel extends JPanel implements Refreshable {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;
    private static final String GENERATE_PROMPT = "Generate a plan for your selection.";

    private final UiTasks tasks;
    private final Supplier<List<String>> loadTopics;
    private final BiFunction<String, Integer, ReviewPlanViewModel.Result> generatePlan;
    private final JComboBox<Ui.TopicOption> topic = Ui.topicSelector();
    private final JSpinner limit = new JSpinner(new SpinnerNumberModel(10, MIN_LIMIT, MAX_LIMIT, 1));
    private final JFormattedTextField limitEditor = ((JSpinner.DefaultEditor) limit.getEditor()).getTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Word", "Definition", "Due date", "Reason"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel message = Ui.label(GENERATE_PROMPT, 14);
    private final JTextArea detail = Ui.display("");
    private boolean updatingTopics;

    ReviewPlanPanel(AppController controller, UiTasks tasks) {
        this(() -> controller.browse(null, "").topics, controller::generateReviewPlan, tasks);
    }

    ReviewPlanPanel(Supplier<List<String>> loadTopics,
                    BiFunction<String, Integer, ReviewPlanViewModel.Result> generatePlan, UiTasks tasks) {
        super(new BorderLayout());
        this.tasks = tasks;
        this.loadTopics = loadTopics;
        this.generatePlan = generatePlan;

        topic.setName("reviewPlan.topic");
        limit.setName("reviewPlan.limit");
        limitEditor.setFocusLostBehavior(JFormattedTextField.PERSIST);
        JButton generate = Ui.button("reviewPlan.generate", "Generate plan");
        JLabel topicLabel = Ui.label("Topic", 14);
        topicLabel.setLabelFor(topic);
        JLabel limitLabel = Ui.label("Word limit (1–20)", 14);
        limitLabel.setLabelFor(limit);
        JPanel filters = Ui.row(topicLabel, topic, limitLabel, limit, generate);

        table.setName("reviewPlan.table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        DefaultTableCellRenderer plainText = new DefaultTableCellRenderer();
        plainText.putClientProperty("html.disable", true);
        table.setDefaultRenderer(Object.class, plainText);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(390);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        message.setName("reviewPlan.message");
        detail.setName("reviewPlan.detail");
        detail.setRows(4);

        JPanel results = new JPanel(new BorderLayout(8, 8));
        results.add(message, BorderLayout.NORTH);
        results.add(new JScrollPane(table), BorderLayout.CENTER);
        results.add(new JScrollPane(detail), BorderLayout.SOUTH);
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.add(filters, BorderLayout.NORTH);
        body.add(results, BorderLayout.CENTER);
        JPanel page = Ui.page("Today's review plan", "Select a word to read its full definition and review reason.");
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        topic.addActionListener(event -> {
            if (!updatingTopics) {
                clearPlan(GENERATE_PROMPT);
            }
        });
        limitEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                clearPlan(GENERATE_PROMPT);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                clearPlan(GENERATE_PROMPT);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                clearPlan(GENERATE_PROMPT);
            }
        });
        generate.addActionListener(event -> generate());
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedWord();
            }
        });
    }

    @Override
    public void refresh() {
        clearPlan(GENERATE_PROMPT);
        tasks.run("Loading review topics", loadTopics, topics -> {
            updatingTopics = true;
            try {
                Ui.topics(topic, topics);
            } finally {
                updatingTopics = false;
            }
        });
    }

    private void generate() {
        if (tasks.isBusy()) {
            return;
        }
        clearPlan("");
        int requestedLimit;
        try {
            requestedLimit = commitLimit();
        } catch (NumberFormatException | ParseException failure) {
            message.setText("Enter a whole number from 1 to 20.");
            return;
        }
        message.setText("");
        String selectedTopic = Ui.topic(topic);
        tasks.run("Generating review plan", () -> generatePlan.apply(selectedTopic, requestedLimit), this::showPlan);
    }

    private int commitLimit() throws ParseException {
        String entered = limitEditor.getText();
        String trimmed = entered.strip();
        // The formatted editor may accept a numeric prefix, so validate the entire entry.
        int value = Integer.parseInt(trimmed);
        if (value < MIN_LIMIT || value > MAX_LIMIT) {
            throw new ParseException("The word limit is outside the allowed range.", 0);
        }
        if (!trimmed.equals(entered)) {
            limitEditor.setText(trimmed);
        }
        limit.commitEdit();
        return value;
    }

    private void showPlan(ReviewPlanViewModel.Result result) {
        if (result.success) {
            for (ReviewPlanViewModel.Row row : result.rows) {
                model.addRow(new Object[]{row.term, row.definition, row.dueDate, row.reason});
            }
            if (!result.rows.isEmpty()) {
                table.setRowSelectionInterval(0, 0);
                table.scrollRectToVisible(table.getCellRect(0, 0, true));
            }
        }
        message.setText(result.message);
    }

    private void clearPlan(String prompt) {
        table.clearSelection();
        model.setRowCount(0);
        message.setText(prompt);
        detail.setText("");
    }

    private void showSelectedWord() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detail.setText("");
            return;
        }
        int index = table.convertRowIndexToModel(row);
        detail.setText(model.getValueAt(index, 0) + "\n\n" + model.getValueAt(index, 1)
                + "\n\n" + model.getValueAt(index, 3));
        detail.setCaretPosition(0);
    }
}
