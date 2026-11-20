package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;
import java.util.List;
import java.util.stream.Collectors;

/** Presents the shared word bank used by the administrator's question form. */
public final class ContentPresenter {
    public ViewModels.Library sharedWords(List<String> topics, List<VocabularyWord> words,
                                          ViewModels.VocabularyProgress progress) {
        return new ViewModels.Library(topics, words.stream().map(word -> WordDisplay.from(word, progress))
                .collect(Collectors.toList()));
    }
}
