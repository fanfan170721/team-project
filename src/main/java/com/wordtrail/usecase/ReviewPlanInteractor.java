package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** UC-10: selects a daily plan from the signed-in learner's vocabulary and review history. */
public final class ReviewPlanInteractor implements ReviewPlanInputBoundary {
    private static final Comparator<ReviewPlanEntry> PLAN_ORDER = Comparator
            .comparing((ReviewPlanEntry entry) -> entry.hasReviewDate)
            .thenComparing(entry -> entry.dueDate)
            .thenComparing(entry -> entry.term, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(entry -> entry.wordId);

    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;
    private final Clock clock;
    private final ReviewPlanOutputBoundary output;

    public ReviewPlanInteractor(ContentRepository content, ProgressRepository progress, SessionContext session,
                                Clock clock, ReviewPlanOutputBoundary output) {
        this.content = content;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
        this.output = output;
    }

    @Override
    public void execute(String topic, int limit) {
        if (limit < 1 || limit > 20) {
            output.presentFailure("Enter a word limit from 1 to 20.");
            return;
        }
        String username;
        try {
            username = session.requireUser().getUsername();
        } catch (AppException failure) {
            output.presentFailure(failure.getMessage());
            return;
        }

        LearningProgress history;
        List<VocabularyWord> words;
        try {
            history = progress.getProgress(username);
            words = ContentQueries.visibleWords(content, history);
        } catch (RuntimeException failure) {
            output.presentFailure("Could not read your vocabulary or review history. Please try again.");
            return;
        }

        LocalDate today = LocalDate.now(clock);
        String selectedTopic = topic == null ? "" : topic.strip();
        List<ReviewPlanEntry> entries = words.stream()
                .filter(word -> selectedTopic.isEmpty() || word.getTopic().equalsIgnoreCase(selectedTopic))
                .map(word -> schedule(word, history, today))
                .filter(entry -> !entry.dueDate.isAfter(today))
                .sorted(PLAN_ORDER)
                .limit(limit)
                .collect(Collectors.toList());
        output.presentPlan(new ReviewPlanResponse(entries));
    }

    private ReviewPlanEntry schedule(VocabularyWord word, LearningProgress history, LocalDate today) {
        Instant lastReviewed = history.getLastReviewed().get(word.getId());
        if (lastReviewed == null) {
            // Legacy progress can include a review count without a review date.
            return new ReviewPlanEntry(word, today, "No recorded review date. Eligible today.", false);
        }

        // A recorded date is evidence of one review even when the stored count is missing or invalid.
        Integer reviews = history.getReviewCounts().get(word.getId());
        if (reviews == null || reviews <= 1) {
            return reviewedEntry(word, lastReviewed, 1, "Due 1 day after the last review.");
        }
        if (reviews == 2) {
            return reviewedEntry(word, lastReviewed, 3, "Due 3 days after the last review.");
        }
        return reviewedEntry(word, lastReviewed, 7, "Due 7 days after the last review.");
    }

    private ReviewPlanEntry reviewedEntry(VocabularyWord word, Instant lastReviewed, int days, String reason) {
        LocalDate due = lastReviewed.atZone(clock.getZone()).toLocalDate().plusDays(days);
        return new ReviewPlanEntry(word, due, reason, true);
    }
}
