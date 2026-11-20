package com.wordtrail.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface QuizUseCase {
    Question start(String topic, int maximumQuestions);
    Question answer(int optionIndex);
    Question next();

    /** No correct answer is exposed until the learner submits an answer. */
    final class Question {
        public final String wordId;
        public final String prompt;
        public final List<String> options;
        public final int position;
        public final int total;
        public final boolean answered;
        public final boolean finished;
        public final int selectedIndex;
        public final int correctIndex;
        public final int score;

        public Question(String wordId, String prompt, List<String> options, int position, int total,
                        boolean answered, boolean finished, int selectedIndex, int correctIndex, int score) {
            this.wordId = wordId;
            this.prompt = prompt;
            this.options = Collections.unmodifiableList(new ArrayList<>(options));
            this.position = position;
            this.total = total;
            this.answered = answered;
            this.finished = finished;
            this.selectedIndex = selectedIndex;
            this.correctIndex = correctIndex;
            this.score = score;
        }
    }
}
