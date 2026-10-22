package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.util.List;

public interface CatalogUseCase {
    List<String> topics();
    List<VocabularyWord> browse(String topic, String search);
    DictionaryGateway.Entry lookup(String term);
}
