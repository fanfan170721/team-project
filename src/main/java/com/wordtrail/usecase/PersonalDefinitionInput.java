package com.wordtrail.usecase;

/** A requested private definition override for a word visible to the account. */
public final class PersonalDefinitionInput {
    public final String wordId;
    public final String definition;

    public PersonalDefinitionInput(String wordId, String definition) {
        this.wordId = wordId;
        this.definition = definition;
    }
}
