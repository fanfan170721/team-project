package com.wordtrail.usecase;

public interface ReviewPlanOutputBoundary {
    /** Presents the result, including a successful empty plan. */
    void presentPlan(ReviewPlanResponse response);

    /** Presents a user-facing explanation when the plan cannot be generated. */
    void presentFailure(String message);
}
