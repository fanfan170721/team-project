package com.wordtrail.adapter;

import com.wordtrail.usecase.ExportResultsOutputBoundary;
import com.wordtrail.usecase.ExportResultsResponse;

/** Turns UC-09 outcomes into the messages shown on the Progress screen. */
public final class ExportResultsPresenter implements ExportResultsOutputBoundary {
    private final ExportResultsViewModel viewModel;

    public ExportResultsPresenter(ExportResultsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(ExportResultsResponse response) {
        int count = response.getExportedCount();
        String message = "Exported " + count + (count == 1 ? " quiz result" : " quiz results")
                + " to " + response.getDestination() + ".";
        if (count == 0) {
            message += " The file contains only the header row.";
        }
        viewModel.complete(new ExportResultsViewModel.Result(true, response.getDestination(), count, message));
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.complete(new ExportResultsViewModel.Result(false, "", 0, errorMessage));
    }
}
