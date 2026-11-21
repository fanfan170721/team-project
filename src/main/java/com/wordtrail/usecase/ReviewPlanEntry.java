package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import java.time.LocalDate;

/** One due word with the signed-in learner's effective definition. */
public final class ReviewPlanEntry {
    public final String wordId;
    public final String term;
    public final String definition;
    public final LocalDate dueDate;
    public final String reason;
    final boolean hasReviewDate;

    ReviewPlanEntry(VocabularyWord word, LocalDate dueDate, String reason, boolean hasReviewDate) {
        this.wordId = word.getId();
        this.term = word.getTerm();
        this.definition = word.getDefinition();
        this.dueDate = dueDate;
        this.reason = reason;
        this.hasReviewDate = hasReviewDate;
    }
}
