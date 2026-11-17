package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;
import java.util.List;
import java.util.stream.Collectors;

/** Presents the review use case's saved words with their account-specific review history. */
public final class ReviewPresenter {
    public ViewModels.Library list(List<String> topics, List<VocabularyWord> savedWords,
                                   ViewModels.VocabularyProgress progress) {
        return new ViewModels.Library(topics, savedWords.stream().map(word -> WordDisplay.from(word, progress))
                .collect(Collectors.toList()));
    }
}
