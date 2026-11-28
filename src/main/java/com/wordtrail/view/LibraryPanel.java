package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Vocabulary browser and saved review list, sharing the same selection workflow. */
final class LibraryPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final PronunciationControls audio;
    private final UiTasks tasks;
    private final boolean reviewOnly;
    private final JComboBox<Ui.TopicOption> topic = Ui.topicSelector();
    private final JTextField search = Ui.field("library.search", 18);
    private final JComboBox<String> order = new JComboBox<>(new String[]{
        "Word A-Z", "Reviews: low to high", "Reviews: high to low", "Recently reviewed"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Word", "Topic", "Definition", "Saved", "Reviews", "Last reviewed"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 4 ? Integer.class : column == 5 ? ViewModels.ReviewTime.class : String.class;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextArea detail = Ui.display("Select a word to see its definition and example.");
    private final JLabel selectedTerm = Ui.heading("No word selected", 27);
    private final JLabel selectedMeta = Ui.muted("Choose a word from the list above.", 12);
    private final JLabel resultCount = Ui.heading("0 words shown", 14);
    private final JLabel detailKind = Ui.muted("SAVED DEFINITION", 11);
    private final JButton save;
    private final JButton edit;
    private final JButton lookup;
    private List<ViewModels.Word> words = new ArrayList<>();
    private boolean updating;
    private final CardLayout pages = new CardLayout();
    private final WordEditorPanel editor;

    LibraryPanel(AppController controller, UiTasks tasks, boolean reviewOnly) {
        this.controller = controller;
        audio = new PronunciationControls(controller.audio());
        this.tasks = tasks;
        this.reviewOnly = reviewOnly;
        setLayout(pages);
        String prefix = reviewOnly ? "review" : "library";
        JPanel page = Ui.page(reviewOnly ? "Review later" : "Your vocabulary",
                reviewOnly ? "A little more practice for the words you want to remember."
                        : "Discover a word. Make it yours. Keep learning.");
        page.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JButton refresh = Ui.button(prefix + ".refresh", "Refresh");
        JButton find = Ui.button("library.find", "Search");
        JButton add = Ui.primaryButton(prefix + ".add", "+  Add my word");
        save = Ui.button(prefix + ".save", reviewOnly ? "Remove from review later" : "Save for review later");
        edit = Ui.button(prefix + ".edit", "Edit my definition");
        lookup = Ui.button(prefix + ".lookup", "Look up online");
        lookup.setToolTipText("Look up the selected word without changing your saved definition");
        save.setEnabled(false);
        edit.setEnabled(false);
        lookup.setEnabled(false);
        topic.setName(prefix + ".topic");
        order.setName(prefix + ".order");
        selectedTerm.setName(prefix + ".selectedTerm");
        selectedMeta.setName(prefix + ".selectedMeta");
        resultCount.setName(prefix + ".resultCount");
        Ui.styleCombo(order);
        if (!reviewOnly) {
            Component heading = ((BorderLayout) page.getLayout()).getLayoutComponent(BorderLayout.NORTH);
            JPanel pageHeader = transparent(new BorderLayout(16, 0));
            pageHeader.add(heading, BorderLayout.CENTER);
            JPanel action = transparent(new FlowLayout(FlowLayout.RIGHT, 0, 6));
            action.add(add);
            pageHeader.add(action, BorderLayout.EAST);
            page.add(pageHeader, BorderLayout.NORTH);
        }
        JPanel body = transparent(new BorderLayout(0, 6));
        if (!reviewOnly) {
            body.add(filters(find, refresh), BorderLayout.NORTH);
        }
        JPanel collection = collection(prefix, refresh);
        JPanel definition = definition(prefix);
        collection.setMinimumSize(new Dimension(0, 112));
        definition.setMinimumSize(new Dimension(0, 196));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, collection, definition) {
            @Override
            public void doLayout() {
                // Reserve room for both cards when the window shrinks or is first laid out.
                int available = getHeight() - getDividerSize();
                int limit = available - getBottomComponent().getMinimumSize().height;
                int desired = getDividerLocation();
                if (available > 0) {
                    int position = Math.max(getTopComponent().getMinimumSize().height, Math.min(desired, limit));
                    if (position != desired) {
                        setDividerLocation(position);
                    }
                }
                super.doLayout();
            }
        };
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.55);
        split.setDividerLocation(230);
        body.add(split, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, "list");
        editor = new WordEditorPanel(controller, tasks, prefix, result -> {
            pages.show(this, "list");
            if (!reviewOnly) {
                updating = true;
                topic.setSelectedIndex(0);
                search.setText("");
                updating = false;
            }
            refresh(result.wordId, result.message);
        }, () -> pages.show(this, "list"));
        add(editor, "editor");
        refresh.addActionListener(event -> refresh());
        find.addActionListener(event -> refresh());
        search.addActionListener(event -> refresh());
        topic.addActionListener(event -> {
            if (!updating) {
                refresh();
            }
        });
        order.addActionListener(event -> applyOrder());
        if (reviewOnly) {
            order.setSelectedIndex(1);
        } else {
            applyOrder();
        }
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelection();
            }
        });
        add.addActionListener(event -> {
            editor.addWord();
            pages.show(this, "editor");
        });
        edit.addActionListener(event -> {
            ViewModels.Word word = selected();
        audio.word(word == null ? "" : word.term);
            if (word != null) {
                editor.edit(word);
                pages.show(this, "editor");
            }
        });
        save.addActionListener(event -> {
            ViewModels.Word word = selected();
        audio.word(word == null ? "" : word.term);
            if (word != null) {
                tasks.run("Saving review list", () -> {
                    controller.saveWord(word.id, !word.saved);
                    return true;
                }, ignored -> refresh());
            }
        });
        lookup.addActionListener(event -> lookupSelected());
    }

    private JPanel filters(JButton find, JButton refresh) {
        JPanel filters = transparent(new BorderLayout(16, 0));
        JPanel topicFilter = transparent(new BorderLayout(10, 0));
        JLabel topicLabel = Ui.muted("Topic", 13);
        topicLabel.setLabelFor(topic);
        topicFilter.add(topicLabel, BorderLayout.WEST);
        topic.setPreferredSize(new Dimension(175, 36));
        topicFilter.add(topic, BorderLayout.CENTER);
        filters.add(topicFilter, BorderLayout.WEST);
        JPanel query = transparent(new BorderLayout(10, 0));
        JLabel searchLabel = Ui.muted("Find a word", 13);
        searchLabel.setLabelFor(search);
        search.getAccessibleContext().setAccessibleName("Find a word");
        search.setToolTipText("Search your vocabulary, then press Enter or Search");
        query.add(searchLabel, BorderLayout.WEST);
        query.add(search, BorderLayout.CENTER);
        filters.add(query, BorderLayout.CENTER);
        JPanel actions = transparent(new GridLayout(1, 2, 8, 0));
        actions.add(find);
        actions.add(refresh);
        filters.add(actions, BorderLayout.EAST);
        return filters;
    }

    private JPanel collection(String prefix, JButton refresh) {
        JPanel collection = Ui.surface(new BorderLayout(0, 6), 10);
        JPanel toolbar = transparent(new BorderLayout(10, 0));
        toolbar.add(resultCount, BorderLayout.WEST);
        JPanel sorting = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        if (reviewOnly) {
            sorting.add(refresh);
        }
        JLabel sortLabel = Ui.muted("Sort by", 12);
        sortLabel.setLabelFor(order);
        sorting.add(sortLabel);
        sorting.add(order);
        toolbar.add(sorting, BorderLayout.EAST);
        collection.add(toolbar, BorderLayout.NORTH);
        table.setName(prefix + ".table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        Ui.styleTable(table);
        int[] widths = {140, 115, 320, 85, 85, 145};
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScroll = Ui.scroll(table);
        tableScroll.setColumnHeaderView(table.getTableHeader());
        collection.add(tableScroll, BorderLayout.CENTER);
        return collection;
    }

    private JPanel definition(String prefix) {
        JPanel definition = Ui.surface(new BorderLayout(0, 8), 10);
        JPanel title = transparent(new BorderLayout(0, 5));
        title.add(detailKind, BorderLayout.NORTH);
        title.add(selectedTerm, BorderLayout.CENTER);
        title.add(selectedMeta, BorderLayout.SOUTH);
        JPanel header = transparent(new BorderLayout(12, 0));
        header.add(title, BorderLayout.CENTER);
        JPanel actions = transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.add(save);
        actions.add(edit);
        actions.add(lookup);
        header.add(actions, BorderLayout.EAST);
        definition.add(header, BorderLayout.NORTH);
        detail.setName(prefix + ".detail");
        detail.setRows(3);
        definition.add(Ui.scroll(detail), BorderLayout.CENTER);
        definition.add(audio, BorderLayout.SOUTH);
        return definition;
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private void showSelection() {
        ViewModels.Word word = selected();
        audio.word(word == null ? "" : word.term);
        save.setEnabled(word != null);
        edit.setEnabled(word != null);
        lookup.setEnabled(word != null);
        if (word == null) {
            selectedTerm.setText("No word selected");
            selectedMeta.setText(words.isEmpty()
                    ? "Your next word starts here." : "Choose a word from the list above.");
            detailKind.setText("WORD DETAILS");
            detail.setText(words.isEmpty() ? (reviewOnly
                    ? "Your review list is empty. Save a word from Vocabulary to come back to it here."
                    : "No matching words. Try another search or topic, or add a word of your own.")
                    : "Select a word to see its definition and example.");
            save.setText(reviewOnly ? "Remove from review later" : "Save for review later");
            detail.setCaretPosition(0);
            return;
        }
        save.setText(word.saved ? "Remove from review later" : "Save for review later");
        showSavedDefinition(word);
    }

    private void showSavedDefinition(ViewModels.Word word) {
        selectedTerm.setText(word.term);
        selectedMeta.setText(word.topic + "  /  " + word.reviewCount + " completed reviews"
                + (word.lastReviewed.isBlank() ? "" : "  /  Last: " + word.lastReviewed));
        detailKind.setText(word.saved ? "SAVED FOR REVIEW LATER" : "SAVED DEFINITION");
        detail.setText(word.definition + (word.example.isBlank() ? "" : "\n\nExample: " + word.example)
                + "\n\nSource: " + word.source + wordMetadata(word));
        detail.setCaretPosition(0);
    }

    private static String wordMetadata(ViewModels.Word word) {
        // Labels can abbreviate long values; the scrollable details always retain the full text.
        return "\n\nWord: " + word.term + "\nTopic: " + word.topic + "\nCompleted reviews: " + word.reviewCount
                + (word.lastReviewed.isBlank() ? "" : "\nLast reviewed: " + word.lastReviewed);
    }

    private void lookupSelected() {
        ViewModels.Word word = selected();
        audio.word(word == null ? "" : word.term);
        if (word == null) {
            return;
        }
        showSavedDefinition(word);
        tasks.run("Looking up " + word.term, () -> controller.lookup(word.term), entry -> {
            detailKind.setText("ONLINE DICTIONARY / YOUR SAVED MEANING IS UNCHANGED");
            detail.setText(entry.term + " — online definition\n\n" + entry.definition
                    + (entry.example.isBlank() ? "" : "\nExample: " + entry.example)
                    + "\nSource: " + entry.source + "\n\nYour saved definition (unchanged):\n" + word.definition
                    + wordMetadata(word));
            detail.setCaretPosition(0);
            tasks.message("Online definition loaded. Use Edit my definition to change your saved meaning.");
        });
    }

    private void applyOrder() {
        int column = order.getSelectedIndex() == 0 ? 0 : order.getSelectedIndex() == 3 ? 5 : 4;
        SortOrder direction = order.getSelectedIndex() >= 2 ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(column, direction),
                new RowSorter.SortKey(0, SortOrder.ASCENDING)));
    }

    @Override
    public void refresh() {
        refresh(selected() == null ? null : selected().id, null);
    }

    private void refresh(String selectedId, String completionMessage) {
        pages.show(this, "list");
        String chosen = Ui.topic(topic);
        String query = search.getText();
        tasks.run("Loading vocabulary", () -> reviewOnly ? controller.reviewList() : controller.browse(chosen, query),
                library -> {
                    updating = true;
                    try {
                        Ui.topics(topic, library.topics);
                        table.clearSelection();
                        model.setRowCount(0);
                        words = library.words;
                        resultCount.setText(words.size() + (words.size() == 1 ? " word shown" : " words shown"));
                        int selection = words.isEmpty() ? -1 : 0;
                        for (int i = 0; i < words.size(); i++) {
                            ViewModels.Word word = words.get(i);
                            model.addRow(new Object[]{word.term, word.topic, word.definition, word.saved ? "Yes" : "",
                                word.reviewCount, word.reviewTime});
                            if (word.id.equals(selectedId)) {
                                selection = i;
                            }
                        }
                        if (selection >= 0) {
                            int row = table.convertRowIndexToView(selection);
                            table.setRowSelectionInterval(row, row);
                            table.scrollRectToVisible(table.getCellRect(row, 0, true));
                        } else {
                            showSelection();
                        }
                    } finally {
                        updating = false;
                    }
                    if (completionMessage != null) {
                        tasks.message(completionMessage);
                    }
                });
    }

    private ViewModels.Word selected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= table.getRowCount()) {
            return null;
        }
        int index = table.convertRowIndexToModel(row);
        return index >= 0 && index < words.size() ? words.get(index) : null;
    }
}
