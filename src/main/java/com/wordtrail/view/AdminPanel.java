package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

final class AdminPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JTextField term = Ui.field("admin.term", 28);
    private final JTextField topic = Ui.field("admin.topic", 28);
    private final JTextArea definition = Ui.textArea("admin.definition", 4);
    private final JTextArea example = Ui.textArea("admin.example", 3);
    private final JTextField source = Ui.field("admin.source", 28);
    private final JComboBox<ViewModels.Word> linkedWord = new JComboBox<>();
    private final JTextArea prompt = Ui.textArea("admin.prompt", 2);
    private final JTextArea[] options = new JTextArea[4];
    private final JComboBox<String> correct = new JComboBox<>(new String[]{"Choice 1", "Choice 2", "Choice 3", "Choice 4"});
    private boolean updating;

    AdminPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks;
        JPanel page = Ui.page("Content administration", "Add a word, then add a four-choice question linked to that word.");
        JTabbedPane forms = new JTabbedPane();
        forms.setName("admin.tabs");
        forms.addTab("Add vocabulary", wordForm()); forms.addTab("Add quiz question", questionForm());
        page.add(forms, BorderLayout.CENTER); add(page);
    }

    private JPanel wordForm() {
        JPanel form = new JPanel(new GridBagLayout());
        source.setText("Admin-created content");
        JButton lookup = Ui.button("admin.lookup", "Fill from online dictionary");
        Ui.formRow(form, 0, "English word", Ui.row(term, lookup));
        Ui.formRow(form, 1, "Topic (existing or new)", topic);
        Ui.formRow(form, 2, "Definition", new JScrollPane(definition));
        Ui.formRow(form, 3, "Example (optional)", new JScrollPane(example));
        Ui.formRow(form, 4, "Source / attribution", source);
        JButton add = Ui.button("admin.addWord", "Add vocabulary word");
        Ui.formRow(form, 5, "", add);
        Ui.formRow(form, 6, "", Ui.display("Online lookup fills the definition, example and source.\nReview the text before saving. You can also enter all content manually."));
        lookup.addActionListener(event -> {
            String word = term.getText();
            tasks.run("Looking up definition", () -> controller.lookup(word), entry -> {
                term.setText(entry.term); definition.setText(entry.definition);
                example.setText(entry.example); source.setText(entry.source);
            });
        });
        add.addActionListener(event -> {
            String word = term.getText(), category = topic.getText(), meaning = definition.getText();
            String usage = example.getText(), attribution = source.getText();
            tasks.run("Adding vocabulary", () -> { controller.addWord(word, category, meaning, usage, attribution); return true; }, ignored -> {
                term.setText(""); definition.setText(""); example.setText(""); source.setText("Admin-created content");
                refresh();
                tasks.message("Vocabulary word added. You can now create its quiz question.");
            });
        });
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel questionForm() {
        JPanel form = new JPanel(new GridBagLayout());
        linkedWord.setName("admin.linkedWord"); correct.setName("admin.correct");
        Ui.formRow(form, 0, "Vocabulary word", linkedWord);
        Ui.formRow(form, 1, "Question prompt", new JScrollPane(prompt));
        for (int i = 0; i < options.length; i++) {
            options[i] = Ui.textArea("admin.option" + i, 2);
            Ui.formRow(form, i + 2, "Choice " + (i + 1), new JScrollPane(options[i]));
        }
        Ui.formRow(form, 6, "Correct answer", correct);
        JButton add = Ui.button("admin.addQuestion", "Add quiz question");
        Ui.formRow(form, 7, "", add);
        linkedWord.addActionListener(event -> {
            if (!updating && linkedWord.getSelectedItem() != null) {
                ViewModels.Word word = (ViewModels.Word) linkedWord.getSelectedItem();
                prompt.setText("What does \"" + word.term + "\" mean?");
            }
        });
        add.addActionListener(event -> {
            ViewModels.Word word = (ViewModels.Word) linkedWord.getSelectedItem();
            String wordId = word == null ? null : word.id;
            String question = prompt.getText();
            List<String> choices = new ArrayList<>();
            for (JTextArea option : options) { choices.add(option.getText()); }
            int answer = correct.getSelectedIndex();
            tasks.run("Adding quiz question", () -> { controller.addQuestion(wordId, question, choices, answer); return true; }, ignored -> {
                for (JTextArea option : options) { option.setText(""); }
                tasks.message("Quiz question added.");
            });
        });
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);
        return wrapper;
    }

    @Override public void refresh() {
        String previousId = linkedWord.getSelectedItem() == null ? null
                : ((ViewModels.Word) linkedWord.getSelectedItem()).id;
        tasks.run("Loading admin content", () -> controller.browse(null, ""), library -> {
            updating = true;
            linkedWord.removeAllItems();
            for (ViewModels.Word word : library.words) {
                linkedWord.addItem(word);
                if (word.id.equals(previousId)) { linkedWord.setSelectedItem(word); }
            }
            updating = false;
            if (prompt.getText().isBlank() && linkedWord.getSelectedItem() != null) {
                ViewModels.Word word = (ViewModels.Word) linkedWord.getSelectedItem();
                prompt.setText("What does \"" + word.term + "\" mean?");
            }
        });
    }
}
