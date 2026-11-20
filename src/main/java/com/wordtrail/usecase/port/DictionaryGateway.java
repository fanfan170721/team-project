package com.wordtrail.usecase.port;

/** External service boundary; the core does not depend on HTTP or JSON. */
public interface DictionaryGateway {
    Entry lookup(String term);

    final class Entry {
        public final String term;
        public final String definition;
        public final String example;
        public final String source;

        public Entry(String term, String definition, String example, String source) {
            this.term = term;
            this.definition = definition;
            this.example = example;
            this.source = source;
        }
    }
}
