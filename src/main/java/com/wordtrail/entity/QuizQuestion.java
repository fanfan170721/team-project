package com.wordtrail.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** A question is linked to a word so it can be selected by topic. */
public final class QuizQuestion {
    private final String id;
    private final String wordId;
    private final String prompt;
    private final List<String> options;
    private final int correctIndex;

    public QuizQuestion(String id, String wordId, String prompt, List<String> options, int correctIndex) {
        if (options.size() != 4 || new HashSet<>(options).size() != 4
                || options.stream().anyMatch(s -> s == null || s.isBlank())
                || correctIndex < 0 || correctIndex >= 4 || prompt.isBlank()) {
            throw new IllegalArgumentException("A question needs four distinct options and one correct answer.");
        }
        this.id = Objects.requireNonNull(id);
        this.wordId = Objects.requireNonNull(wordId);
        this.prompt = prompt;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.correctIndex = correctIndex;
    }

    public String getId() { return id; }
    public String getWordId() { return wordId; }
    public String getPrompt() { return prompt; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}
