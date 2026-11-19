package com.wordtrail.usecase;

/** UC-09 entry point: export the signed-in learner's completed quiz results to a destination. */
public interface ExportResultsInputBoundary {
    /**
     * Exports the current account's completed quiz results.
     *
     * @param destination where the exporter should place the results, e.g. a chosen file path
     */
    void execute(String destination);
}
