package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.LocalDate;

/** US-06: progress is derived from persisted study dates and completed quizzes. */
public final class ProgressInteractor implements ProgressUseCase {
    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;
    private final Clock clock;

    public ProgressInteractor(ContentRepository content, ProgressRepository progress, SessionContext session, Clock clock) {
        this.content = content;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
    }

    @Override public Report report() {
        LearningProgress data = progress.getProgress(session.requireUser().getUsername());
        LocalDate today = LocalDate.now(clock);
        LocalDate day = data.getStudyDays().containsKey(today) ? today : today.minusDays(1);
        int streak = 0;
        while (data.getStudyDays().containsKey(day) && !data.getStudyDays().get(day).isEmpty()) {
            streak++;
            day = day.minusDays(1);
        }
        return new Report(data, content.getWords().size(), streak, today);
    }
}
