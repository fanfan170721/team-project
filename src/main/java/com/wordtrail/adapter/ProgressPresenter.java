package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.usecase.ProgressUseCase;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Produces the chart, dated quiz rows and weighted accuracy for the progress view. */
public final class ProgressPresenter {
    private final DateTimeFormatter completionTime;

    public ProgressPresenter(ZoneId zone) {
        completionTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone);
    }

    public ViewModels.Dashboard dashboard(ProgressUseCase.Report report) {
        LearningProgress data = report.progress;
        int correct = data.getAttempts().stream().mapToInt(QuizAttempt::getCorrect).sum();
        int total = data.getAttempts().stream().mapToInt(QuizAttempt::getTotal).sum();
        List<String[]> daily = new ArrayList<>();
        for (int offset = 13; offset >= 0; offset--) {
            LocalDate date = report.today.minusDays(offset);
            daily.add(new String[]{date.toString(),
                    Integer.toString(data.getStudyDays().getOrDefault(date, Collections.emptySet()).size())});
        }
        List<QuizAttempt> attempts = new ArrayList<>(data.getAttempts());
        Collections.reverse(attempts);
        attempts.sort(Comparator.comparing(QuizAttempt::getCompletedAt).reversed());
        List<String[]> rows = new ArrayList<>();
        for (QuizAttempt attempt : attempts) {
            rows.add(new String[]{completionTime.format(attempt.getCompletedAt()), attempt.getTopic(),
                    attempt.getCorrect() + " / " + attempt.getTotal(),
                    Math.round(100.0 * attempt.getCorrect() / attempt.getTotal()) + "%"});
        }
        return new ViewModels.Dashboard(data.getStudiedWordIds().size(), report.totalWords,
                data.getReviewWordIds().size(), attempts.size(), total == 0 ? 0 : (int) Math.round(100.0 * correct / total),
                report.studyStreak, data.getReviewCounts().values().stream().mapToInt(Integer::intValue).sum(), daily, rows);
    }
}
