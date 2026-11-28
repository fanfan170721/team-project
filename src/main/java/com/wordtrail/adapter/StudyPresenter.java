package com.wordtrail.adapter;

import com.wordtrail.entity.StudyState;
import com.wordtrail.entity.StudyState.Question;
import com.wordtrail.entity.StudyState.Round;
import com.wordtrail.usecase.StudyOutputBoundary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StudyPresenter implements StudyOutputBoundary {
    private StudyViewModel result;

    @Override public void present(StudyState state, String feedback) {
        StudyViewModel view = new StudyViewModel();
        view.summary = "Level L" + state.level + "  |  Mastered: " + state.mastered.size()
                + "  |  Net-new learned: " + state.learned + "  |  Practice words: " + state.practiced
                + "\nNew-word goal: " + (state.learned - state.goalBaseline) + " / " + state.goalTarget;
        view.goal = state.goalTarget;
        view.feedback = feedback;
        view.round = render(state.round);
        view.assessment = render(state.assessment);
        view.history = new ArrayList<>();
        Map<String, Question> words = new LinkedHashMap<>();
        for (Round round : state.history) {
            round.questions.forEach(q -> words.put(q.id, q));
            view.history.add(new StudyViewModel.HistoryRow(round.id,
                    new Object[]{round.completedAt, round.mode, round.phase, "L" + round.level,
                            round.firstCorrect, round.netNew}, round.phase.equals("COMPLETED")));
        }
        if (state.round != null) { state.round.questions.forEach(q -> words.put(q.id, q)); }
        StringBuilder mistakes = new StringBuilder();
        state.mistakes.forEach((id, counts) -> {
            Question word = words.get(id);
            mistakes.append(word == null ? id : word.term + " — " + word.definition)
                    .append("\nIncorrect: ").append(counts[0]).append(" | Unknown: ").append(counts[1])
                    .append(" | Retests: ").append(counts[2]).append("\n\n");
        });
        view.mistakes = mistakes.length() == 0 ? "No mistakes recorded yet." : mistakes.toString();
        result = view;
    }

    private StudyViewModel.RoundView render(Round round) {
        if (round == null) { return null; }
        StudyViewModel.RoundView view = new StudyViewModel.RoundView();
        view.token = round.token();
        view.phase = round.phase;
        view.mode = round.mode;
        view.adopted = !round.adopted.isEmpty();
        String mode = round.mode.equals("ASSESSMENT") ? "Placement"
                : round.mode.equals("REPLAY") ? "History replay" : "Guided round";
        String phase = Map.of("OVERVIEW", "Overview", "LEARN", "Learn", "TEST", "Test",
                "REVIEW", "Review mistakes", "COMPLETED", "Complete", "SUPERSEDED", "Replaced").get(round.phase);
        view.heading = mode + " · Target L" + round.level + " · " + phase + " · Pass " + round.pass
                + " · " + Math.min(round.position + 1, round.pending.size()) + " / " + round.pending.size();
        view.result = round.mode.equals("ASSESSMENT") ? "Score: " + round.firstCorrect
                + " / 20. Recommended sample level: L" + round.level
                + ".\nThis is a small sample placement, not a standardized language test."
                : "Round complete. New words learned: " + round.netNew
                + ".\nAll 20 words are mastered; earlier mistakes remain in your notebook.";
        view.options = Collections.emptyList();
        view.word = "";
        view.definition = "";
        if (round.position < round.pending.size()) {
            Question word = round.questions.get(round.pending.get(round.position));
            view.word = word.term;
            view.definition = word.definition;
            view.options = word.options;
        }
        view.overview = new ArrayList<>();
        for (int i = 0; i < round.questions.size(); i++) {
            Question q = round.questions.get(i);
            view.overview.add(new Object[]{round.known.contains(i), q.term, q.definition});
        }
        return view;
    }

    public StudyViewModel result() { return result; }
}
