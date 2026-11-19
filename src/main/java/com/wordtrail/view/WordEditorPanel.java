package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

/** Inline editor keeps save/cancel behavior testable without modal desktop windows. */
final class WordEditorPanel extends JPanel {
    private final AppController controller;
    private final UiTasks tasks;
    private final Consumer<PersonalVocabularyViewModel.SaveResult> onSaved;
    private final JLabel title = Ui.label("Add my word", 24);
    private final JTextField term = Ui.field("wordEditor.term", 28);
    private final JTextField topic = Ui.field("wordEditor.topic", 28);
    private final JTextArea definition = Ui.textArea("wordEditor.definition", 5);
    private final JTextArea example = Ui.textArea("wordEditor.example", 3);
    private final JTextField source = Ui.field("wordEditor.source", 28);
    private final JButton lookup = Ui.button("wordEditor.lookup", "Fill from online dictionary");
    private ViewModels.Word editing;

    WordEditorPanel(AppController controller, UiTasks tasks, String prefix,
                    Consumer<PersonalVocabularyViewModel.SaveResult> onSaved, Runnable onCancel) {
        super(new BorderLayout(12, 12));
        this.controller = controller; this.tasks = tasks; this.onSaved = onSaved;
        term.setName(prefix + ".editor.term"); topic.setName(prefix + ".editor.topic");
        definition.setName(prefix + ".editor.definition"); example.setName(prefix + ".editor.example");
        source.setName(prefix + ".editor.source"); lookup.setName(prefix + ".editor.lookup");
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 7));
        header.add(title);
        header.add(Ui.label("Saved to your account only. Other learners keep their own definitions.", 14));
        add(header, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        Ui.formRow(form, 0, "Word", term);
        Ui.formRow(form, 1, "Topic (existing or new)", topic);
        Ui.formRow(form, 2, "My definition", new JScrollPane(definition));
        Ui.formRow(form, 3, "Example (for new words)", new JScrollPane(example));
        Ui.formRow(form, 4, "Source (for new words)", source);
        Ui.formRow(form, 5, "", lookup);
        add(new JScrollPane(form), BorderLayout.CENTER);
        JButton save = Ui.button("wordEditor.save", "Save");
        JButton cancel = Ui.button("wordEditor.cancel", "Cancel");
        save.setName(prefix + ".editor.save"); cancel.setName(prefix + ".editor.cancel");
        add(Ui.row(save, cancel), BorderLayout.SOUTH);
        cancel.addActionListener(event -> onCancel.run());
        lookup.addActionListener(event -> {
            String word = term.getText();
            tasks.run("Looking up definition", () -> controller.lookup(word), entry -> {
                term.setText(entry.term); definition.setText(entry.definition);
                example.setText(entry.example); source.setText(entry.source);
            });
        });
        save.addActionListener(event -> {
            String word = term.getText(), category = topic.getText(), meaning = definition.getText();
            String usage = example.getText(), attribution = source.getText();
            ViewModels.Word selected = editing;
            tasks.run("Saving personal vocabulary", () -> selected == null
                    ? controller.addPersonalWord(word, category, meaning, usage, attribution)
                    : controller.changeDefinition(selected.id, meaning), result -> {
                        if (result.success) {
                            onSaved.accept(result);
                        } else {
                            tasks.message(result.message);
                        }
                    });
        });
    }

    void addWord() {
        editing = null; title.setText("Add my word");
        term.setText(""); topic.setText(""); definition.setText(""); example.setText("");
        source.setText("Personal vocabulary");
        setCreationFields(true);
    }

    void edit(ViewModels.Word word) {
        editing = word; title.setText("Edit my definition");
        term.setText(word.term); topic.setText(word.topic); definition.setText(word.definition);
        example.setText(word.example); source.setText(word.source);
        setCreationFields(false); definition.setCaretPosition(0);
    }

    private void setCreationFields(boolean enabled) {
        term.setEditable(enabled); topic.setEditable(enabled);
        example.setEditable(enabled); source.setEditable(enabled); lookup.setEnabled(enabled);
    }
}
