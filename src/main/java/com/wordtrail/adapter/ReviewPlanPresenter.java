package com.wordtrail.adapter;

import com.wordtrail.usecase.ReviewPlanEntry;
import com.wordtrail.usecase.ReviewPlanOutputBoundary;
import com.wordtrail.usecase.ReviewPlanResponse;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Converts the use case's ordered plan into display values. */
public final class ReviewPlanPresenter implements ReviewPlanOutputBoundary {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    private final ReviewPlanViewModel viewModel;

    public ReviewPlanPresenter(ReviewPlanViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentPlan(ReviewPlanResponse response) {
        List<ReviewPlanViewModel.Row> rows = new ArrayList<>(response.entries.size());
        for (ReviewPlanEntry entry : response.entries) {
            rows.add(new ReviewPlanViewModel.Row(entry.wordId, entry.term, entry.definition,
                    DATE.format(entry.dueDate), entry.reason));
        }
        viewModel.complete(new ReviewPlanViewModel.Result(true, rows, successMessage(rows.size())));
    }

    @Override
    public void presentFailure(String message) {
        viewModel.complete(new ReviewPlanViewModel.Result(false, List.of(), message));
    }

    private static String successMessage(int count) {
        if (count == 0) {
            return "No due words match these filters.";
        }
        if (count == 1) {
            return "1 word is due for review.";
        }
        return count + " words are due for review.";
    }
}
