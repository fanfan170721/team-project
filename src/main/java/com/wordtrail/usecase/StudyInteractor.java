package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.entity.StudyState;
import com.wordtrail.entity.StudyState.Question;
import com.wordtrail.entity.StudyState.Round;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ProgressRepository;
import com.wordtrail.usecase.port.StudyRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Durable 20-word mastery loop. Quiz scores and progress are committed with completion. */
public final class StudyInteractor implements StudyInputBoundary {
    private final List<Question> catalog;
    private final StudyRepository repository;
    private final ProgressRepository progress;
    private final SessionContext session;
    private final Clock clock;
    private final Random random;
    private final StudyOutputBoundary output;

    public StudyInteractor(List<Question> catalog, StudyRepository repository, ProgressRepository progress,
                           SessionContext session, Clock clock, Random random, StudyOutputBoundary output) {
        if (catalog.size() < 20) {
            throw new IllegalArgumentException("Guided study requires at least 20 words.");
        }
        this.catalog = List.copyOf(catalog);
        this.repository = repository;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
        this.random = random;
        this.output = output;
    }

    private String user() {
        return session.requireUser().getUsername();
    }

    private StudyState load() {
        return new StudyState(repository.getStudy(user()));
    }

    private void save(StudyState state, String message) {
        save(state, progress.getProgress(user()), message);
    }

    private void save(StudyState state, LearningProgress learning, String message) {
        repository.saveStudy(user(), state, learning);
        output.present(new StudyState(state), message);
    }

    @Override public void show() {
        output.present(load(), "");
    }

    @Override public void start() {
        StudyState state = load();
        if (state.round == null || state.round.phase.equals("COMPLETED")) {
            state.round = create(state, false, false);
            save(state, "Review the 20 words and mark only those you already know.");
        } else {
            output.present(state, "Your saved round is ready to continue.");
        }
    }

    private Round create(StudyState state, boolean assessment, boolean placementRefresh) {
        List<Question> available = new ArrayList<>(catalog);
        Collections.shuffle(available, random);
        if (!assessment) {
            Comparator<Question> distance = Comparator.comparingInt(q -> Math.abs(q.level - state.level));
            Comparator<Question> mastered = Comparator.comparingInt(q -> state.mastered.contains(q.id) ? 1 : 0);
            available.sort(placementRefresh ? distance.thenComparing(mastered) : mastered.thenComparing(distance));
        }
        Round round = new Round();
        round.id = UUID.randomUUID().toString();
        round.level = state.level;
        round.mode = assessment ? "ASSESSMENT" : "STUDY";
        round.phase = assessment ? "TEST" : "OVERVIEW";
        // Placement cycles across the sample levels instead of relying on a random imbalance.
        List<Question> chosen = new ArrayList<>();
        for (int index = 0; chosen.size() < 20; index++) {
            int wantedLevel = index % 3 + 1;
            Question word = assessment ? available.stream().filter(q -> q.level == wantedLevel)
                    .findFirst().orElse(available.get(0)) : available.get(0);
            available.remove(word);
            chosen.add(word);
        }
        LearningProgress learning = progress.getProgress(user());
        for (Question word : chosen) {
            String definition = learning.getDefinitions().getOrDefault("guided-" + word.id, word.definition);
            List<String> options = new ArrayList<>();
            options.add(definition);
            // Keep distractors inside this round's original pool, including single-word retries.
            for (Question other : chosen) {
                String alternative = learning.getDefinitions().getOrDefault("guided-" + other.id, other.definition);
                if (options.stream().noneMatch(option -> option.equalsIgnoreCase(alternative)) && options.size() < 4) {
                    options.add(alternative);
                }
            }
            if (options.size() != 4) {
                throw new AppException("This round needs at least four distinct definitions.");
            }
            Collections.shuffle(options, random);
            round.questions.add(new Question(word.id, word.term, definition, word.level, options));
            round.pending.add(round.pending.size());
        }
        return round;
    }

    private Round checked(Round round, String token) {
        if (round == null || !round.token().equals(token)) {
            throw new AppException("This screen is out of date. Refresh to continue the saved round.");
        }
        return round;
    }

    @Override public void known(String token, int index, boolean value) {
        StudyState state = load();
        Round round = checked(state.round, token);
        if (!round.phase.equals("OVERVIEW") || index < 0 || index >= 20) {
            throw new AppException("Mark known words in the round overview.");
        }
        if (value) {
            round.known.add(index);
        } else {
            round.known.remove(index);
        }
        save(state, "Known-word selection saved.");
    }

    @Override public void advance(String token) {
        StudyState state = load();
        Round round = checked(state.round, token);
        if (round.phase.equals("OVERVIEW")) {
            round.pending.removeAll(round.known);
            round.phase = "LEARN";
        } else if (round.phase.equals("LEARN") || round.phase.equals("REVIEW")) {
            round.position++;
            if (round.position == round.pending.size()) {
                round.position = 0;
                round.phase = "TEST";
            }
        } else {
            throw new AppException("Choose an answer before continuing.");
        }
        if (round.pending.isEmpty()) {
            finish(state);
        } else {
            save(state, "Learning step saved.");
        }
    }

    @Override public void answer(String token, int choice, boolean assessment) {
        StudyState state = load();
        Round round = checked(assessment ? state.assessment : state.round, token);
        if (!round.phase.equals("TEST") || choice < 0 || choice > 4) {
            throw new AppException("Choose one answer or I don't know.");
        }
        int index = round.pending.get(round.position);
        Question question = round.questions.get(index);
        boolean correct = choice < 4 && question.options.get(choice).equals(question.definition);
        round.answers.add(new int[]{round.pass, index, choice});
        if (correct && round.pass == 1) {
            round.firstCorrect++;
        }
        if (!correct && !assessment) {
            round.wrong.add(index);
            int[] counts = state.mistakes.computeIfAbsent(question.id, key -> new int[3]);
            counts[choice == 4 ? 1 : 0]++;
        }
        if (round.pass > 1 && !assessment) {
            state.mistakes.get(question.id)[2]++;
        }
        round.position++;
        String message = correct ? "Correct." : "Answer: " + question.definition;
        if (round.position < round.pending.size()) {
            save(state, message);
        } else if (assessment) {
            round.phase = "COMPLETED";
            round.level = Math.min(3, 1 + round.firstCorrect * 3 / 20);
            round.completedAt = Instant.now(clock).toString();
            save(state, "Assessment complete. Recommended sample level: L" + round.level + ".");
        } else if (round.wrong.isEmpty()) {
            finish(state);
        } else {
            round.pending.clear();
            round.pending.addAll(round.wrong);
            round.wrong.clear();
            round.position = 0;
            round.pass++;
            round.phase = "REVIEW";
            save(state, message + " Relearn and retest only the words you missed.");
        }
    }

    private void finish(StudyState state) {
        Round round = state.round;
        LearningProgress learning = progress.getProgress(user());
        if (!round.mode.equals("REPLAY")) {
            for (int index = 0; index < round.questions.size(); index++) {
                Question word = round.questions.get(index);
                if (state.mastered.add(word.id) && !round.known.contains(index)) {
                    round.netNew++;
                }
                String id = "guided-" + word.id;
                if (learning.getPersonalWords().stream().noneMatch(existing -> existing.getId().equals(id))) {
                    learning = learning.withPersonalWord(new VocabularyWord(id, word.term, "Level " + word.level,
                            word.definition, "", "Java reference sample curriculum"));
                }
                if (!round.known.contains(index)) {
                    learning = learning.withCompletedReview(id, LocalDate.now(clock), Instant.now(clock));
                }
                if (state.mistakes.containsKey(word.id)) {
                    learning = learning.withReviewWord(id, true);
                }
            }
            state.learned += round.netNew;
            state.practiced += 20;
            learning = learning.withAttempt(new QuizAttempt(Instant.now(clock), "Guided study L" + round.level,
                    round.firstCorrect + round.known.size(), 20));
        }
        round.phase = "COMPLETED";
        round.completedAt = Instant.now(clock).toString();
        state.history.add(new Round(round));
        save(state, learning, "Round completed. Net-new words: " + round.netNew + ".");
    }

    @Override public void assess() {
        StudyState state = load();
        if (state.assessment == null || !state.assessment.adopted.isEmpty()) {
            state.assessment = create(state, true, false);
            save(state, "Sample-level placement: 20 questions. Your study round remains saved.");
        } else {
            output.present(state, "Continue your saved assessment or adopt its completed result.");
        }
    }

    @Override public void adopt(String token) {
        StudyState state = load();
        Round assessment = checked(state.assessment, token);
        if (!assessment.phase.equals("COMPLETED")) {
            throw new AppException("Finish the assessment before adopting its result.");
        }
        if (assessment.adopted.isEmpty()) {
            if (state.round != null && !state.round.phase.equals("COMPLETED")) {
                state.round.phase = "SUPERSEDED";
                state.history.add(new Round(state.round));
            }
            state.level = assessment.level;
            state.round = create(state, false, true);
            assessment.adopted = state.round.id;
            save(state, "Level applied. A fresh 20-word round is ready; unfinished work earned no credit.");
        } else {
            output.present(state, "This result was already applied. Your current round is unchanged.");
        }
    }

    @Override public void goal(int target) {
        if (target < 1 || target > 1000000) {
            throw new AppException("Choose a new-word goal from 1 to 1000000.");
        }
        StudyState state = load();
        state.goalTarget = target;
        state.goalBaseline = state.learned;
        save(state, "Goal starts from today's net-new count.");
    }

    @Override public void replay(String id) {
        StudyState state = load();
        if (state.round != null && !state.round.phase.equals("COMPLETED")) {
            throw new AppException("Finish the active round before replaying history.");
        }
        Round previous = state.history.stream().filter(item -> item.id.equals(id)
                && item.phase.equals("COMPLETED")).findFirst()
                .orElseThrow(() -> new AppException("Choose a completed round to replay."));
        Round round = new Round();
        round.id = UUID.randomUUID().toString();
        round.mode = "REPLAY";
        round.level = previous.level;
        round.questions.addAll(previous.questions);
        for (int index = 0; index < round.questions.size(); index++) {
            round.pending.add(index);
        }
        state.round = round;
        save(state, "Replaying saved questions. This does not increase mastery, goals or practice totals.");
    }
}
