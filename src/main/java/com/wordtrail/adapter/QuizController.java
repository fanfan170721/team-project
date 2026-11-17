package com.wordtrail.adapter;

import com.wordtrail.usecase.QuizUseCase;

/** Routes quiz transitions and converts their returned state into learner feedback. */
public final class QuizController {
    private final QuizUseCase quiz;
    private final QuizPresenter presenter;

    public QuizController(QuizUseCase quiz, QuizPresenter presenter) {
        this.quiz = quiz;
        this.presenter = presenter;
    }

    public ViewModels.Quiz start(String topic, int count) {
        return ControllerCalls.call(() -> presenter.quiz(quiz.start(topic, count)));
    }

    public ViewModels.Quiz answer(int index) {
        return ControllerCalls.call(() -> presenter.quiz(quiz.answer(index)));
    }

    public ViewModels.Quiz next() {
        return ControllerCalls.call(() -> presenter.quiz(quiz.next()));
    }
}
