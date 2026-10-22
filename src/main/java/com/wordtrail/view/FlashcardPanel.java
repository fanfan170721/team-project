package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import javax.swing.*;

final class FlashcardPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JComboBox<String> topic = new JComboBox<>(new String[]{"All topics"});
    private final JCheckBox reviewOnly = new JCheckBox("Use review list only");
    private final JLabel counter = Ui.label("Start a deck to study.", 15);
    private final JLabel term = Ui.label("Ready to learn?", 36);
    private final JTextArea definition = Ui.display("Choose a topic and start a deck.");
    private final JButton reveal = Ui.button("flash.reveal", "Reveal definition");
    private final JButton studied = Ui.button("flash.studied", "Complete review");
    private final JButton next = Ui.button("flash.next", "Next card");
    private final JButton save = Ui.button("flash.save", "Save for review later");
    private String currentId;
    private boolean saved;

    FlashcardPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks;
        JPanel page = Ui.page("Flashcards", "Reveal, then complete one review. Each new visit can count again, including today.");
        topic.setName("flash.topic"); reviewOnly.setName("flash.reviewOnly");
        JButton start = Ui.button("flash.start", "Start / restart deck");
        JPanel body = new JPanel(new BorderLayout(12, 18));
        body.add(Ui.row(Ui.label("Topic", 14), topic, reviewOnly, start), BorderLayout.NORTH);
        JPanel card = new JPanel(new BorderLayout(12, 20));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(207, 220, 224)),
                BorderFactory.createEmptyBorder(28, 30, 28, 30)));
        JPanel title = new JPanel(new GridLayout(0, 1, 0, 10));
        counter.setName("flash.counter"); term.setName("flash.term"); definition.setName("flash.definition");
        title.add(counter); title.add(term);
        card.add(title, BorderLayout.NORTH);
        definition.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        card.add(new JScrollPane(definition), BorderLayout.CENTER);
        body.add(card, BorderLayout.CENTER);
        body.add(Ui.row(reveal, studied, next, save), BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER); add(page);
        reveal.setEnabled(false); studied.setEnabled(false); next.setEnabled(false); save.setEnabled(false);
        start.addActionListener(event -> {
            String chosen = Ui.topic(topic);
            boolean onlySaved = reviewOnly.isSelected();
            tasks.run("Preparing flashcards", () -> controller.startCards(chosen, onlySaved), this::render);
        });
        reveal.addActionListener(event -> tasks.run("Revealing card", controller::revealCard, this::render));
        studied.addActionListener(event -> tasks.run("Recording study", controller::studyCard, this::render));
        next.addActionListener(event -> tasks.run("Loading next card", controller::nextCard, this::render));
        save.addActionListener(event -> {
            String id = currentId;
            boolean nextSaved = !saved;
            tasks.run("Saving review list", () -> { controller.saveWord(id, nextSaved); return nextSaved; }, value -> {
                saved = value;
                save.setText(saved ? "Remove from review later" : "Save for review later");
            });
        });
    }

    @Override public void refresh() {
        tasks.run("Loading topics", () -> controller.browse(null, ""), library -> Ui.topics(topic, library.topics));
    }

    private void render(ViewModels.Card card) {
        currentId = card.word.id; saved = card.word.saved;
        counter.setText(card.counter + "  |  " + card.word.topic + "  |  Reviews: " + card.word.reviewCount
                + (card.reviewed ? "  |  Review completed" : ""));
        term.setText(card.word.term);
        definition.setText(card.word.definition + (card.word.example.isBlank() ? "" : "\n\nExample: " + card.word.example)
                + (card.word.source.isBlank() ? "" : "\n\nSource: " + card.word.source));
        definition.setCaretPosition(0);
        reveal.setEnabled(!card.revealed); studied.setEnabled(card.revealed && !card.reviewed);
        next.setEnabled(true); save.setEnabled(true);
        save.setText(saved ? "Remove from review later" : "Save for review later");
    }
}
