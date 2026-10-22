package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class LibraryPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final boolean reviewOnly;
    private final JComboBox<String> topic = new JComboBox<>(new String[]{"All topics"});
    private final JTextField search = Ui.field("library.search", 18);
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Word", "Topic", "Definition", "Saved"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea detail = Ui.display("Select a word to see its definition and example.");
    private final JButton save;
    private List<ViewModels.Word> words = new ArrayList<>();
    private boolean updating;

    LibraryPanel(AppController controller, UiTasks tasks, boolean reviewOnly) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks; this.reviewOnly = reviewOnly;
        String prefix = reviewOnly ? "review" : "library";
        JPanel page = Ui.page(reviewOnly ? "Review later" : "Your vocabulary",
                reviewOnly ? "Words you saved for another look. Study them in the Flashcards tab."
                        : "Browse by topic, search definitions, or look up a word online.");
        JPanel body = new JPanel(new BorderLayout(10, 10));
        JButton refresh = Ui.button(prefix + ".refresh", "Refresh");
        JButton find = Ui.button("library.find", "Search");
        topic.setName(prefix + ".topic");
        JPanel toolbar = reviewOnly ? Ui.row(refresh) : Ui.row(Ui.label("Topic", 14), topic, search, find, refresh);
        body.add(toolbar, BorderLayout.NORTH);
        table.setName(prefix + ".table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(125);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(470);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);
        detail.setName(prefix + ".detail");
        detail.setRows(6);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JScrollPane(detail), BorderLayout.CENTER);
        save = Ui.button(prefix + ".save", reviewOnly ? "Remove from review later" : "Save for review later");
        save.setEnabled(false);
        JButton lookup = Ui.button(prefix + ".lookup", "Look up selected word online");
        lookup.setEnabled(false);
        bottom.add(Ui.row(save, lookup), BorderLayout.SOUTH);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setColumnHeaderView(table.getTableHeader());
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottom);
        split.setResizeWeight(0.58); split.setDividerLocation(270);
        body.add(split, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER); add(page);
        refresh.addActionListener(event -> refresh());
        find.addActionListener(event -> refresh());
        search.addActionListener(event -> refresh());
        topic.addActionListener(event -> { if (!updating) { refresh(); } });
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) { return; }
            ViewModels.Word word = selected();
            save.setEnabled(word != null); lookup.setEnabled(word != null);
            if (word == null) { detail.setText(words.isEmpty() ? "No words found." : "Select a word."); return; }
            save.setText(word.saved ? "Remove from review later" : "Save for review later");
            detail.setText(word.term + "  |  " + word.topic + "\n\n" + word.definition
                    + (word.example.isBlank() ? "" : "\nExample: " + word.example) + "\nSource: " + word.source);
            detail.setCaretPosition(0);
        });
        save.addActionListener(event -> {
            ViewModels.Word word = selected();
            if (word != null) {
                tasks.run("Saving review list", () -> { controller.saveWord(word.id, !word.saved); return true; },
                        ignored -> refresh());
            }
        });
        lookup.addActionListener(event -> {
            ViewModels.Word word = selected();
            if (word != null) {
                tasks.run("Looking up " + word.term, () -> controller.lookup(word.term), entry -> {
                    detail.setText(entry.term + " — live dictionary\n\n" + entry.definition
                            + (entry.example.isBlank() ? "" : "\nExample: " + entry.example) + "\nSource: " + entry.source);
                    detail.setCaretPosition(0);
                });
            }
        });
    }

    @Override public void refresh() {
        String chosen = Ui.topic(topic);
        String query = search.getText();
        String selectedId = selected() == null ? null : selected().id;
        tasks.run("Loading vocabulary", () -> reviewOnly ? controller.reviewList() : controller.browse(chosen, query), library -> {
            updating = true;
            Ui.topics(topic, library.topics);
            words = library.words;
            model.setRowCount(0);
            int selection = words.isEmpty() ? -1 : 0;
            for (int i = 0; i < words.size(); i++) {
                ViewModels.Word word = words.get(i);
                model.addRow(new Object[]{word.term, word.topic, word.definition, word.saved ? "Yes" : ""});
                if (word.id.equals(selectedId)) { selection = i; }
            }
            if (selection >= 0) { int row = table.convertRowIndexToView(selection); table.setRowSelectionInterval(row, row); }
            else { detail.setText("No words found. Try another search or save words from the Vocabulary tab."); }
            updating = false;
        });
    }

    private ViewModels.Word selected() {
        int row = table.getSelectedRow();
        if (row < 0) { return null; }
        int index = table.convertRowIndexToModel(row);
        return index < words.size() ? words.get(index) : null;
    }
}
