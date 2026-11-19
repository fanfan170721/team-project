package com.wordtrail.usecase;

import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.usecase.port.ProgressRepository;
import com.wordtrail.usecase.port.QuizResultsExporter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** UC-09: export only the signed-in learner's completed quizzes, oldest first. Progress is only read. */
public final class ExportResultsInteractor implements ExportResultsInputBoundary {
    private final ProgressRepository progress;
    private final QuizResultsExporter exporter;
    private final SessionContext session;
    private final ExportResultsOutputBoundary output;

    public ExportResultsInteractor(ProgressRepository progress, QuizResultsExporter exporter,
                                   SessionContext session, ExportResultsOutputBoundary output) {
        this.progress = progress;
        this.exporter = exporter;
        this.session = session;
        this.output = output;
    }

    @Override public void execute(String destination) {
        String username;
        try {
            username = session.requireUser().getUsername();
        } catch (AppException e) {
            output.prepareFailView(e.getMessage());
            return;
        }
        if (destination == null || destination.isBlank()) {
            output.prepareFailView("Please choose where to save the exported results.");
            return;
        }
        List<QuizAttempt> attempts;
        try {
            attempts = new ArrayList<>(progress.getProgress(username).getAttempts());
        } catch (RuntimeException e) {
            output.prepareFailView("Could not read your quiz results. Nothing was exported.");
            return;
        }
        attempts.sort(Comparator.comparing(QuizAttempt::getCompletedAt));
        List<ExportResultsRow> rows = new ArrayList<>();
        for (QuizAttempt attempt : attempts) {
            rows.add(new ExportResultsRow(DateTimeFormatter.ISO_INSTANT.format(attempt.getCompletedAt()),
                    attempt.getTopic(), attempt.getCorrect(), attempt.getTotal()));
        }
        try {
            exporter.export(destination, rows);
        } catch (RuntimeException e) {
            output.prepareFailView("Export failed: " + e.getMessage());
            return;
        }
        output.prepareSuccessView(new ExportResultsResponse(destination, rows.size()));
    }
}
