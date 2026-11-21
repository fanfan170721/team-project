package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.ExportResultsRow;
import com.wordtrail.usecase.port.QuizResultsExporter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Writes UC-09 quiz results as an RFC 4180 style UTF-8 CSV file.
 *
 * <p>The file is first written completely to a temporary file in the destination's directory and then
 * atomically moved over the destination, so a failed export never leaves a partial or corrupted file
 * where the learner expects their results. If the file system cannot replace atomically, the export
 * fails instead of falling back to a direct overwrite.
 */
public final class CsvQuizResultsExporter implements QuizResultsExporter {
    static final String HEADER = "completed_at_utc,topic,correct,total";
    static final String LINE_END = "\r\n";
    static final String TEMP_PREFIX = ".wordtrail-export-";
    static final String TEMP_SUFFIX = ".tmp";

    /** Seam for the final replace step so tests can simulate move failures on any operating system. */
    interface FileMover {
        void move(Path source, Path target) throws IOException;
    }

    private final FileMover mover;

    public CsvQuizResultsExporter() {
        this((source, target) -> Files.move(source, target,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    CsvQuizResultsExporter(FileMover mover) {
        this.mover = mover;
    }

    @Override public void export(String destination, List<ExportResultsRow> rows) {
        if (destination == null || destination.isBlank()) {
            throw new AppException("Please choose where to save the exported results.");
        }
        if (rows == null) {
            throw new AppException("There are no results to export.");
        }
        Path target;
        try {
            target = Path.of(destination).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new AppException("The chosen location is not a valid file path.", e);
        }
        String csv = toCsv(rows);
        Path temp = null;
        try {
            temp = Files.createTempFile(target.getParent(), TEMP_PREFIX, TEMP_SUFFIX);
            Files.writeString(temp, csv, StandardCharsets.UTF_8); // opens, writes and closes the file
            mover.move(temp, target);
        } catch (AtomicMoveNotSupportedException e) {
            throw new AppException("This location cannot replace files safely, so nothing was exported. "
                    + "Any existing file was left unchanged.", e);
        } catch (IOException e) {
            throw new AppException("Could not save the results file. Any existing file was left unchanged.", e);
        } finally {
            deleteQuietly(temp);
        }
    }

    static String toCsv(List<ExportResultsRow> rows) {
        StringBuilder csv = new StringBuilder(HEADER).append(LINE_END);
        for (ExportResultsRow row : rows) {
            csv.append(field(row.getCompletedAtUtc())).append(',')
                    .append(field(row.getTopic())).append(',')
                    .append(row.getCorrect()).append(',')
                    .append(row.getTotal()).append(LINE_END);
        }
        return csv.toString();
    }

    /** Quotes a field only when needed; embedded double quotes are doubled. */
    static String field(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static void deleteQuietly(Path temp) {
        if (temp == null) { return; }
        try {
            Files.deleteIfExists(temp); // already gone after a successful move
        } catch (IOException ignored) {
            // The export outcome is already decided; a leftover hidden temp file is harmless.
        }
    }
}
