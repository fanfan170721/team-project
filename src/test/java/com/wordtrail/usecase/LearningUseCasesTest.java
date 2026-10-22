package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LearningUseCasesTest {
    @Test void browsingAndReviewAreAuthenticatedFilteredAndAccountScoped() {
        UseCaseFixture f = new UseCaseFixture();
        assertThrows(AppException.class, () -> f.catalog.browse(null, ""));
        f.admin();
        assertEquals(3, f.catalog.browse("travel", "").size());
        assertEquals(1, f.catalog.browse(null, "WORD2").size());
        assertTrue(f.catalog.browse("Missing", "").isEmpty());
        assertEquals("API definition", f.catalog.lookup("hello").definition);
        assertThrows(AppException.class, () -> f.catalog.lookup("../bad"));
        f.review.setSaved("w0", true);
        f.review.setSaved("w0", true);
        assertEquals(1, f.review.list().size());
        assertThrows(AppException.class, () -> f.review.setSaved("missing", true));
        f.learner("alice");
        assertTrue(f.review.list().isEmpty());
        f.loginAdmin();
        f.review.setSaved("w0", false);
        assertTrue(f.review.list().isEmpty());
    }

    @Test void flashcardsRequireRevealAndCountDistinctWordsPerDay() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        assertThrows(AppException.class, () -> f.flash.start(null, true));
        f.review.setSaved("w0", true);
        FlashcardUseCase.Card card = f.flash.start(null, true);
        assertEquals("w0", card.word.getId());
        assertFalse(card.revealed);
        assertThrows(AppException.class, f.flash::markStudied);
        assertTrue(f.flash.reveal().revealed);
        assertTrue(f.flash.markStudied().studiedToday);
        f.flash.markStudied();
        assertEquals(1, f.progress.report().progress.getStudiedWordIds().size());
        assertFalse(f.flash.next().revealed);
        f.auth.logout();
        f.loginAdmin();
        assertThrows(AppException.class, f.flash::reveal);
    }

    @Test void streakIncludesYesterdayAndStopsAtAMissingDay() {
        UseCaseFixture f = new UseCaseFixture();
        f.admin();
        LocalDate today = LocalDate.now(f.clock);
        f.store.saveProgress("admin", LearningProgress.empty().withStudiedWord("w0", today.minusDays(1))
                .withStudiedWord("w1", today.minusDays(2)).withStudiedWord("w2", today.minusDays(4)));
        assertEquals(2, f.progress.report().studyStreak);
        f.store.saveProgress("admin", f.store.getProgress("admin").withStudiedWord("w0", today));
        assertEquals(3, f.progress.report().studyStreak);
        assertEquals(3, f.progress.report().progress.getStudiedWordIds().size());
    }

    @Test void repeatedReviewsCountPerVisitAndFailedSaveCanBeRetried() {
        UseCaseFixture f = new UseCaseFixture(); f.admin();
        f.review.setSaved("w0", true);
        f.flash.start(null, true); f.flash.reveal();
        f.store.failSave = true;
        assertThrows(AppException.class, f.flash::markStudied);
        f.store.failSave = false;
        assertEquals(1, f.flash.markStudied().reviewCount);
        assertEquals(1, f.flash.markStudied().reviewCount);
        f.flash.next(); f.flash.reveal();
        assertEquals(2, f.flash.markStudied().reviewCount);
        assertTrue(f.flash.markStudied().reviewed);
        assertEquals(1, f.progress.report().progress.getStudiedWordIds().size());
        assertEquals(1, f.progress.report().progress.getStudyDays().get(LocalDate.now(f.clock)).size());
        f.learner("alice");
        assertTrue(f.progress.report().progress.getReviewCounts().isEmpty());
    }
}
