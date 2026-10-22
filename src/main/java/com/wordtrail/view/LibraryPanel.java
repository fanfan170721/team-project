package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

final class LibraryPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final boolean reviewOnly;
    private final JComboBox<String> topic = new JComboBox<>(new String[]{"All topics"});
    private final JTextField search = Ui.field("library.search", 18);
    private final JComboBox<String> order = new JComboBox<>(new String[]{
            "Word A-Z", "Reviews: low to high", "Reviews: high to low", "Recently reviewed"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Word", "Topic", "Definition", "Saved", "Reviews", "Last reviewed"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) { return column == 4 ? Integer.class : String.class; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextArea detail = Ui.display("Select a word to see its definition and example.");
    private final JButton save;
    private List<ViewModels.Word> words = new ArrayList<>();
    private boolean updating;
    private final CardLayout pages = new CardLayout();
    private final WordEditorPanel editor;

    LibraryPanel(AppController controller, UiTasks tasks, boolean reviewOnly) {
        this.controller = controller; this.tasks = tasks; this.reviewOnly = reviewOnly;
        setLayout(pages);
        String prefix = reviewOnly ? "review" : "library";
        JPanel page = Ui.page(reviewOnly ? "Review later" : "Your vocabulary",
                reviewOnly ? "Choose a review order, then study your saved words in Flashcards."
                        : "Browse shared and personal words, or add and customize your own vocabulary.");
        JPanel body = new JPanel(new BorderLayout(10, 10));
        JButton refresh = Ui.button(prefix + ".refresh", "Refresh");
        JButton find = Ui.button("library.find", "Search");
        JButton add = Ui.button(prefix + ".add", "Add my word");
        topic.setName(prefix + ".topic"); order.setName(prefix + ".order");
        JPanel toolbar = new JPanel(new GridLayout(0, 1));
        if (!reviewOnly) { toolbar.add(Ui.row(Ui.label("Topic", 14), topic, search, find, refresh)); }
        toolbar.add(reviewOnly ? Ui.row(refresh, Ui.label("Order", 14), order)
                : Ui.row(add, Ui.label("Order", 14), order));
        body.add(toolbar, BorderLayout.NORTH);
        table.setName(prefix + ".table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28); table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(360);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(65);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        detail.setName(prefix + ".detail"); detail.setRows(6);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JScrollPane(detail), BorderLayout.CENTER);
        save = Ui.button(prefix + ".save", reviewOnly ? "Remove from review later" : "Save for review later");
        JButton edit = Ui.button(prefix + ".edit", "Edit my definition");
        JButton lookup = Ui.button(prefix + ".lookup", "Look up selected word online");
        save.setEnabled(false); edit.setEnabled(false); lookup.setEnabled(false);
        bottom.add(Ui.row(save, edit, lookup), BorderLayout.SOUTH);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setColumnHeaderView(table.getTableHeader());
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottom);
        split.setResizeWeight(0.55); split.setDividerLocation(245);
        body.add(split, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, "list");
        editor = new WordEditorPanel(controller, tasks, prefix, () -> {
            pages.show(this, "list");
            if (!reviewOnly) {
                updating = true; topic.setSelectedIndex(0); search.setText(""); updating = false;
            }
            refresh();
        }, () -> pages.show(this, "list"));
        add(editor, "editor");
        refresh.addActionListener(event -> refresh());
        find.addActionListener(event -> refresh());
        search.addActionListener(event -> refresh());
        topic.addActionListener(event -> { if (!updating) { refresh(); } });
        order.addActionListener(event -> applyOrder());
        if (reviewOnly) { order.setSelectedIndex(1); }
        else { applyOrder(); }
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) { return; }
            ViewModels.Word word = selected();
            save.setEnabled(word != null); edit.setEnabled(word != null); lookup.setEnabled(word != null);
            if (word == null) { detail.setText(words.isEmpty() ? "No words found." : "Select a word."); return; }
            save.setText(word.saved ? "Remove from review later" : "Save for review later");
            showSavedDefinition(word);
        });
        add.addActionListener(event -> { editor.addWord(); pages.show(this, "editor"); });
        edit.addActionListener(event -> {
            ViewModels.Word word = selected();
            if (word != null) { editor.edit(word); pages.show(this, "editor"); }
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
                showSavedDefinition(word);
                tasks.run("Looking up " + word.term, () -> controller.lookup(word.term), entry -> {
                    detail.setText(entry.term + " — online definition\n\n" + entry.definition
                            + (entry.example.isBlank() ? "" : "\nExample: " + entry.example)
                            + "\nSource: " + entry.source + "\n\nYour saved definition (unchanged):\n" + word.definition);
                    detail.setCaretPosition(0);
                    tasks.message("Online definition loaded. Use Edit my definition to change your saved meaning.");
                });
            }
        });
    }

    private void showSavedDefinition(ViewModels.Word word) {
        detail.setText(word.term + "  |  " + word.topic + "  |  Completed reviews: " + word.reviewCount
                + (word.lastReviewed.isBlank() ? "" : "  |  Last: " + word.lastReviewed)
                + "\n\n" + word.definition + (word.example.isBlank() ? "" : "\nExample: " + word.example)
                + "\nSource: " + word.source);
        detail.setCaretPosition(0);
    }

    private void applyOrder() {
        int column = order.getSelectedIndex() == 0 ? 0 : order.getSelectedIndex() == 3 ? 5 : 4;
        SortOrder direction = order.getSelectedIndex() >= 2 ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(column, direction),
                new RowSorter.SortKey(0, SortOrder.ASCENDING)));
    }

    @Override public void refresh() {
        pages.show(this, "list");
        String chosen = Ui.topic(topic);
        String query = search.getText();
        String selectedId = selected() == null ? null : selected().id;
        tasks.run("Loading vocabulary", () -> reviewOnly ? controller.reviewList() : controller.browse(chosen, query), library -> {
            updating = true;
            try {
                Ui.topics(topic, library.topics);
                table.clearSelection();
                model.setRowCount(0);
                words = library.words;
                int selection = words.isEmpty() ? -1 : 0;
                for (int i = 0; i < words.size(); i++) {
                    ViewModels.Word word = words.get(i);
                    model.addRow(new Object[]{word.term, word.topic, word.definition, word.saved ? "Yes" : "",
                            word.reviewCount, word.lastReviewed});
                    if (word.id.equals(selectedId)) { selection = i; }
                }
                if (selection >= 0) {
                    int row = table.convertRowIndexToView(selection); table.setRowSelectionInterval(row, row);
                }
                else { detail.setText("No words found. Try another search or save words from the Vocabulary tab."); }
            }
            finally { updating = false; }
        });
    }

    private ViewModels.Word selected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= table.getRowCount()) { return null; }
        int index = table.convertRowIndexToModel(row);
        return index >= 0 && index < words.size() ? words.get(index) : null;
    }
}
