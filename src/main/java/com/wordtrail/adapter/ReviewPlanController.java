package com.wordtrail.adapter;

import com.wordtrail.usecase.ReviewPlanInputBoundary;

/** Runs one synchronous review plan request and returns its display result. */
public final class ReviewPlanController {
    private final ReviewPlanInputBoundary input;
    private final ReviewPlanViewModel viewModel;

    public ReviewPlanController(ReviewPlanInputBoundary input, ReviewPlanViewModel viewModel) {
        this.input = input;
        this.viewModel = viewModel;
    }

    public synchronized ReviewPlanViewModel.Result generate(String topic, int limit) {
        viewModel.beginRequest();
        input.execute(topic, limit);
        return viewModel.result();
    }
}
