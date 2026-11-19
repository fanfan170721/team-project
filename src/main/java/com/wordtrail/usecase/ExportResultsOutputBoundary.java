package com.wordtrail.usecase;

/** UC-09 results; exactly one method is called per export request. */
public interface ExportResultsOutputBoundary {
    void prepareSuccessView(ExportResultsResponse response);

    void prepareFailView(String errorMessage);
}
