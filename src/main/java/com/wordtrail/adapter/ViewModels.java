package com.wordtrail.adapter;

import java.util.List;

/** Plain display data. Swing views do not import entities, repositories or interactors. */
public final class ViewModels {
    private ViewModels() { }

    public static final class Account {
        public final String username;
        public final boolean admin;
        public Account(String username, boolean admin) { this.username = username; this.admin = admin; }
    }

    public static final class Word {
        public final String id;
        public final String term;
        public final String topic;
        public final String definition;
        public final String example;
        public final String source;
        public final boolean saved;
        public Word(String id, String term, String topic, String definition, String example, String source, boolean saved) {
            this.id = id; this.term = term; this.topic = topic; this.definition = definition;
            this.example = example; this.source = source; this.saved = saved;
        }
        @Override public String toString() { return term + " (" + topic + ")"; }
    }

    public static final class Library {
        public final List<String> topics;
        public final List<Word> words;
        public Library(List<String> topics, List<Word> words) {
            this.topics = List.copyOf(topics); this.words = List.copyOf(words);
        }
    }

    public static final class Card {
        public final Word word;
        public final String counter;
        public final boolean revealed;
        public final boolean studied;
        public Card(Word word, String counter, boolean revealed, boolean studied) {
            this.word = word; this.counter = counter; this.revealed = revealed; this.studied = studied;
        }
    }

    public static final class Quiz {
        public final String wordId;
        public final String prompt;
        public final List<String> options;
        public final String counter;
        public final String feedback;
        public final boolean answered;
        public final boolean finished;
        public Quiz(String wordId, String prompt, List<String> options, String counter,
                    String feedback, boolean answered, boolean finished) {
            this.wordId = wordId; this.prompt = prompt; this.options = List.copyOf(options);
            this.counter = counter; this.feedback = feedback; this.answered = answered; this.finished = finished;
        }
    }

    public static final class Dashboard {
        public final int studied;
        public final int totalWords;
        public final int saved;
        public final int quizzes;
        public final int accuracy;
        public final int streak;
        public final List<String[]> dailyRows;
        public final List<String[]> quizRows;
        public Dashboard(int studied, int totalWords, int saved, int quizzes, int accuracy,
                         int streak, List<String[]> dailyRows, List<String[]> quizRows) {
            this.studied = studied; this.totalWords = totalWords; this.saved = saved;
            this.quizzes = quizzes; this.accuracy = accuracy; this.streak = streak;
            this.dailyRows = List.copyOf(dailyRows); this.quizRows = List.copyOf(quizRows);
        }
    }

    public static final class Definition {
        public final String term;
        public final String definition;
        public final String example;
        public final String source;
        public Definition(String term, String definition, String example, String source) {
            this.term = term; this.definition = definition; this.example = example; this.source = source;
        }
    }
}
