package com.wordtrail.usecase;

public interface ReviewPlanInputBoundary {
    /** Generates a plan for the topic (or all topics if blank or null) with a limit of 1 to 20. */
    void execute(String topic, int limit);
}
