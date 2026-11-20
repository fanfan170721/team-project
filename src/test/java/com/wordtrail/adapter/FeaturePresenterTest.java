package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.FlashcardUseCase;
import com.wordtrail.usecase.ProgressUseCase;
import com.wordtrail.usecase.QuizUseCase;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeaturePresenterTest {
    private final ZoneId zone = ZoneId.of("America/New_York");

    @Test void unrevealedCardsDoNotExposeDefinitionsOrExamples() {
        VocabularyWord word = new VocabularyWord("id", "term", "topic", "secret definition", "secret example", "source");
        FlashcardPresenter presenter = new FlashcardPresenter();
        ViewModels.Card card = presenter.card(new FlashcardUseCase.Card(word, 1, 3, false, false), Collections.emptySet());
        assertFalse(card.word.definition.contains("secret"));
        assertEquals("", card.word.example);
        assertEquals("", card.word.source);
    }

    @Test void dashboardUsesWeightedAccuracyAndFillsMissingDaysWithZeroes() {
        LocalDate today = LocalDate.of(2026, 10, 22);
        LearningProgress progress = LearningProgress.empty().withStudiedWord("id", today)
                .withAttempt(new QuizAttempt(Instant.parse("2026-10-22T15:00:00Z"), "Travel", 1, 1))
                .withAttempt(new QuizAttempt(Instant.parse("2026-10-22T16:00:00Z"), "Nature", 0, 3));
        ViewModels.Dashboard view = new ProgressPresenter(zone).dashboard(
                new ProgressUseCase.Report(progress, 16, 1, today));
        assertEquals(25, view.accuracy);
        assertEquals(14, view.dailyRows.size());
        assertEquals("0", view.dailyRows.get(0)[1]);
        assertEquals("1", view.dailyRows.get(13)[1]);
        assertEquals("Nature", view.quizRows.get(0)[1]);
        assertEquals("2026-10-22 12:00", view.quizRows.get(0)[0]);
    }

    @Test void wordScreensPreserveSavedStatusCountsAndLocalReviewTime() {
        VocabularyWord word = new VocabularyWord("id", "term", "topic", "meaning", "example", "source");
        LocalDate day = LocalDate.of(2026, 11, 17);
        LearningProgress progress = LearningProgress.empty().withReviewWord("id", true)
                .withCompletedReview("id", day, Instant.parse("2026-11-17T21:20:30Z"));
        ViewModels.VocabularyProgress metadata = new PersonalVocabularyPresenter(
                zone, new PersonalVocabularyViewModel()).progress(progress);
        List<ViewModels.Library> screens = List.of(
                new CatalogPresenter().library(List.of("topic"), List.of(word), metadata),
                new ReviewPresenter().list(List.of("topic"), List.of(word), metadata),
                new ContentPresenter().sharedWords(List.of("topic"), List.of(word), metadata));
        for (ViewModels.Library screen : screens) {
            ViewModels.Word displayed = screen.words.get(0);
            assertTrue(displayed.saved);
            assertEquals(1, displayed.reviewCount);
            assertEquals("2026-11-17 16:20:30", displayed.lastReviewed);
            assertEquals("meaning", displayed.definition);
            assertEquals("source", displayed.source);
        }
        assertThrows(UnsupportedOperationException.class, () -> metadata.savedIds.clear());
        assertThrows(UnsupportedOperationException.class, () -> metadata.reviewCounts.put("id", 99));
    }

    @Test void quizFeedbackRemainsHiddenUntilSubmissionAndShowsFinalResult() {
        QuizPresenter presenter = new QuizPresenter();
        List<String> choices = List.of("first", "second", "third", "fourth");
        ViewModels.Quiz pending = presenter.quiz(new QuizUseCase.Question(
                "word", "Prompt", choices, 1, 2, false, false, -1, -1, 0));
        assertEquals("", pending.feedback);
        ViewModels.Quiz finished = presenter.quiz(new QuizUseCase.Question(
                "word", "Prompt", choices, 2, 2, true, true, 0, 1, 1));
        assertTrue(finished.feedback.contains("Correct answer: second"));
        assertTrue(finished.feedback.contains("1 / 2 (50%)"));
        assertTrue(finished.finished);
    }
}
