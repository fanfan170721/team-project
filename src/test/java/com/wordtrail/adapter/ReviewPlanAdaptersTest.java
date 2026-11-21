package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.User;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.AuthInteractor;
import com.wordtrail.usecase.ReviewPlanInteractor;
import com.wordtrail.usecase.SessionContext;
import com.wordtrail.usecase.port.AccountRepository;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.PasswordHasher;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewPlanAdaptersTest {
    @Test
    void forwardsInputsOnceAndDisplaysEveryFieldInUseCaseOrder() {
        Fixture fixture = new Fixture();

        ReviewPlanViewModel.Result result = fixture.controller.generate("  TrAvEl  ", 2);

        assertEquals(1, fixture.executions);
        assertEquals("  TrAvEl  ", fixture.receivedTopic);
        assertEquals(2, fixture.receivedLimit);
        assertTrue(result.success);
        assertEquals("2 words are due for review.", result.message);
        assertEquals(2, result.rows.size());
        assertRow(result.rows.get(0), "undated", "zebra", "My striped animal.", "2026-11-17",
                "No recorded review date. Eligible today.");
        assertRow(result.rows.get(1), "reviewed", "aardvark", "A burrowing animal.", "2026-11-16",
                "Due 1 day after the last review.");
    }

    @Test
    void anEmptySuccessClearsThePreviousPlanWithAnExplicitMessage() {
        Fixture fixture = new Fixture();
        assertEquals(2, fixture.controller.generate(null, 2).rows.size());

        ReviewPlanViewModel.Result empty = fixture.controller.generate("No matching topic", 2);

        assertTrue(empty.success);
        assertTrue(empty.rows.isEmpty());
        assertEquals("No due words match these filters.", empty.message);
    }

    @Test
    void failureClearsRowsAndTheNextSuccessClearsTheError() {
        Fixture fixture = new Fixture();
        assertEquals(2, fixture.controller.generate(null, 2).rows.size());

        ReviewPlanViewModel.Result failed = fixture.controller.generate(null, 0);

        assertFalse(failed.success);
        assertTrue(failed.rows.isEmpty());
        assertEquals("Enter a word limit from 1 to 20.", failed.message);

        ReviewPlanViewModel.Result recovered = fixture.controller.generate(null, 1);
        assertTrue(recovered.success);
        assertEquals(1, recovered.rows.size());
        assertEquals("1 word is due for review.", recovered.message);
    }

    @Test
    void missingOutputCannotReuseAnEarlierPlan() {
        Fixture fixture = new Fixture();
        assertTrue(fixture.controller.generate(null, 2).success);
        fixture.omitOutput = true;

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> fixture.controller.generate(null, 2));

        assertEquals("The review plan request did not produce a result.", failure.getMessage());
        assertEquals(2, fixture.executions);
    }

    @Test
    void returnedRowsAreImmutableSnapshotsAcrossRequests() {
        Fixture fixture = new Fixture();
        ReviewPlanViewModel.Result earlier = fixture.controller.generate(null, 2);

        assertThrows(UnsupportedOperationException.class, () -> earlier.rows.clear());
        assertEquals(1, fixture.controller.generate(null, 1).rows.size());
        assertTrue(fixture.controller.generate("No matching topic", 2).rows.isEmpty());

        assertTrue(earlier.success);
        assertEquals("2 words are due for review.", earlier.message);
        assertEquals(2, earlier.rows.size());
        assertRow(earlier.rows.get(0), "undated", "zebra", "My striped animal.", "2026-11-17",
                "No recorded review date. Eligible today.");
        assertEquals("reviewed", earlier.rows.get(1).wordId);
    }

    private static void assertRow(ReviewPlanViewModel.Row row, String wordId, String term, String definition,
                                  String dueDate, String reason) {
        assertEquals(wordId, row.wordId);
        assertEquals(term, row.term);
        assertEquals(definition, row.definition);
        assertEquals(dueDate, row.dueDate);
        assertEquals(reason, row.reason);
    }

    private static final class Fixture {
        private final ReviewPlanController controller;
        private int executions;
        private String receivedTopic;
        private int receivedLimit;
        private boolean omitOutput;

        private Fixture() {
            MemoryRepositories repositories = new MemoryRepositories();
            SessionContext session = new SessionContext();
            AuthInteractor auth = new AuthInteractor(repositories, new PasswordHasher() {
                @Override
                public String hash(char[] password) {
                    return "test-hash:" + new String(password);
                }

                @Override
                public boolean matches(char[] password, String encodedHash) {
                    return encodedHash.equals(hash(password));
                }
            }, session);
            auth.setupAdministrator("admin", "test-password".toCharArray());
            auth.register("learner", "test-password".toCharArray());
            Clock clock = Clock.fixed(Instant.parse("2026-11-18T02:00:00Z"), ZoneId.of("America/Toronto"));
            ReviewPlanViewModel model = new ReviewPlanViewModel();
            ReviewPlanPresenter presenter = new ReviewPlanPresenter(model);
            ReviewPlanInteractor interactor = new ReviewPlanInteractor(repositories, repositories, session,
                    clock, presenter);
            controller = new ReviewPlanController((topic, limit) -> {
                executions++;
                receivedTopic = topic;
                receivedLimit = limit;
                if (!omitOutput) {
                    interactor.execute(topic, limit);
                }
            }, model);
        }
    }

    private static final class MemoryRepositories implements AccountRepository, ContentRepository, ProgressRepository {
        private final Map<String, User> accounts = new HashMap<>();
        private final LearningProgress history = LearningProgress.empty()
                .withDefinition("undated", "My striped animal.")
                .withCompletedReview("reviewed", LocalDate.of(2026, 11, 15),
                        Instant.parse("2026-11-15T19:00:00Z"));

        @Override
        public Optional<User> findUser(String username) {
            return Optional.ofNullable(accounts.get(username));
        }

        @Override
        public boolean hasAccounts() {
            return !accounts.isEmpty();
        }

        @Override
        public void createUser(User user) {
            accounts.put(user.getUsername(), user);
        }

        @Override
        public List<VocabularyWord> getWords() {
            return List.of(new VocabularyWord("reviewed", "aardvark", "Travel", "A burrowing animal.", "", ""),
                    new VocabularyWord("undated", "zebra", "Travel", "A striped animal.", "", ""));
        }

        @Override
        public LearningProgress getProgress(String username) {
            return history;
        }

        @Override
        public void saveProgress(String username, LearningProgress progress) {
            throw new AssertionError("Generating a review plan must not save progress.");
        }

        @Override
        public List<QuizQuestion> getQuestions() {
            throw new AssertionError("Generating a review plan must not read quiz questions.");
        }

        @Override
        public void addWord(VocabularyWord word) {
            throw new AssertionError("Generating a review plan must not add words.");
        }

        @Override
        public void addQuestion(QuizQuestion question) {
            throw new AssertionError("Generating a review plan must not add quiz questions.");
        }
    }
}
