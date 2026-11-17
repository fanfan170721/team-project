package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.util.List;
import java.util.stream.Collectors;

/** Presents browse results and keeps a live lookup separate from the saved definition. */
public final class CatalogPresenter {
    public ViewModels.Library library(List<String> topics, List<VocabularyWord> words,
                                      ViewModels.VocabularyProgress progress) {
        return new ViewModels.Library(topics, words.stream().map(word -> WordDisplay.from(word, progress))
                .collect(Collectors.toList()));
    }

    public ViewModels.Definition definition(DictionaryGateway.Entry entry) {
        return new ViewModels.Definition(entry.term, entry.definition, entry.example, entry.source);
    }
}
