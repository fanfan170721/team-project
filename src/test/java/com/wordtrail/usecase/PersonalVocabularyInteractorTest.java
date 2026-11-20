package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonalVocabularyInteractorTest {
    @Test void personalWordsAndDefinitionsArePrivateAndIntegratedIntoLearning() {
        UseCaseFixture f = new UseCaseFixture();
        assertFailure(f, () -> f.personal.addWord(new PersonalWordInput("word", "topic", "meaning", "", "")),
                "Please log in first");
        f.admin(); f.learner("alice");
        VocabularyWord personal = add(f, " resilient ", "Mindset", "Recovers quickly.", "Try again.", "");
        assertEquals("resilient", personal.getTerm());
        assertEquals(5, f.catalog.browse(null, "").size());
        assertTrue(f.catalog.topics().contains("Mindset"));
        change(f, "w0", "My own explanation.");
        assertEquals("My own explanation.", f.catalog.browse(null, "word0").get(0).getDefinition());
        f.review.setSaved(personal.getId(), true);
        assertEquals(personal.getId(), f.flash.start("Mindset", true).word.getId());
        assertEquals("Recovers quickly.", f.flash.reveal().word.getDefinition());
        assertEquals(5, f.progress.report().totalWords);
        f.loginAdmin();
        assertEquals(4, f.catalog.browse(null, "").size());
        assertEquals("definition0", f.catalog.browse(null, "word0").get(0).getDefinition());
        assertFalse(f.catalog.topics().contains("Mindset"));
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput(personal.getId(), "Not mine.")),
                "Please select a word from your vocabulary");
        assertThrows(AppException.class, () -> f.review.setSaved(personal.getId(), true));
    }

    @Test void invalidOrDuplicateWordsAndFailedWritesDoNotPartiallySave() {
        UseCaseFixture f = new UseCaseFixture(); f.admin(); f.learner("alice");
        assertFailure(f, () -> f.personal.addWord(new PersonalWordInput("WORD0", "travel", "duplicate", "", "")),
                "already exists");
        assertFailure(f, () -> f.personal.addWord(new PersonalWordInput(" ", "Travel", "meaning", "", "")),
                "Word is required");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("w0", " ")),
                "Definition is required");
        f.store.failSave = true;
        assertFailure(f, () -> f.personal.addWord(new PersonalWordInput("resilient", "Mindset", "A meaning.", "", "")),
                "Simulated disk failure");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("w0", "Changed")),
                "Simulated disk failure");
        assertTrue(f.store.getProgress("alice").getPersonalWords().isEmpty());
        assertTrue(f.store.getProgress("alice").getDefinitions().isEmpty());
    }

    @Test void quizUsesPersonalDefinitionsAndGeneratesFourUniqueShuffledChoices() {
        UseCaseFixture f = new UseCaseFixture(); f.admin(); f.learner("alice");
        VocabularyWord word = add(f, "resilient", "Mindset", "Recovers quickly.", "", "");
        change(f, word.getId(), "My revised meaning.");
        QuizUseCase.Question question = f.quiz.start("Mindset", 1);
        assertEquals(word.getId(), question.wordId);
        assertEquals(4, new java.util.HashSet<>(question.options).size());
        assertEquals(-1, question.correctIndex);
        assertTrue(question.options.contains("My revised meaning."));
        assertFalse(question.options.contains("Recovers quickly."));
        assertEquals(1, f.quiz.answer(question.options.indexOf("My revised meaning.")).score);
        change(f, "w0", "Changed shared meaning for me.");
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

    static VocabularyWord add(UseCaseFixture f, String term, String topic, String definition,
                              String example, String source) {
        f.personalOutput.clear();
        f.personal.addWord(new PersonalWordInput(term, topic, definition, example, source));
        assertTrue(f.personalOutput.failures.isEmpty());
        assertEquals(1, f.personalOutput.successes.size());
        PersonalVocabularyResponse response = f.personalOutput.successes.get(0);
        assertEquals(PersonalVocabularyResponse.Action.ADDED, response.action);
        VocabularyWord word = ContentQueries.requireVisibleWord(f.store, f.personal.details(), response.wordId);
        assertEquals(word.getTerm(), response.term);
        assertEquals(word.getDefinition(), response.definition);
        return word;
    }

    static void change(UseCaseFixture f, String wordId, String definition) {
        f.personalOutput.clear();
        f.personal.changeDefinition(new PersonalDefinitionInput(wordId, definition));
        assertTrue(f.personalOutput.failures.isEmpty());
        assertEquals(1, f.personalOutput.successes.size());
        PersonalVocabularyResponse response = f.personalOutput.successes.get(0);
        assertEquals(PersonalVocabularyResponse.Action.DEFINITION_CHANGED, response.action);
        assertEquals(wordId, response.wordId);
        assertEquals(definition.strip(), response.definition);
    }

    static void assertFailure(UseCaseFixture f, Runnable command, String message) {
        f.personalOutput.clear();
        command.run();
        assertTrue(f.personalOutput.successes.isEmpty());
        assertEquals(1, f.personalOutput.failures.size());
        assertTrue(f.personalOutput.failures.get(0).contains(message), f.personalOutput.failures.get(0));
    }
}
