package com.wordtrail.adapter;

import java.util.List;

/** Holds the display result for one synchronous review plan request. */
public final class ReviewPlanViewModel {
    private Result result;

    void beginRequest() {
        result = null;
    }

    void complete(Result completed) {
        result = completed;
    }

    Result result() {
        if (result == null) {
            throw new IllegalStateException("The review plan request did not produce a result.");
        }
        return result;
    }

    /** An immutable snapshot in the order in which the words should be reviewed. */
    public static final class Result {
        public final boolean success;
        public final List<Row> rows;
        public final String message;

        Result(boolean success, List<Row> rows, String message) {
            this.success = success;
            this.rows = List.copyOf(rows);
            this.message = message;
        }
    }

    /** Display values for a scheduled word. */
    public static final class Row {
        public final String wordId;
        public final String term;
        public final String definition;
        public final String dueDate;
        public final String reason;

        Row(String wordId, String term, String definition, String dueDate, String reason) {
            this.wordId = wordId;
            this.term = term;
            this.definition = definition;
            this.dueDate = dueDate;
            this.reason = reason;
        }
    }
}
