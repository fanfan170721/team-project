package com.wordtrail.usecase.port;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.StudyState;

public interface StudyRepository {
    StudyState getStudy(String username);
    /** Persist both snapshots atomically; publish neither if writing fails. */
    void saveStudy(String username, StudyState state, LearningProgress progress);
}
