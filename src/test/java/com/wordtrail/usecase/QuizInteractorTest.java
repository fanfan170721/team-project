package com.wordtrail.usecase;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuizInteractorTest {
    @Test void answersAreHiddenAndOnlyCompletedQuizzesAreSavedOnce() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        QuizUseCase.Question question = f.quiz.start("Travel", 20);
        assertEquals(3, question.total);
        assertEquals(-1, question.correctIndex);
        assertThrows(AppException.class, f.quiz::next);
        assertThrows(AppException.class, () -> f.quiz.answer(-1));
        for (int i = 0; i < 3; i++) {
            String correct = "correct" + question.wordId.substring(1);
            question = f.quiz.answer(question.options.indexOf(correct));
            assertTrue(question.answered);
            assertEquals(i + 1, question.score);
            assertThrows(AppException.class, () -> f.quiz.answer(0));
            if (i < 2) {
                assertTrue(f.store.getProgress("admin").getAttempts().isEmpty());
                question = f.quiz.next();
            }
        }
        assertTrue(question.finished);
        assertEquals(1, f.store.getProgress("admin").getAttempts().size());
        assertEquals(3, f.store.getProgress("admin").getAttempts().get(0).getCorrect());
        assertThrows(AppException.class, f.quiz::next);
    }

    @Test void failedFinalSaveCanBeRetriedWithoutDoubleCounting() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        QuizUseCase.Question question = f.quiz.start("Nature", 5);
        int answer = question.options.indexOf("correct3");
        f.store.failSave = true;
        assertThrows(AppException.class, () -> f.quiz.answer(answer));
        f.store.failSave = false;
        assertEquals(1, f.quiz.answer(answer).score);
        assertEquals(1, f.store.getProgress("admin").getAttempts().size());
    }

    @Test void abandonedSessionsNeverTransferToAnotherAccount() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        f.quiz.start(null, 5);
        f.learner("alice");
        assertThrows(AppException.class, () -> f.quiz.answer(0));
        assertTrue(f.store.getProgress("alice").getAttempts().isEmpty());
        assertThrows(AppException.class, () -> f.quiz.start("Missing", 5));
        assertThrows(AppException.class, () -> f.quiz.start(null, 0));
        QuizUseCase.Question question = f.quiz.start("Nature", 1);
        int wrong = (question.options.indexOf("correct3") + 1) % 4;
        assertEquals(0, f.quiz.answer(wrong).score);
    }

    @Test void correctChoiceMovesBetweenPositionsWithoutChangingScoring() {
        UseCaseFixture f = new UseCaseFixture(); f.admin();
        java.util.Set<Integer> positions = new java.util.HashSet<>();
        for (int i = 0; i < 16; i++) {
            QuizUseCase.Question question = f.quiz.start("Nature", 1);
            int correct = question.options.indexOf("correct3");
            positions.add(correct);
            assertEquals(4, new java.util.HashSet<>(question.options).size());
            assertEquals(1, f.quiz.answer(correct).score);
        }
        assertTrue(positions.size() > 1, "Correct answers must not always occupy the same position.");
    }
}
