package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

final class QuizPanel extends JPanel implements Refreshable {
    private final AppController controller;
    private final UiTasks tasks;
    private final JComboBox<Ui.TopicOption> topic = Ui.topicSelector();
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private final JLabel counter = Ui.muted("Start a quiz when you are ready.", 13);
    private final JTextArea prompt = Ui.display("Choose a topic to test your vocabulary.");
    private final JRadioButton[] choices = new JRadioButton[4];
    private final JTextArea[] choiceTexts = new JTextArea[4];
    private final ButtonGroup group = new ButtonGroup();
    private final JTextArea feedback = Ui.display("");
    private final JPanel feedbackCard = Ui.surface(new BorderLayout(), 8);
    private final JButton submit = Ui.primaryButton("quiz.submit", "Submit answer");
    private final JButton next = Ui.primaryButton("quiz.next", "Next question");
    private final JButton save = Ui.button("quiz.save", "Save this word for review");
    private String currentWordId;

    QuizPanel(AppController controller, UiTasks tasks) {
        super(new BorderLayout());
        this.controller = controller;
        this.tasks = tasks;
        JPanel page = Ui.page("Quiz", "Choose the meaning. Build confidence, one word at a time.");
        topic.setName("quiz.topic");
        count.setName("quiz.count");
        JButton start = Ui.button("quiz.start", "Start / restart quiz");
        JPanel filters = Ui.surface(new BorderLayout(12, 0), 10);
        JPanel selections = transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel topicLabel = Ui.muted("Topic", 13);
        topicLabel.setLabelFor(topic);
        JLabel countLabel = Ui.muted("Questions (up to)", 13);
        countLabel.setLabelFor(count);
        selections.add(topicLabel);
        selections.add(topic);
        selections.add(countLabel);
        selections.add(count);
        filters.add(selections, BorderLayout.CENTER);
        filters.add(start, BorderLayout.EAST);
        JPanel body = transparent(new BorderLayout(0, 10));
        body.add(filters, BorderLayout.NORTH);

        JPanel question = transparent(new BorderLayout(0, 8));
        JPanel heading = Ui.surface(new BorderLayout(0, 6), 12);
        counter.setName("quiz.counter");
        prompt.setName("quiz.prompt");
        prompt.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        prompt.setRows(2);
        heading.add(counter, BorderLayout.NORTH);
        heading.add(Ui.scroll(prompt), BorderLayout.CENTER);
        question.add(heading, BorderLayout.NORTH);
        JPanel options = transparent(new GridLayout(2, 2, 8, 8));
        for (int i = 0; i < choices.length; i++) {
            choices[i] = new JRadioButton(String.valueOf((char) ('A' + i)));
            choices[i].setName("quiz.choice" + i);
            choices[i].setEnabled(false);
            choices[i].setOpaque(false);
            choices[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            choices[i].setForeground(Ui.INK);
            choices[i].putClientProperty("html.disable", true);
            choices[i].getAccessibleContext().setAccessibleName("Choice " + (i + 1));
            group.add(choices[i]);
            // Text stays selectable, wraps and scrolls without interpreting API content as HTML.
            JTextArea choiceText = Ui.display("");
            choiceTexts[i] = choiceText;
            choiceText.setName("quiz.choiceText" + i);
            choiceText.setRows(1);
            JPanel option = new AnswerCard(choices[i]);
            JScrollPane choiceScroll = Ui.scroll(choiceText);
            choiceScroll.getViewport().setOpaque(false);
            choiceScroll.setOpaque(false);
            option.add(choices[i], BorderLayout.WEST);
            option.add(choiceScroll, BorderLayout.CENTER);
            final int choiceIndex = i;
            MouseAdapter selection = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (choices[choiceIndex].isEnabled()) {
                        choices[choiceIndex].setSelected(true);
                        choices[choiceIndex].requestFocusInWindow();
                    }
                }
            };
            choiceText.addMouseListener(selection);
            option.addMouseListener(selection);
            choices[i].addItemListener(event -> option.repaint());
            options.add(option);
        }
        question.add(options, BorderLayout.CENTER);
        feedback.setName("quiz.feedback");
        feedback.setRows(2);
        feedback.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        feedbackCard.add(Ui.scroll(feedback), BorderLayout.CENTER);
        feedbackCard.setVisible(false);
        question.add(feedbackCard, BorderLayout.SOUTH);
        body.add(question, BorderLayout.CENTER);

        JPanel actions = transparent(new BorderLayout(12, 0));
        JPanel answerActions = transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        answerActions.add(submit);
        answerActions.add(next);
        actions.add(answerActions, BorderLayout.CENTER);
        actions.add(save, BorderLayout.EAST);
        body.add(actions, BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER);
        add(page);
        submit.setEnabled(false);
        next.setEnabled(false);
        save.setEnabled(false);

        start.addActionListener(event -> {
            String chosen = Ui.topic(topic);
            int maximum = (Integer) count.getValue();
            tasks.run("Preparing quiz", () -> controller.startQuiz(chosen, maximum), this::render);
        });
        submit.addActionListener(event -> {
            int answer = -1;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].isSelected()) {
                    answer = i;
                }
            }
            final int selected = answer;
            tasks.run("Checking answer", () -> controller.answer(selected), this::render);
        });
        next.addActionListener(event -> tasks.run("Loading next question", controller::nextQuestion, this::render));
        save.addActionListener(event -> {
            String id = currentWordId;
            tasks.run("Saving review word", () -> {
                controller.saveWord(id, true);
                return true;
            }, ignored -> tasks.message("Word saved to your review list."));
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

    private void render(ViewModels.Quiz quiz) {
        currentWordId = quiz.wordId;
        counter.setText(quiz.counter);
        prompt.setText(quiz.prompt);
        prompt.setCaretPosition(0);
        feedback.setText(quiz.feedback);
        feedback.setCaretPosition(0);
        feedbackCard.setVisible(!quiz.feedback.isBlank());
        if (!quiz.answered) {
            group.clearSelection();
        }
        for (int i = 0; i < choices.length; i++) {
            choices[i].setEnabled(!quiz.answered);
            choices[i].getAccessibleContext().setAccessibleDescription(quiz.options.get(i));
            choiceTexts[i].setText(quiz.options.get(i));
            choiceTexts[i].setCaretPosition(0);
        }
        submit.setEnabled(!quiz.answered);
        next.setEnabled(quiz.answered && !quiz.finished);
        save.setEnabled(true);
        revalidate();
        repaint();
    }

    /** Selection styling follows the existing radio model without replacing its keyboard behavior. */
    private static final class AnswerCard extends JPanel {
        private final JRadioButton choice;

        private AnswerCard(JRadioButton choice) {
            super(new BorderLayout(8, 0));
            this.choice = choice;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.setColor(choice.isSelected() ? Ui.TINT : Ui.SURFACE);
            canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            canvas.setColor(choice.isSelected() ? Ui.ACCENT : Ui.BORDER);
            canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            canvas.dispose();
        }
    }
}
