package com.wordtrail.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Detached persistence snapshot. Interactors copy it before making a durable transition. */
public final class StudyState {
    public int level = 1;
    public int goalTarget = 100;
    public int goalBaseline;
    public int learned;
    public int practiced;
    public final Set<String> mastered = new LinkedHashSet<>();
    public final Map<String, int[]> mistakes = new LinkedHashMap<>();
    public final List<Round> history = new ArrayList<>();
    public Round round;
    public Round assessment;

    public StudyState() { }

    public StudyState(StudyState source) {
        level = source.level;
        goalTarget = source.goalTarget;
        goalBaseline = source.goalBaseline;
        learned = source.learned;
        practiced = source.practiced;
        mastered.addAll(source.mastered);
        source.mistakes.forEach((id, counts) -> mistakes.put(id, counts.clone()));
        source.history.forEach(item -> history.add(new Round(item)));
        round = source.round == null ? null : new Round(source.round);
        assessment = source.assessment == null ? null : new Round(source.assessment);
    }

    /** Original questions and order survive changes to the current content bank. */
    public static final class Round {
        public String id = "";
        public String phase = "OVERVIEW";
        public String mode = "STUDY";
        public String completedAt = "";
        public String adopted = "";
        public int level = 1;
        public int position;
        public int pass = 1;
        public int firstCorrect;
        public int netNew;
        public final List<Question> questions = new ArrayList<>();
        public final List<Integer> pending = new ArrayList<>();
        public final List<Integer> wrong = new ArrayList<>();
        public final Set<Integer> known = new LinkedHashSet<>();
        public final List<int[]> answers = new ArrayList<>();

        public Round() { }

        public Round(Round source) {
            id = source.id;
            phase = source.phase;
            mode = source.mode;
            completedAt = source.completedAt;
            adopted = source.adopted;
            level = source.level;
            position = source.position;
            pass = source.pass;
            firstCorrect = source.firstCorrect;
            netNew = source.netNew;
            questions.addAll(source.questions);
            pending.addAll(source.pending);
            wrong.addAll(source.wrong);
            known.addAll(source.known);
            source.answers.forEach(answer -> answers.add(answer.clone()));
        }

        public String token() {
            return id + ":" + phase + ":" + pass + ":" + position;
        }
    }

    public static final class Question {
        public final String id;
        public final String term;
        public final String definition;
        public final int level;
        public final List<String> options;

        public Question(String id, String term, String definition, int level, List<String> options) {
            if (id.isBlank() || term.isBlank() || definition.isBlank() || level < 1 || level > 3
                    || options.size() != 4 || new LinkedHashSet<>(options).size() != 4
                    || !options.contains(definition)) {
                throw new IllegalArgumentException("Invalid study question.");
            }
            this.id = id;
            this.term = term;
            this.definition = definition;
            this.level = level;
            this.options = List.copyOf(options);
        }
    }
}
