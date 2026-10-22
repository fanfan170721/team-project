package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;

/** Personal additions and definition overrides never modify the shared word bank. */
public interface PersonalVocabularyUseCase {
    VocabularyWord addWord(String term, String topic, String definition, String example, String source);
    void changeDefinition(String wordId, String definition);
    LearningProgress details();
}
