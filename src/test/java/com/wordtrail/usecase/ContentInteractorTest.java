package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentInteractorTest {
    @Test void authorizationIsEnforcedInTheUseCase() {
        UseCaseFixture f = new UseCaseFixture();
        assertThrows(AppException.class, () -> f.content.addWord("word", "Topic", "meaning", "", ""));
        f.admin();
        f.learner("alice");
        assertThrows(AppException.class, () -> f.content.addWord("word", "Topic", "meaning", "", ""));
        assertThrows(AppException.class, () -> f.content.addQuestion("w0", "prompt", Arrays.asList("a", "b", "c", "d"), 0));
    }

    @Test void adminCanAddContentAndInvalidFormsDoNotMutateTheCatalog() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        VocabularyWord word = f.content.addWord(" suitcase ", "travel", "A case for luggage.", "", "");
        assertEquals("Travel", word.getTopic());
        assertEquals("suitcase", word.getTerm());
        assertThrows(AppException.class, () -> f.content.addWord("SUITCASE", "Travel", "definition", "", ""));
        assertThrows(AppException.class, () -> f.content.addWord(" ", "Topic", "definition", "", ""));
        assertThrows(AppException.class, () -> f.content.addQuestion(word.getId(), "prompt", Arrays.asList("a", " A ", "c", "d"), 0));
        assertThrows(AppException.class, () -> f.content.addQuestion("missing", "prompt", Arrays.asList("a", "b", "c", "d"), 0));
        f.content.addQuestion(word.getId(), "What holds luggage?", Arrays.asList("Suitcase", "Ticket", "Map", "Airport"), 0);
        assertEquals(5, f.store.words.size());
        assertEquals(5, f.store.questions.size());
    }
}
