package com.wordtrail.usecase.port;

import com.wordtrail.usecase.ExportResultsRow;
import java.util.List;

/** Writes exported quiz results somewhere outside the core (e.g. a CSV file); implemented in an outer layer. */
public interface QuizResultsExporter {
    /**
     * Writes all rows, in the given order, to the destination. An empty list is still a valid export.
     * Implementations throw a RuntimeException (preferably AppException) when the export cannot complete.
     */
    void export(String destination, List<ExportResultsRow> rows);
}
