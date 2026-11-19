package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ProgressRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.wordtrail.usecase.PersonalVocabularyInteractorTest.add;
import static com.wordtrail.usecase.PersonalVocabularyInteractorTest.assertFailure;
import static org.junit.jupiter.api.Assertions.*;

class PersonalVocabularyBoundaryTest {
    @Test void optionalFieldsDefaultOrNormalizeAndTopicSpellingRemainsConsistent() {
        UseCaseFixture f = learner();
        VocabularyWord empty = add(f, " first ", " travel ", " meaning ", null, null);
        assertEquals("Travel", empty.getTopic());
        assertEquals("meaning", empty.getDefinition());
        assertEquals("", empty.getExample());
        assertEquals("Personal vocabulary", empty.getSource());
        VocabularyWord blank = add(f, "second", "Travel", "meaning", " \t ", " \t ");
        assertEquals("", blank.getExample());
        assertEquals("Personal vocabulary", blank.getSource());
        VocabularyWord attributed = add(f, "third", "Travel", "meaning", " example ", " my notes ");
        assertEquals("example", attributed.getExample());
        assertEquals("my notes", attributed.getSource());
    }

    @Test void duplicateMatchingIsCaseInsensitiveWithinATopicButAllowsAnotherTopic() {
        UseCaseFixture f = learner();
        VocabularyWord otherTopic = add(f, "WORD0", "Science", "A second context.", "", "");
        assertEquals("Science", otherTopic.getTopic());
        LearningProgress before = f.personal.details();
        assertFailure(f, () -> f.personal.addWord(new PersonalWordInput("word0", "sCiEnCe", "duplicate", "", "")),
                "already exists");
        assertSame(before, f.personal.details());
        assertEquals(1, f.personal.details().getPersonalWords().size());
        assertEquals(4, f.store.getWords().size());
    }

    @Test void oversizedAndMissingFormValuesPreserveTheExistingSnapshot() {
        UseCaseFixture f = learner();
        add(f, "existing", "Personal", "Keep this meaning.", "", "");
        LearningProgress before = f.personal.details();
        PersonalWordInput[] invalid = {
            new PersonalWordInput(null, "Topic", "meaning", "", ""),
            new PersonalWordInput("new", null, "meaning", "", ""),
            new PersonalWordInput("new", "Topic", null, "", ""),
            new PersonalWordInput("w".repeat(81), "Topic", "meaning", "", ""),
            new PersonalWordInput("new", "t".repeat(51), "meaning", "", ""),
            new PersonalWordInput("new", "Topic", "d".repeat(2001), "", ""),
            new PersonalWordInput("new", "Topic", "meaning", "e".repeat(2001), ""),
            new PersonalWordInput("new", "Topic", "meaning", "", "s".repeat(501))
        };
        String[] messages = {"Word is required", "Topic is required", "Definition is required",
                "Word must be at most 80", "Topic must be at most 50", "Definition must be at most 2000",
                "Example must be at most 2000", "Source must be at most 500"};
        for (int i = 0; i < invalid.length; i++) {
            PersonalWordInput input = invalid[i];
            assertFailure(f, () -> f.personal.addWord(input), messages[i]);
            assertSame(before, f.personal.details());
        }
        VocabularyWord maximum = add(f, "w".repeat(80), "t".repeat(50), "d".repeat(2000),
                "e".repeat(2000), "s".repeat(500));
        assertEquals(2000, maximum.getExample().length());
        assertEquals(500, maximum.getSource().length());
    }

    @Test void nullRequestsAndInvalidDefinitionTargetsProduceFailureWithoutChanges() {
        UseCaseFixture f = learner();
        add(f, "existing", "Personal", "Keep this meaning.", "", "");
        LearningProgress before = f.personal.details();
        assertFailure(f, () -> f.personal.addWord(null), "Please provide a personal word");
        assertFailure(f, () -> f.personal.changeDefinition(null), "Please provide a definition change");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput(null, "meaning")),
                "Please select a word");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("missing", "meaning")),
                "Please select a word");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("w0", null)),
                "Definition is required");
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("w0", "d".repeat(2001))),
                "Definition must be at most 2000");
        assertSame(before, f.personal.details());
        f.auth.logout();
        assertFailure(f, () -> f.personal.changeDefinition(new PersonalDefinitionInput("w0", "meaning")),
                "Please log in first");
        assertThrows(AppException.class, f.personal::details);
    }

    @Test void eachSuccessIsPresentedExactlyOnceAfterItsSaveAndContainsCommittedValues() {
        UseCaseFixture f = learner();
        List<String> events = new ArrayList<>();
        List<PersonalVocabularyResponse> responses = new ArrayList<>();
        ProgressRepository repository = new ProgressRepository() {
            public LearningProgress getProgress(String username) { return f.store.getProgress(username); }
            public void saveProgress(String username, LearningProgress value) {
                f.store.saveProgress(username, value);
                events.add("saved");
            }
        };
        PersonalVocabularyOutputBoundary output = new PersonalVocabularyOutputBoundary() {
            public void presentSuccess(PersonalVocabularyResponse response) {
                LearningProgress persisted = f.store.getProgress("alice");
                VocabularyWord word = ContentQueries.requireVisibleWord(f.store, persisted, response.wordId);
                assertEquals(word.getTerm(), response.term);
                assertEquals(word.getDefinition(), response.definition);
                responses.add(response);
                events.add(response.action.name());
            }
            public void presentFailure(String message) { fail("Unexpected failure: " + message); }
        };
        PersonalVocabularyInteractor interactor = new PersonalVocabularyInteractor(f.store, repository, f.session, output);
        interactor.addWord(new PersonalWordInput(" new ", " Travel ", " initial ", "", ""));
        String id = responses.get(0).wordId;
        interactor.changeDefinition(new PersonalDefinitionInput(id, " revised "));
        assertEquals(List.of("saved", "ADDED", "saved", "DEFINITION_CHANGED"), events);
        assertEquals(2, responses.size());
        assertEquals("initial", responses.get(0).definition);
        assertEquals("revised", responses.get(1).definition);
        assertEquals(id, responses.get(1).wordId);
    }

    @Test void failedReadsReportOnlyFailureAndNeverWriteProgress() {
        UseCaseFixture f = learner();
        ProgressRepository repository = new ProgressRepository() {
            public LearningProgress getProgress(String username) { throw new AppException("Cannot read progress."); }
            public void saveProgress(String username, LearningProgress value) { fail("Failed reads must not save."); }
        };
        PersonalVocabularyInteractor interactor = new PersonalVocabularyInteractor(
                f.store, repository, f.session, f.personalOutput);
        assertFailure(f, () -> interactor.addWord(new PersonalWordInput("new", "Topic", "meaning", "", "")),
                "Cannot read progress");
        assertFailure(f, () -> interactor.changeDefinition(new PersonalDefinitionInput("w0", "meaning")),
                "Cannot read progress");
        assertTrue(f.store.getProgress("alice").getPersonalWords().isEmpty());
    }

    @Test void presenterFailuresAreNotMisreportedAsBusinessFailuresOrRetried() {
        UseCaseFixture f = learner();
        AppException presentationFailure = new AppException("Presenter failed.");
        List<String> callbacks = new ArrayList<>();
        PersonalVocabularyOutputBoundary output = new PersonalVocabularyOutputBoundary() {
            public void presentSuccess(PersonalVocabularyResponse response) {
                callbacks.add("success");
                throw presentationFailure;
            }
            public void presentFailure(String message) {
                callbacks.add("failure");
                throw presentationFailure;
            }
        };
        PersonalVocabularyInteractor interactor = new PersonalVocabularyInteractor(f.store, f.store, f.session, output);
        assertSame(presentationFailure, assertThrows(AppException.class,
                () -> interactor.addWord(new PersonalWordInput("new", "Topic", "meaning", "", ""))));
        assertEquals(1, f.store.getProgress("alice").getPersonalWords().size());
        assertSame(presentationFailure, assertThrows(AppException.class,
                () -> interactor.changeDefinition(new PersonalDefinitionInput("w0", "revised"))));
        assertEquals("revised", f.store.getProgress("alice").getDefinitions().get("w0"));
        assertSame(presentationFailure, assertThrows(AppException.class, () -> interactor.addWord(null)));
        assertSame(presentationFailure, assertThrows(AppException.class, () -> interactor.changeDefinition(null)));
        assertEquals(List.of("success", "success", "failure", "failure"), callbacks);
    }

    private static UseCaseFixture learner() {
        UseCaseFixture fixture = new UseCaseFixture();
        fixture.admin();
        fixture.learner("alice");
        return fixture;
    }
}
