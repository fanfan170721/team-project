package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.DictionaryGateway;
import com.wordtrail.usecase.port.ProgressRepository;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

/** US-02: topic-based browsing and on-demand live dictionary lookup. */
public final class CatalogInteractor implements CatalogUseCase {
    private final ContentRepository content;
    private final DictionaryGateway dictionary;
    private final SessionContext session;
    private final ProgressRepository progress;

    public CatalogInteractor(ContentRepository content, ProgressRepository progress,
                             DictionaryGateway dictionary, SessionContext session) {
        this.content = content;
        this.dictionary = dictionary;
        this.session = session;
        this.progress = progress;
    }

    @Override public List<String> topics() {
        return ContentQueries.visibleWords(content, progress, session.requireUser().getUsername()).stream()
                .map(VocabularyWord::getTopic).distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    @Override public List<VocabularyWord> browse(String topic, String search) {
        return ContentQueries.filter(ContentQueries.visibleWords(content, progress,
                session.requireUser().getUsername()), topic, search);
    }

    @Override public DictionaryGateway.Entry lookup(String term) {
        session.requireUser();
        String word = InputValidation.text(term, "English word", 80).toLowerCase(Locale.ROOT);
        if (!word.matches("[a-zA-Z]+(?:[-' ][a-zA-Z]+)*")) {
            throw new AppException("Enter an English word using letters, spaces, apostrophes or hyphens.");
        }
        return dictionary.lookup(word);
    }
}
