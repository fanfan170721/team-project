package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Formats this account's review metadata for vocabulary screens after edits. */
public final class PersonalVocabularyPresenter {
    private final DateTimeFormatter reviewTime;

    public PersonalVocabularyPresenter(ZoneId zone) {
        reviewTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zone);
    }

    public ViewModels.VocabularyProgress progress(LearningProgress progress) {
        Map<String, String> lastReviewed = new LinkedHashMap<>();
        progress.getLastReviewed().forEach((id, instant) -> lastReviewed.put(id, reviewTime.format(instant)));
        return new ViewModels.VocabularyProgress(progress.getReviewWordIds(), progress.getReviewCounts(), lastReviewed);
    }
}
