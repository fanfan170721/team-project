package com.wordtrail.usecase;

/** Unvalidated form values for adding a word to the current account. */
public final class PersonalWordInput {
    public final String term;
    public final String topic;
    public final String definition;
    public final String example;
    public final String source;

    public PersonalWordInput(String term, String topic, String definition, String example, String source) {
        this.term = term;
        this.topic = topic;
        this.definition = definition;
        this.example = example;
        this.source = source;
    }
}
