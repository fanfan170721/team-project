package com.wordtrail.adapter;

import com.wordtrail.entity.*;
import com.wordtrail.usecase.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppPresenterTest {
    private final AppPresenter presenter = new AppPresenter(ZoneId.of("America/New_York"));

    @Test void unrevealedCardsDoNotExposeDefinitionsOrExamples() {
        VocabularyWord word = new VocabularyWord("id", "term", "topic", "secret definition", "secret example", "source");
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
        ViewModels.Dashboard view = presenter.dashboard(new ProgressUseCase.Report(progress, 16, 1, today));
        assertEquals(25, view.accuracy);
        assertEquals(14, view.dailyRows.size());
        assertEquals("0", view.dailyRows.get(0)[1]);
        assertEquals("1", view.dailyRows.get(13)[1]);
        assertEquals("Nature", view.quizRows.get(0)[1]);
        assertEquals("2026-10-22 12:00", view.quizRows.get(0)[0]);
    }
}
