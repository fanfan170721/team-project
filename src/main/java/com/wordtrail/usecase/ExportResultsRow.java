package com.wordtrail.usecase;

/** One completed quiz as plain data for an exporter; the time is ISO-8601 UTC, e.g. 2026-10-22T16:00:00Z. */
public final class ExportResultsRow {
    private final String completedAtUtc;
    private final String topic;
    private final int correct;
    private final int total;

    public ExportResultsRow(String completedAtUtc, String topic, int correct, int total) {
        this.completedAtUtc = completedAtUtc;
        this.topic = topic;
        this.correct = correct;
        this.total = total;
    }

    public String getCompletedAtUtc() { return completedAtUtc; }
    public String getTopic() { return topic; }
    public int getCorrect() { return correct; }
    public int getTotal() { return total; }
}
