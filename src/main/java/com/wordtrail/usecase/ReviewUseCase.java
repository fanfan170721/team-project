package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;
import java.util.List;
import java.util.Set;

public interface ReviewUseCase {
    List<VocabularyWord> list();
    Set<String> savedIds();
    void setSaved(String wordId, boolean saved);
}
