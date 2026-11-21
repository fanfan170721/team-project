package com.wordtrail.adapter;

import com.wordtrail.usecase.ExportResultsInputBoundary;

/** UC-09 controller: forwards the chosen destination and returns the presented outcome. */
public final class ExportResultsController {
    private final ExportResultsInputBoundary interactor;
    private final ExportResultsViewModel viewModel;

    public ExportResultsController(ExportResultsInputBoundary interactor, ExportResultsViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public synchronized ExportResultsViewModel.Result export(String destination) {
        viewModel.beginRequest();
        interactor.execute(destination);
        return viewModel.result();
    }
}
