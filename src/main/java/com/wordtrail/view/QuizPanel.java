package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import javax.swing.*;

final class QuizPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JComboBox<Ui.TopicOption> topic = Ui.topicSelector();
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private final JLabel counter = Ui.label("Start a quiz when you are ready.", 16);
    private final JTextArea prompt = Ui.display("Choose a topic to test your vocabulary.");
    private final JRadioButton[] choices = new JRadioButton[4];
    private final JTextArea[] choiceTexts = new JTextArea[4];
    private final ButtonGroup group = new ButtonGroup();
    private final JTextArea feedback = Ui.display("");
    private final JButton submit = Ui.button("quiz.submit", "Submit answer");
    private final JButton next = Ui.button("quiz.next", "Next question");
    private final JButton save = Ui.button("quiz.save", "Save this word for review");
    private String currentWordId;

    QuizPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks;
        JPanel page = Ui.page("Quiz", "Answer each question once. Completed results are saved automatically.");
        topic.setName("quiz.topic"); count.setName("quiz.count");
        JButton start = Ui.button("quiz.start", "Start / restart quiz");
        JPanel body = new JPanel(new BorderLayout(12, 10));
        body.add(Ui.row(Ui.label("Topic", 14), topic, Ui.label("Questions (up to)", 14), count, start), BorderLayout.NORTH);
        JPanel question = new JPanel(new BorderLayout(10, 14));
        JPanel heading = new JPanel(new BorderLayout());
        counter.setName("quiz.counter"); prompt.setName("quiz.prompt");
        prompt.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22)); prompt.setRows(2);
        heading.add(counter, BorderLayout.NORTH); heading.add(prompt, BorderLayout.CENTER);
        question.add(heading, BorderLayout.NORTH);
        JPanel options = new JPanel(new GridLayout(4, 1, 0, 8));
        for (int i = 0; i < choices.length; i++) {
            choices[i] = new JRadioButton("Choice " + (i + 1));
            choices[i].setName("quiz.choice" + i); choices[i].setEnabled(false);
            choices[i].putClientProperty("html.disable", true);
            group.add(choices[i]);
            // A wrapping text area handles long admin/API definitions without truncation.
            JTextArea choiceText = Ui.display("");
            choiceTexts[i] = choiceText;
            choiceText.setName("quiz.choiceText" + i);
            choiceText.setRows(2);
            final int choiceIndex = i;
            choiceText.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (choices[choiceIndex].isEnabled()) { choices[choiceIndex].setSelected(true); }
                }
            });
            JPanel option = new JPanel(new BorderLayout(8, 0));
            option.add(choices[i], BorderLayout.WEST); option.add(new JScrollPane(choiceText), BorderLayout.CENTER);
            options.add(option);
        }
        question.add(options, BorderLayout.CENTER);
        feedback.setName("quiz.feedback"); feedback.setRows(3);
        question.add(feedback, BorderLayout.SOUTH);
        body.add(question, BorderLayout.CENTER);
        body.add(Ui.row(submit, next, save), BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER); add(page);
        submit.setEnabled(false); next.setEnabled(false); save.setEnabled(false);
        start.addActionListener(event -> {
            String chosen = Ui.topic(topic); int maximum = (Integer) count.getValue();
            tasks.run("Preparing quiz", () -> controller.startQuiz(chosen, maximum), this::render);
        });
        submit.addActionListener(event -> {
            int answer = -1;
            for (int i = 0; i < choices.length; i++) { if (choices[i].isSelected()) { answer = i; } }
            final int selected = answer;
            tasks.run("Checking answer", () -> controller.answer(selected), this::render);
        });
        next.addActionListener(event -> tasks.run("Loading next question", controller::nextQuestion, this::render));
        save.addActionListener(event -> {
            String id = currentWordId;
            tasks.run("Saving review word", () -> { controller.saveWord(id, true); return true; },
                    ignored -> tasks.message("Word saved to your review list."));
        });
    }

    @Override public void refresh() {
        tasks.run("Loading topics", () -> controller.browse(null, ""), library -> Ui.topics(topic, library.topics));
    }

    private void render(ViewModels.Quiz quiz) {
        currentWordId = quiz.wordId;
        counter.setText(quiz.counter); prompt.setText(quiz.prompt); feedback.setText(quiz.feedback);
        if (!quiz.answered) { group.clearSelection(); }
        for (int i = 0; i < choices.length; i++) {
            choices[i].setEnabled(!quiz.answered);
            choiceTexts[i].setText(quiz.options.get(i));
            choiceTexts[i].setCaretPosition(0);
        }
        submit.setEnabled(!quiz.answered); next.setEnabled(quiz.answered && !quiz.finished); save.setEnabled(true);
    }
}
