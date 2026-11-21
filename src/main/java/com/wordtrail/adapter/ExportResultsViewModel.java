package com.wordtrail.adapter;

/** One synchronous UC-09 export result; the controller clears it before each request. */
public final class ExportResultsViewModel {
    private Result result;

    void beginRequest() {
        result = null;
    }

    void complete(Result completed) {
        result = completed;
    }

    Result result() {
        if (result == null) {
            throw new IllegalStateException("The export request did not produce a result.");
        }
        return result;
    }

    /** Display data only: no entity, use-case or Swing object crosses this boundary. */
    public static final class Result {
        public final boolean success;
        public final String destination;
        public final int exportedCount;
        public final String message;

        Result(boolean success, String destination, int exportedCount, String message) {
            this.success = success;
            this.destination = destination;
            this.exportedCount = exportedCount;
            this.message = message;
        }
    }
}
