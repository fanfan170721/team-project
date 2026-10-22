package com.wordtrail.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable snapshot. Transformations return copies so failed writes cannot leak state. */
public final class LearningProgress {
    private final Set<String> reviewWordIds;
    private final Map<LocalDate, Set<String>> studyDays;
    private final List<QuizAttempt> attempts;

    public LearningProgress(Set<String> reviewWordIds, Map<LocalDate, Set<String>> studyDays,
                            List<QuizAttempt> attempts) {
        this.reviewWordIds = Collections.unmodifiableSet(new LinkedHashSet<>(reviewWordIds));
        Map<LocalDate, Set<String>> days = new LinkedHashMap<>();
        studyDays.forEach((date, ids) -> days.put(date,
                Collections.unmodifiableSet(new LinkedHashSet<>(ids))));
        this.studyDays = Collections.unmodifiableMap(days);
        this.attempts = Collections.unmodifiableList(new ArrayList<>(attempts));
    }

    public static LearningProgress empty() {
        return new LearningProgress(Collections.emptySet(), Collections.emptyMap(), Collections.emptyList());
    }

    public LearningProgress withReviewWord(String wordId, boolean saved) {
        Set<String> ids = new LinkedHashSet<>(reviewWordIds);
        if (saved) { ids.add(wordId); }
        else { ids.remove(wordId); }
        return new LearningProgress(ids, studyDays, attempts);
    }

    public LearningProgress withStudiedWord(String wordId, LocalDate date) {
        Map<LocalDate, Set<String>> days = new LinkedHashMap<>(studyDays);
        Set<String> ids = new LinkedHashSet<>(days.getOrDefault(date, Collections.emptySet()));
        ids.add(wordId);
        days.put(date, ids);
        return new LearningProgress(reviewWordIds, days, attempts);
    }

    public LearningProgress withAttempt(QuizAttempt attempt) {
        List<QuizAttempt> updated = new ArrayList<>(attempts);
        updated.add(attempt);
        return new LearningProgress(reviewWordIds, studyDays, updated);
    }

    public Set<String> getReviewWordIds() { return reviewWordIds; }
    public Map<LocalDate, Set<String>> getStudyDays() { return studyDays; }
    public List<QuizAttempt> getAttempts() { return attempts; }
    public Set<String> getStudiedWordIds() {
        Set<String> ids = new LinkedHashSet<>();
        studyDays.values().forEach(ids::addAll);
        return Collections.unmodifiableSet(ids);
    }
}
