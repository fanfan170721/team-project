package com.wordtrail.entity;

import java.time.LocalDate;
import java.time.Instant;
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
    private final List<VocabularyWord> personalWords;
    private final Map<String, String> definitions;
    private final Map<String, Integer> reviewCounts;
    private final Map<String, Instant> lastReviewed;

    public LearningProgress(Set<String> reviewWordIds, Map<LocalDate, Set<String>> studyDays,
                            List<QuizAttempt> attempts) {
        this(reviewWordIds, studyDays, attempts, Collections.emptyList(), Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap());
    }

    public LearningProgress(Set<String> reviewWordIds, Map<LocalDate, Set<String>> studyDays,
                            List<QuizAttempt> attempts, List<VocabularyWord> personalWords,
                            Map<String, String> definitions, Map<String, Integer> reviewCounts,
                            Map<String, Instant> lastReviewed) {
        this.reviewWordIds = Collections.unmodifiableSet(new LinkedHashSet<>(reviewWordIds));
        Map<LocalDate, Set<String>> days = new LinkedHashMap<>();
        studyDays.forEach((date, ids) -> days.put(date,
                Collections.unmodifiableSet(new LinkedHashSet<>(ids))));
        this.studyDays = Collections.unmodifiableMap(days);
        this.attempts = Collections.unmodifiableList(new ArrayList<>(attempts));
        this.personalWords = Collections.unmodifiableList(new ArrayList<>(personalWords));
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
        this.reviewCounts = Collections.unmodifiableMap(new LinkedHashMap<>(reviewCounts));
        this.lastReviewed = Collections.unmodifiableMap(new LinkedHashMap<>(lastReviewed));
    }

    public static LearningProgress empty() {
        return new LearningProgress(Collections.emptySet(), Collections.emptyMap(), Collections.emptyList());
    }

    public LearningProgress withReviewWord(String wordId, boolean saved) {
        Set<String> ids = new LinkedHashSet<>(reviewWordIds);
        if (saved) {
            ids.add(wordId);
        } else {
            ids.remove(wordId);
        }
        return new LearningProgress(ids, studyDays, attempts, personalWords, definitions, reviewCounts, lastReviewed);
    }

    public LearningProgress withStudiedWord(String wordId, LocalDate date) {
        Map<LocalDate, Set<String>> days = new LinkedHashMap<>(studyDays);
        Set<String> ids = new LinkedHashSet<>(days.getOrDefault(date, Collections.emptySet()));
        ids.add(wordId);
        days.put(date, ids);
        return new LearningProgress(reviewWordIds, days, attempts, personalWords,
                definitions, reviewCounts, lastReviewed);
    }

    public LearningProgress withAttempt(QuizAttempt attempt) {
        List<QuizAttempt> updated = new ArrayList<>(attempts);
        updated.add(attempt);
        return new LearningProgress(reviewWordIds, studyDays, updated, personalWords,
                definitions, reviewCounts, lastReviewed);
    }

    public LearningProgress withPersonalWord(VocabularyWord word) {
        List<VocabularyWord> words = new ArrayList<>(personalWords);
        words.add(word);
        return new LearningProgress(reviewWordIds, studyDays, attempts, words, definitions, reviewCounts, lastReviewed);
    }

    public LearningProgress withDefinition(String wordId, String definition) {
        Map<String, String> updated = new LinkedHashMap<>(definitions);
        updated.put(wordId, definition);
        return new LearningProgress(reviewWordIds, studyDays, attempts, personalWords,
                updated, reviewCounts, lastReviewed);
    }

    public LearningProgress withCompletedReview(String wordId, LocalDate day, Instant time) {
        Map<String, Integer> counts = new LinkedHashMap<>(reviewCounts);
        counts.put(wordId, Math.addExact(counts.getOrDefault(wordId, 0), 1));
        Map<String, Instant> times = new LinkedHashMap<>(lastReviewed);
        times.put(wordId, time);
        LearningProgress studied = withStudiedWord(wordId, day);
        return new LearningProgress(reviewWordIds, studied.studyDays, attempts, personalWords,
                definitions, counts, times);
    }

    public Set<String> getReviewWordIds() {
        return reviewWordIds;
    }
    public Map<LocalDate, Set<String>> getStudyDays() {
        return studyDays;
    }
    public List<QuizAttempt> getAttempts() {
        return attempts;
    }
    public List<VocabularyWord> getPersonalWords() {
        return personalWords;
    }
    public Map<String, String> getDefinitions() {
        return definitions;
    }
    public Map<String, Integer> getReviewCounts() {
        return reviewCounts;
    }
    public Map<String, Instant> getLastReviewed() {
        return lastReviewed;
    }
    public Set<String> getStudiedWordIds() {
        Set<String> ids = new LinkedHashSet<>();
        studyDays.values().forEach(ids::addAll);
        return Collections.unmodifiableSet(ids);
    }
}
