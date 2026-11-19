package com.wordtrail.usecase;

/** Successful UC-09 outcome: where the results went and how many were exported. */
public final class ExportResultsResponse {
    private final String destination;
    private final int exportedCount;

    public ExportResultsResponse(String destination, int exportedCount) {
        this.destination = destination;
        this.exportedCount = exportedCount;
    }

    public String getDestination() { return destination; }
    public int getExportedCount() { return exportedCount; }
}
