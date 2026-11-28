package com.wordtrail.usecase;

import com.wordtrail.entity.StudyState;

public interface StudyOutputBoundary {
    void present(StudyState state, String feedback);
}
