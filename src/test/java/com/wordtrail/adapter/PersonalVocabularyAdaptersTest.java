package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.usecase.PersonalDefinitionInput;
import com.wordtrail.usecase.PersonalVocabularyResponse;
import com.wordtrail.usecase.PersonalVocabularyUseCase;
import com.wordtrail.usecase.PersonalWordInput;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonalVocabularyAdaptersTest {
    @Test
    void controllerPassesInputAndPresenterOwnsSuccessMessages() {
        Fixture fixture = new Fixture();
        PersonalVocabularyViewModel.SaveResult added = fixture.controller.addWord(
                "word", "Topic", "Meaning", "Example", "Source");
        assertEquals("word", fixture.input.added.term);
        assertEquals("Topic", fixture.input.added.topic);
        assertEquals("Meaning", fixture.input.added.definition);
        assertEquals("Example", fixture.input.added.example);
        assertEquals("Source", fixture.input.added.source);
        assertTrue(added.success);
        assertEquals("id", added.wordId);
        assertEquals("word", added.term);
        assertEquals("Meaning", added.definition);
        assertEquals("Added \"word\" to your vocabulary.", added.message);

        PersonalVocabularyViewModel.SaveResult changed = fixture.controller.changeDefinition("id", "New meaning");
        assertEquals("id", fixture.input.changed.wordId);
        assertEquals("New meaning", fixture.input.changed.definition);
        assertTrue(changed.success);
        assertEquals("New meaning", changed.definition);
        assertEquals("Updated your definition of \"word\".", changed.message);
        assertEquals("Meaning", added.definition, "An earlier immutable result must not change.");
    }

    @Test
    void failedRequestReplacesSuccessWithoutLeakingThePreviousWord() {
        Fixture fixture = new Fixture();
        assertTrue(fixture.controller.addWord("word", "Topic", "Meaning", "", "").success);
        fixture.input.failure = "The saved file could not be replaced.";
        PersonalVocabularyViewModel.SaveResult failed = fixture.controller.changeDefinition("id", "New meaning");
        assertFalse(failed.success);
        assertEquals(fixture.input.failure, failed.message);
        assertEquals("", failed.wordId);
        assertEquals("", failed.term);
        assertEquals("", failed.definition);
    }

    @Test
    void missingOutputCannotReuseAnEarlierSuccessfulResult() {
        Fixture fixture = new Fixture();
        assertTrue(fixture.controller.addWord("word", "Topic", "Meaning", "", "").success);
        fixture.input.omitOutput = true;
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> fixture.controller.addWord("other", "Topic", "Meaning", "", ""));
        assertTrue(failure.getMessage().contains("did not produce a result"));
    }

    private static final class Fixture {
        private final PersonalVocabularyViewModel model = new PersonalVocabularyViewModel();
        private final PersonalVocabularyPresenter presenter = new PersonalVocabularyPresenter(ZoneId.of("UTC"), model);
        private final StubUseCase input = new StubUseCase(presenter);
        private final PersonalVocabularyController controller = new PersonalVocabularyController(input, presenter, model);
    }

    private static final class StubUseCase implements PersonalVocabularyUseCase {
        private final PersonalVocabularyPresenter output;
        private PersonalWordInput added;
        private PersonalDefinitionInput changed;
        private String failure;
        private boolean omitOutput;

        private StubUseCase(PersonalVocabularyPresenter output) {
            this.output = output;
        }

        @Override
        public void addWord(PersonalWordInput input) {
            added = input;
            if (!omitOutput) {
                output.presentSuccess(new PersonalVocabularyResponse(
                        PersonalVocabularyResponse.Action.ADDED, "id", input.term, input.definition));
            }
        }

        @Override
        public void changeDefinition(PersonalDefinitionInput input) {
            changed = input;
            if (failure != null) {
                output.presentFailure(failure);
            } else {
                output.presentSuccess(new PersonalVocabularyResponse(
                        PersonalVocabularyResponse.Action.DEFINITION_CHANGED, input.wordId, "word", input.definition));
            }
        }

        @Override
        public LearningProgress details() {
            return LearningProgress.empty();
        }
    }
}
