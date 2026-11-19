package com.wordtrail.usecase;

/** The committed personal-vocabulary change, without repository or UI objects. */
public final class PersonalVocabularyResponse {
    public enum Action { ADDED, DEFINITION_CHANGED }

    public final Action action;
    public final String wordId;
    public final String term;
    public final String definition;

    public PersonalVocabularyResponse(Action action, String wordId, String term, String definition) {
        this.action = action;
        this.wordId = wordId;
        this.term = term;
        this.definition = definition;
    }
}
