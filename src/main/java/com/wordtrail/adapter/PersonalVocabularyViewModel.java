package com.wordtrail.adapter;

/** One synchronous edit result; the controller clears it before each request. */
public final class PersonalVocabularyViewModel {
    private SaveResult result;

    void beginRequest() {
        result = null;
    }

    void complete(SaveResult completed) {
        result = completed;
    }

    SaveResult result() {
        if (result == null) {
            throw new IllegalStateException("The personal vocabulary request did not produce a result.");
        }
        return result;
    }

    /** Display data only: no entity, repository, or Swing object crosses this boundary. */
    public static final class SaveResult {
        public final boolean success;
        public final String wordId;
        public final String term;
        public final String definition;
        public final String message;

        SaveResult(boolean success, String wordId, String term, String definition, String message) {
            this.success = success;
            this.wordId = wordId;
            this.term = term;
            this.definition = definition;
            this.message = message;
        }
    }
}
