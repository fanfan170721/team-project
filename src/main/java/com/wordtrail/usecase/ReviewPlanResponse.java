package com.wordtrail.usecase;

import java.util.List;

/** An immutable list in the order the learner should review the words. */
public final class ReviewPlanResponse {
    public final List<ReviewPlanEntry> entries;

    public ReviewPlanResponse(List<ReviewPlanEntry> entries) {
        this.entries = List.copyOf(entries);
    }
}
