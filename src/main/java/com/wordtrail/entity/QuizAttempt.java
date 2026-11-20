package com.wordtrail.entity;

import java.time.Instant;
import java.util.Objects;

/** A completed quiz. Abandoned quizzes are not included in progress. */
public final class QuizAttempt {
    private final Instant completedAt;
    private final String topic;
    private final int correct;
    private final int total;

    public QuizAttempt(Instant completedAt, String topic, int correct, int total) {
        if (total < 1 || correct < 0 || correct > total) {
            throw new IllegalArgumentException("Invalid quiz score.");
        }
        this.completedAt = Objects.requireNonNull(completedAt);
        this.topic = Objects.requireNonNull(topic);
        this.correct = correct;
        this.total = total;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
    public String getTopic() {
        return topic;
    }
    public int getCorrect() {
        return correct;
    }
    public int getTotal() {
        return total;
    }
}
