package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonalVocabularyInteractorTest {
    @Test void personalWordsAndDefinitionsArePrivateAndIntegratedIntoLearning() {
        UseCaseFixture f = new UseCaseFixture();
        assertThrows(AppException.class, () -> f.personal.addWord("word", "topic", "meaning", "", ""));
        f.admin(); f.learner("alice");
        VocabularyWord personal = f.personal.addWord(" resilient ", "Mindset", "Recovers quickly.", "Try again.", "");
        assertEquals("resilient", personal.getTerm());
        assertEquals(5, f.catalog.browse(null, "").size());
        assertTrue(f.catalog.topics().contains("Mindset"));
        f.personal.changeDefinition("w0", "My own explanation.");
        assertEquals("My own explanation.", f.catalog.browse(null, "word0").get(0).getDefinition());
        f.review.setSaved(personal.getId(), true);
        assertEquals(personal.getId(), f.flash.start("Mindset", true).word.getId());
        assertEquals("Recovers quickly.", f.flash.reveal().word.getDefinition());
        assertEquals(5, f.progress.report().totalWords);
        f.loginAdmin();
        assertEquals(4, f.catalog.browse(null, "").size());
        assertEquals("definition0", f.catalog.browse(null, "word0").get(0).getDefinition());
        assertFalse(f.catalog.topics().contains("Mindset"));
        assertThrows(AppException.class, () -> f.personal.changeDefinition(personal.getId(), "Not mine."));
        assertThrows(AppException.class, () -> f.review.setSaved(personal.getId(), true));
    }

    @Test void invalidOrDuplicateWordsAndFailedWritesDoNotPartiallySave() {
        UseCaseFixture f = new UseCaseFixture(); f.admin(); f.learner("alice");
        assertThrows(AppException.class, () -> f.personal.addWord("WORD0", "travel", "duplicate", "", ""));
        assertThrows(AppException.class, () -> f.personal.addWord(" ", "Travel", "meaning", "", ""));
        assertThrows(AppException.class, () -> f.personal.changeDefinition("w0", " "));
        f.store.failSave = true;
        assertThrows(AppException.class, () -> f.personal.addWord("resilient", "Mindset", "A meaning.", "", ""));
        assertThrows(AppException.class, () -> f.personal.changeDefinition("w0", "Changed"));
        assertTrue(f.store.getProgress("alice").getPersonalWords().isEmpty());
        assertTrue(f.store.getProgress("alice").getDefinitions().isEmpty());
    }

    @Test void quizUsesPersonalDefinitionsAndGeneratesFourUniqueShuffledChoices() {
        UseCaseFixture f = new UseCaseFixture(); f.admin(); f.learner("alice");
        VocabularyWord word = f.personal.addWord("resilient", "Mindset", "Recovers quickly.", "", "");
        f.personal.changeDefinition(word.getId(), "My revised meaning.");
        QuizUseCase.Question question = f.quiz.start("Mindset", 1);
        assertEquals(word.getId(), question.wordId);
        assertEquals(4, new java.util.HashSet<>(question.options).size());
        assertEquals(-1, question.correctIndex);
        assertTrue(question.options.contains("My revised meaning."));
        assertFalse(question.options.contains("Recovers quickly."));
        assertEquals(1, f.quiz.answer(question.options.indexOf("My revised meaning.")).score);
        f.personal.changeDefinition("w0", "Changed shared meaning for me.");
        question = f.quiz.start("Travel", 20);
        boolean found = false;
        for (int i = 0; i < 3; i++) {
            String answer = "correct" + question.wordId.substring(1);
            if (question.wordId.equals("w0")) {
                found = true;
                answer = "Changed shared meaning for me.";
                assertFalse(question.options.contains("correct0"));
            }
            question = f.quiz.answer(question.options.indexOf(answer));
            if (i < 2) { question = f.quiz.next(); }
        }
        assertTrue(found);
        assertEquals(3, question.score);
        assertEquals(2, f.store.getProgress("alice").getAttempts().size());
    }
}
