package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class ContentQueries {
    private ContentQueries() { }

    static VocabularyWord requireWord(ContentRepository repository, String id) {
        return repository.getWords().stream().filter(word -> word.getId().equals(id)).findFirst()
                .orElseThrow(() -> new AppException("Please select a vocabulary word."));
    }

    static List<VocabularyWord> filter(List<VocabularyWord> words, String topic, String query) {
        String search = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return words.stream().filter(word -> topic == null || topic.isBlank() || word.getTopic().equalsIgnoreCase(topic))
                .filter(word -> word.getTerm().toLowerCase(Locale.ROOT).contains(search)
                        || word.getDefinition().toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparing(VocabularyWord::getTerm, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
