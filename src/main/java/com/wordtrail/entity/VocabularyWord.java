package com.wordtrail.entity;

import java.util.Objects;

/** Immutable vocabulary content; use cases decide whether it is shared or personal. */
public final class VocabularyWord {
    private final String id;
    private final String term;
    private final String topic;
    private final String definition;
    private final String example;
    private final String source;

    public VocabularyWord(String id, String term, String topic, String definition,
                          String example, String source) {
        this.id = Objects.requireNonNull(id);
        this.term = Objects.requireNonNull(term);
        this.topic = Objects.requireNonNull(topic);
        this.definition = Objects.requireNonNull(definition);
        this.example = Objects.requireNonNull(example);
        this.source = Objects.requireNonNull(source);
    }

    public String getId() { return id; }
    public String getTerm() { return term; }
    public String getTopic() { return topic; }
    public String getDefinition() { return definition; }
    public String getExample() { return example; }
    public String getSource() { return source; }
}
