package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;

/** Shared row mapping; use-case presenters decide which rows and result to display. */
final class WordDisplay {
    private WordDisplay() { }

    static ViewModels.Word from(VocabularyWord word, ViewModels.VocabularyProgress progress) {
        return new ViewModels.Word(word.getId(), word.getTerm(), word.getTopic(), word.getDefinition(),
                word.getExample(), word.getSource(), progress.savedIds.contains(word.getId()),
                progress.reviewCounts.getOrDefault(word.getId(), 0),
                progress.lastReviewed.getOrDefault(word.getId(), ""),
                progress.lastReviewedInstants.get(word.getId()));
    }
}
