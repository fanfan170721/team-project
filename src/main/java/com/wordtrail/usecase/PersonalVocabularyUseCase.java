package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;

/** Personal additions and definition overrides never modify the shared word bank. */
public interface PersonalVocabularyUseCase {
    void addWord(PersonalWordInput input);
    void changeDefinition(PersonalDefinitionInput input);
    LearningProgress details();
}
