package com.wordtrail.usecase;

/** Presents exactly one expected result of a personal-vocabulary command. */
public interface PersonalVocabularyOutputBoundary {
    void presentSuccess(PersonalVocabularyResponse response);
    void presentFailure(String message);
}
