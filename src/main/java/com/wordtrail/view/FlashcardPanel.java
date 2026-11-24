package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

final class FlashcardPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JComboBox<Ui.TopicOption> topic = Ui.topicSelector();
    private final JCheckBox reviewOnly = new JCheckBox("Use review list only");
    private final JLabel stage = Ui.muted("READY WHEN YOU ARE", 11);
    private final JLabel counter = Ui.muted("Start a deck to study.", 13);
    private final JLabel term = Ui.heading("Ready to learn?", 42);
    private final JTextArea definition = Ui.display("Choose a topic and start a deck.");
    private final JButton reveal = Ui.primaryButton("flash.reveal", "Reveal definition");
    private final JButton studied = Ui.primaryButton("flash.studied", "Complete review");
    private final JButton next = Ui.button("flash.next", "Next card");
    private final JButton save = Ui.button("flash.save", "Save for review later");
    private String currentId;
    private boolean saved;

    FlashcardPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller;
        this.tasks = tasks;
        JPanel page = Ui.page("Flashcards", "Recall the meaning, reveal it, then complete your review.");
        topic.setName("flash.topic");
        reviewOnly.setName("flash.reviewOnly");
        reviewOnly.setOpaque(false);
        reviewOnly.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        reviewOnly.setForeground(Ui.INK);
        JButton start = Ui.button("flash.start", "Start / restart deck");
        JPanel filters = Ui.surface(new BorderLayout(12, 0), 10);
        JPanel selections = transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel topicLabel = Ui.muted("Topic", 13);
        topicLabel.setLabelFor(topic);
        selections.add(topicLabel);
        selections.add(topic);
        selections.add(reviewOnly);
        filters.add(selections, BorderLayout.CENTER);
        filters.add(start, BorderLayout.EAST);

        JPanel body = transparent(new BorderLayout(0, 12));
        body.add(filters, BorderLayout.NORTH);
        JPanel card = Ui.surface(new BorderLayout(0, 16), 24);
        JPanel title = transparent(new BorderLayout(0, 6));
        stage.setName("flash.stage");
        counter.setName("flash.counter");
        term.setName("flash.term");
        definition.setName("flash.definition");
        title.add(stage, BorderLayout.NORTH);
        title.add(term, BorderLayout.CENTER);
        title.add(counter, BorderLayout.SOUTH);
        card.add(title, BorderLayout.NORTH);
        definition.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19));
        card.add(Ui.scroll(definition), BorderLayout.CENTER);
        body.add(card, BorderLayout.CENTER);

        JPanel actions = transparent(new BorderLayout(12, 0));
        JPanel learningActions = transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        learningActions.add(reveal);
        learningActions.add(studied);
        learningActions.add(next);
        actions.add(learningActions, BorderLayout.CENTER);
        actions.add(save, BorderLayout.EAST);
        body.add(actions, BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER);
        add(page);
        reveal.setEnabled(false);
        studied.setEnabled(false);
        next.setEnabled(false);
        save.setEnabled(false);

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
            tasks.run("Saving review list", () -> {
                controller.saveWord(id, nextSaved);
                return nextSaved;
            }, value -> {
                saved = value;
                save.setText(saved ? "Remove from review later" : "Save for review later");
            });
        });
    }

    private static JPanel transparent(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    @Override
    public void refresh() {
        tasks.run("Loading topics", () -> controller.browse(null, ""), library -> Ui.topics(topic, library.topics));
    }

    private void render(ViewModels.Card card) {
        currentId = card.word.id;
        saved = card.word.saved;
        stage.setText(card.reviewed ? "REVIEW COMPLETE"
                : card.revealed ? "MEANING REVEALED" : "RECALL THE MEANING");
        counter.setText(card.counter + "  |  " + card.word.topic + "  |  Reviews: " + card.word.reviewCount
                + (card.reviewed ? "  |  Review completed" : ""));
        term.setText(card.word.term);
        term.setToolTipText("Word: " + card.word.term);
        definition.setText(card.word.definition + (card.word.example.isBlank() ? "" : "\n\nExample: " + card.word.example)
                + (card.word.source.isBlank() ? "" : "\n\nSource: " + card.word.source)
                + "\n\nWord: " + card.word.term + "\nTopic: " + card.word.topic
                + "\nCompleted reviews: " + card.word.reviewCount);
        definition.setCaretPosition(0);
        reveal.setEnabled(!card.revealed);
        studied.setEnabled(card.revealed && !card.reviewed);
        next.setEnabled(true);
        save.setEnabled(true);
        save.setText(saved ? "Remove from review later" : "Save for review later");
    }
}
