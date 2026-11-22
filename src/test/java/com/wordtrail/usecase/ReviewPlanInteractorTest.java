package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewPlanInteractorTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T16:00:00Z"), TORONTO);

    private final UseCaseFixture fixture = new UseCaseFixture();
    private final PlanRepositories repositories = new PlanRepositories(fixture.store);

    @BeforeEach
    void prepareVocabulary() {
        fixture.admin();
        fixture.learner("alice");
        fixture.store.words.clear();
    }

    @Test
    void includesNeverReviewedAndEachIntervalDueTodayButExcludesTomorrow() {
        addWords("never", "one-due", "one-future", "three-due", "three-future", "seven-due", "seven-future",
                "seven-after-four-reviews");
        setHistory(Map.of("one-due", 1, "one-future", 1, "three-due", 2, "three-future", 2,
                        "seven-due", 3, "seven-future", 3, "seven-after-four-reviews", 4),
                Map.of("one-due", daysAgo(1), "one-future", daysAgo(0), "three-due", daysAgo(3),
                        "three-future", daysAgo(2), "seven-due", daysAgo(7), "seven-future", daysAgo(6),
                        "seven-after-four-reviews", daysAgo(7)));

        List<ReviewPlanEntry> entries = generate(null, 20);

        assertEquals(List.of("never", "one-due", "seven-after-four-reviews", "seven-due", "three-due"), ids(entries));
        assertTrue(entries.stream().allMatch(entry -> TODAY.equals(entry.dueDate)));
        assertTrue(entries.get(0).reason.contains("Eligible today"));
        assertTrue(entries.get(1).reason.contains("1 day"));
        assertTrue(entries.get(2).reason.contains("7 days"));
        assertTrue(entries.get(4).reason.contains("3 days"));
    }

    @Test
    void convertsBothTodayAndLastReviewToTheClocksLocalDate() {
        addWords("never", "before-midnight", "at-midnight");
        setHistory(Map.of("before-midnight", 1, "at-midnight", 1),
                Map.of("before-midnight", Instant.parse("2026-11-17T04:59:59Z"),
                        "at-midnight", Instant.parse("2026-11-17T05:00:00Z")));
        Instant currentTime = Instant.parse("2026-11-18T02:00:00Z");

        List<ReviewPlanEntry> torontoPlan = generate(null, 20, Clock.fixed(currentTime, TORONTO));
        List<ReviewPlanEntry> utcPlan = generate(null, 20, Clock.fixed(currentTime, ZoneOffset.UTC));

        assertEquals(List.of("never", "before-midnight"), ids(torontoPlan));
        assertTrue(torontoPlan.stream().allMatch(entry -> TODAY.equals(entry.dueDate)));
        assertEquals(List.of("never", "at-midnight", "before-midnight"), ids(utcPlan));
        assertTrue(utcPlan.stream().allMatch(entry -> TODAY.plusDays(1).equals(entry.dueDate)));
    }

    @Test
    void missingReviewDateIsEligibleAndRecordedDateSuppliesMinimumReviewCount() {
        addWords("undated", "absent-count", "zero-count", "null-count", "negative-count", "dated-today");
        Map<String, Integer> counts = new HashMap<>(Map.of("undated", 8, "zero-count", 0, "negative-count", -2));
        counts.put("null-count", null);
        setHistory(counts, Map.of("absent-count", daysAgo(1), "zero-count", daysAgo(1), "null-count", daysAgo(1),
                "negative-count", daysAgo(1), "dated-today", daysAgo(0)));
        LearningProgress originalHistory = fixture.store.progress.get("alice");

        List<ReviewPlanEntry> entries = generate(null, 20);

        assertEquals(List.of("undated", "absent-count", "negative-count", "null-count", "zero-count"), ids(entries));
        assertTrue(entries.stream().allMatch(entry -> TODAY.equals(entry.dueDate)));
        assertTrue(entries.get(0).reason.contains("No recorded review date"));
        assertTrue(entries.subList(1, entries.size()).stream().allMatch(entry -> entry.reason.contains("1 day")));
        assertSame(originalHistory, fixture.store.progress.get("alice"));
        assertEquals(0, repositories.saveCount);
    }

    @Test
    void filtersByTopicAndTreatsNullEmptyAndBlankTopicsAsAllTopics() {
        addWords("travel-word");
        fixture.store.words.add(word("nature-word", "fern", "Nature"));

        assertEquals(List.of("travel-word"), ids(generate("  tRaVeL  ", 20)));
        assertEquals(List.of("nature-word"), ids(generate("NATURE", 20)));
        List<String> allIds = List.of("nature-word", "travel-word");
        assertEquals(allIds, ids(generate(null, 20)));
        assertEquals(allIds, ids(generate("", 20)));
        assertEquals(allIds, ids(generate("  ", 20)));
    }

    @Test
    void prioritizesUndatedThenOrdersByDueDateTermAndIdBeforeLimiting() {
        fixture.store.words.addAll(List.of(word("b-undated", "Alpha", "Travel"),
                word("oldest", "Zulu", "Travel"), word("today", "Aardvark", "Travel"),
                word("b-overdue", "Bravo", "Travel"), word("last-undated", "Zulu", "Travel"),
                word("a-undated", "alpha", "Travel"), word("a-overdue", "alpha", "Travel")));
        setHistory(Map.of("oldest", 1, "today", 1, "b-overdue", 1, "a-overdue", 1),
                Map.of("oldest", daysAgo(4), "today", daysAgo(1), "b-overdue", daysAgo(3),
                        "a-overdue", daysAgo(3)));

        List<ReviewPlanEntry> entries = generate(null, 20);

        assertEquals(List.of("a-undated", "b-undated", "last-undated", "oldest", "a-overdue", "b-overdue", "today"),
                ids(entries));
        assertEquals(TODAY.minusDays(3), entries.get(3).dueDate);
        assertEquals(TODAY.minusDays(2), entries.get(4).dueDate);
        assertEquals(List.of("a-undated"), ids(generate(null, 1)));
        assertEquals(List.of("a-undated", "b-undated", "last-undated", "oldest"), ids(generate(null, 4)));
    }

    @Test
    void acceptsBothLimitBoundariesAndCapsTheSortedResultAtTwenty() {
        for (int number = 21; number >= 1; number--) {
            addWords(String.format(Locale.ROOT, "word-%02d", number));
        }

        assertEquals(List.of("word-01"), ids(generate(null, 1)));
        List<String> result = ids(generate(null, 20));
        assertEquals(20, result.size());
        assertEquals("word-01", result.get(0));
        assertEquals("word-20", result.get(19));
        assertFalse(result.contains("word-21"));
    }

    @Test
    void rejectsLimitsOutsideOneThroughTwentyBeforeReadingRepositories() {
        assertTrue(failure(0).contains("1 to 20"));
        assertTrue(failure(21).contains("1 to 20"));
        assertTrue(repositories.progressReads.isEmpty());
        assertEquals(0, repositories.contentReads);
    }

    @Test
    void returnsSuccessfulEmptyPlansWhenNothingIsDueOrNothingMatches() {
        addWords("future");
        setHistory(Map.of("future", 2), Map.of("future", daysAgo(2)));

        assertTrue(generate("Travel", 20).isEmpty());
        assertTrue(generate("Nature", 20).isEmpty());
    }

    @Test
    void usesOnlyTheSignedInAccountsWordsDefinitionsAndReviewHistory() {
        addWords("shared", "scheduled");
        fixture.store.progress.put("alice", LearningProgress.empty()
                .withPersonalWord(word("alice-private", "alice word", "Travel"))
                .withDefinition("alice-private", "Alice's personal word definition")
                .withDefinition("shared", "Alice's shared word definition"));
        fixture.store.progress.put("bob", LearningProgress.empty()
                .withPersonalWord(word("bob-private", "bob word", "Travel"))
                .withDefinition("shared", "Bob's shared word definition")
                .withCompletedReview("scheduled", TODAY, CLOCK.instant()));
        RecordingOutput output = new RecordingOutput();
        ReviewPlanInputBoundary plan = interactor(CLOCK, output);

        plan.execute(null, 20);
        List<ReviewPlanEntry> alicePlan = output.singlePlan();
        assertEquals(List.of("alice-private", "scheduled", "shared"), ids(alicePlan));
        assertEquals("alice word", alicePlan.get(0).term);
        assertEquals("Alice's personal word definition", alicePlan.get(0).definition);
        assertEquals("Definition of scheduled", alicePlan.get(1).definition);
        assertEquals("Alice's shared word definition", alicePlan.get(2).definition);

        fixture.learner("bob");
        output.plans.clear();
        plan.execute(null, 20);
        List<ReviewPlanEntry> bobPlan = output.singlePlan();
        assertEquals(List.of("bob-private", "shared"), ids(bobPlan));
        assertEquals("Definition of bob word", bobPlan.get(0).definition);
        assertEquals("Bob's shared word definition", bobPlan.get(1).definition);
        assertEquals(List.of("alice", "bob"), repositories.progressReads);
    }

    @Test
    void reportsFailureWithoutReadingRepositoriesWhenSignedOut() {
        fixture.auth.logout();

        assertTrue(failure(20).toLowerCase(Locale.ROOT).contains("log in"));
        assertTrue(repositories.progressReads.isEmpty());
        assertEquals(0, repositories.contentReads);
    }

    @Test
    void reportsProgressReadFailureWithoutExposingRepositoryDetails() {
        repositories.progressFailure = new AppException("Private account history");

        String message = failure(20);

        assertTrue(message.contains("read"));
        assertFalse(message.contains("Private account history"));
        assertEquals(List.of("alice"), repositories.progressReads);
    }

    @Test
    void reportsVocabularyReadFailureWithoutExposingRepositoryDetails() {
        repositories.contentFailure = new IllegalStateException("Private account vocabulary");

        String message = failure(20);

        assertTrue(message.contains("read"));
        assertFalse(message.contains("Private account vocabulary"));
        assertEquals(1, repositories.contentReads);
    }

    @Test
    void repeatedGenerationDoesNotSaveOrChangeProgress() {
        addWords("never", "reviewed");
        LearningProgress original = LearningProgress.empty()
                .withCompletedReview("reviewed", TODAY.minusDays(1), daysAgo(1));
        fixture.store.progress.put("alice", original);
        RecordingOutput output = new RecordingOutput();
        ReviewPlanInputBoundary plan = interactor(CLOCK, output);

        plan.execute(null, 20);
        plan.execute(null, 20);

        assertEquals(2, output.plans.size());
        assertTrue(output.failures.isEmpty());
        assertEquals(List.of("never", "reviewed"), ids(output.plans.get(0).entries));
        assertEquals(List.of("never", "reviewed"), ids(output.plans.get(1).entries));
        assertEquals(0, repositories.saveCount);
        assertSame(original, fixture.store.progress.get("alice"));
    }

    private void addWords(String... ids) {
        for (String id : ids) {
            fixture.store.words.add(word(id, id, "Travel"));
        }
    }

    private static VocabularyWord word(String id, String term, String topic) {
        return new VocabularyWord(id, term, topic, "Definition of " + term, "", "test fixture");
    }

    private static Instant daysAgo(int days) {
        return TODAY.minusDays(days).atTime(12, 0).atZone(TORONTO).toInstant();
    }

    private void setHistory(Map<String, Integer> counts, Map<String, Instant> dates) {
        fixture.store.progress.put("alice", new LearningProgress(Set.of(), Map.of(), List.of(), List.of(), Map.of(),
                counts, dates));
    }

    private List<ReviewPlanEntry> generate(String topic, int limit) {
        return generate(topic, limit, CLOCK);
    }

    private List<ReviewPlanEntry> generate(String topic, int limit, Clock clock) {
        RecordingOutput output = new RecordingOutput();
        interactor(clock, output).execute(topic, limit);
        return output.singlePlan();
    }

    private String failure(int limit) {
        RecordingOutput output = new RecordingOutput();
        interactor(CLOCK, output).execute(null, limit);
        assertTrue(output.plans.isEmpty());
        assertEquals(1, output.failures.size());
        return output.failures.get(0);
    }

    private ReviewPlanInputBoundary interactor(Clock clock, RecordingOutput output) {
        return new ReviewPlanInteractor(repositories, repositories, fixture.session, clock, output);
    }

    private static List<String> ids(List<ReviewPlanEntry> entries) {
        return entries.stream().map(entry -> entry.wordId).collect(Collectors.toList());
    }

    private static final class RecordingOutput implements ReviewPlanOutputBoundary {
        private final List<ReviewPlanResponse> plans = new ArrayList<>();
        private final List<String> failures = new ArrayList<>();

        @Override
        public void presentPlan(ReviewPlanResponse response) {
            plans.add(response);
        }

        @Override
        public void presentFailure(String message) {
            failures.add(message);
        }

        private List<ReviewPlanEntry> singlePlan() {
            assertTrue(failures.isEmpty(), () -> "Unexpected failures: " + failures);
            assertEquals(1, plans.size());
            return plans.get(0).entries;
        }
    }

    private static final class PlanRepositories implements ContentRepository, ProgressRepository {
        private final UseCaseFixture.MemoryStore store;
        private final List<String> progressReads = new ArrayList<>();
        private int contentReads;
        private int saveCount;
        private RuntimeException contentFailure;
        private RuntimeException progressFailure;

        private PlanRepositories(UseCaseFixture.MemoryStore store) {
            this.store = store;
        }

        @Override
        public List<VocabularyWord> getWords() {
            contentReads++;
            if (contentFailure != null) {
                throw contentFailure;
            }
            return store.getWords();
        }

        @Override
        public LearningProgress getProgress(String username) {
            progressReads.add(username);
            if (progressFailure != null) {
                throw progressFailure;
            }
            return store.getProgress(username);
        }

        @Override
        public void saveProgress(String username, LearningProgress progress) {
            saveCount++;
            store.saveProgress(username, progress);
        }

        @Override
        public List<QuizQuestion> getQuestions() {
            throw new AssertionError("Review planning does not use quiz questions.");
        }

        @Override
        public void addWord(VocabularyWord word) {
            throw new AssertionError("Review planning does not write vocabulary.");
        }

        @Override
        public void addQuestion(QuizQuestion question) {
            throw new AssertionError("Review planning does not write quiz questions.");
        }
    }
}
