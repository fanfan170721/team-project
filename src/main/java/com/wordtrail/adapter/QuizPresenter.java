package com.wordtrail.adapter;

import com.wordtrail.usecase.QuizUseCase;

/** Formats submitted-answer feedback and the durable completion result. */
public final class QuizPresenter {
    public ViewModels.Quiz quiz(QuizUseCase.Question question) {
        String feedback = "";
        if (question.answered) {
            feedback = question.selectedIndex == question.correctIndex ? "Correct!" :
                    "Correct answer: " + question.options.get(question.correctIndex);
        }
        if (question.finished) {
            feedback += "\nQuiz complete: " + question.score + " / " + question.total + " ("
                    + Math.round(100.0 * question.score / question.total) + "%). Saved to your progress.";
        }
        return new ViewModels.Quiz(question.wordId, question.prompt, question.options,
                "Question " + question.position + " of " + question.total, feedback, question.answered, question.finished);
    }
}
