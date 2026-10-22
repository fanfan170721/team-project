package com.wordtrail.usecase.port;

import com.wordtrail.entity.LearningProgress;

public interface ProgressRepository {
    LearningProgress getProgress(String username);
    void saveProgress(String username, LearningProgress progress);
}
