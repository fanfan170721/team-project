package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;

public interface ProgressUseCase {
    Report report();

    final class Report {
        public final LearningProgress progress;
        public final int totalWords;
        public final int studyStreak;
        public final java.time.LocalDate today;

        public Report(LearningProgress progress, int totalWords, int studyStreak, java.time.LocalDate today) {
            this.progress = progress;
            this.totalWords = totalWords;
            this.studyStreak = studyStreak;
            this.today = today;
        }
    }
}
