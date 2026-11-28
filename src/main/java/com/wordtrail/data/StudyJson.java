package com.wordtrail.data;

import com.wordtrail.entity.StudyState;
import com.wordtrail.entity.StudyState.Question;
import com.wordtrail.entity.StudyState.Round;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Optional versioned extension; old WordTrail accounts start with an empty guided-study state. */
final class StudyJson {
    private StudyJson() { }

    static JSONObject write(StudyState state) {
        JSONObject result = new JSONObject().put("version", 1).put("level", state.level)
                .put("goalTarget", state.goalTarget).put("goalBaseline", state.goalBaseline)
                .put("learned", state.learned).put("practiced", state.practiced)
                .put("mastered", state.mastered).put("mistakes", state.mistakes);
        JSONArray history = new JSONArray();
        state.history.forEach(round -> history.put(writeRound(round)));
        result.put("history", history);
        if (state.round != null) { result.put("round", writeRound(state.round)); }
        if (state.assessment != null) { result.put("assessment", writeRound(state.assessment)); }
        return result;
    }

    static StudyState read(JSONObject json) {
        StudyState state = new StudyState();
        if (json == null) { return state; }
        require(json.getInt("version") == 1);
        state.level = json.getInt("level");
        state.goalTarget = json.getInt("goalTarget");
        state.goalBaseline = json.getInt("goalBaseline");
        state.learned = json.getInt("learned");
        state.practiced = json.getInt("practiced");
        require(state.level >= 1 && state.level <= 3 && state.goalTarget > 0 && state.goalTarget <= 1000000
                && state.goalBaseline >= 0 && state.goalBaseline <= state.learned && state.practiced >= 0);
        for (Object id : json.getJSONArray("mastered")) { state.mastered.add((String) id); }
        JSONObject mistakes = json.getJSONObject("mistakes");
        for (String id : mistakes.keySet()) {
            JSONArray counts = mistakes.getJSONArray(id);
            require(counts.length() == 3);
            int[] numbers = {counts.getInt(0), counts.getInt(1), counts.getInt(2)};
            require(numbers[0] >= 0 && numbers[1] >= 0 && numbers[2] >= 0);
            state.mistakes.put(id, numbers);
        }
        for (Object item : json.getJSONArray("history")) { state.history.add(readRound((JSONObject) item)); }
        if (json.has("round")) { state.round = readRound(json.getJSONObject("round")); }
        if (json.has("assessment")) { state.assessment = readRound(json.getJSONObject("assessment")); }
        return state;
    }

    private static JSONObject writeRound(Round round) {
        JSONArray questions = new JSONArray();
        round.questions.forEach(q -> questions.put(new JSONObject().put("id", q.id).put("term", q.term)
                .put("definition", q.definition).put("level", q.level).put("options", q.options)));
        return new JSONObject().put("id", round.id).put("phase", round.phase).put("mode", round.mode)
                .put("completedAt", round.completedAt).put("adopted", round.adopted).put("level", round.level)
                .put("position", round.position).put("pass", round.pass).put("firstCorrect", round.firstCorrect)
                .put("netNew", round.netNew).put("questions", questions).put("pending", round.pending)
                .put("wrong", round.wrong).put("known", round.known).put("answers", round.answers);
    }

    private static Round readRound(JSONObject json) {
        Round round = new Round();
        round.id = json.getString("id");
        round.phase = json.getString("phase");
        round.mode = json.getString("mode");
        round.completedAt = json.getString("completedAt");
        round.adopted = json.getString("adopted");
        round.level = json.getInt("level");
        round.position = json.getInt("position");
        round.pass = json.getInt("pass");
        round.firstCorrect = json.getInt("firstCorrect");
        round.netNew = json.getInt("netNew");
        for (Object item : json.getJSONArray("questions")) {
            JSONObject q = (JSONObject) item;
            round.questions.add(new Question(q.getString("id"), q.getString("term"), q.getString("definition"),
                    q.getInt("level"), q.getJSONArray("options").toList().stream()
                            .map(String.class::cast).collect(java.util.stream.Collectors.toList())));
        }
        for (Object index : json.getJSONArray("pending")) { round.pending.add((Integer) index); }
        for (Object index : json.getJSONArray("wrong")) { round.wrong.add((Integer) index); }
        for (Object index : json.getJSONArray("known")) { round.known.add((Integer) index); }
        for (Object item : json.getJSONArray("answers")) {
            JSONArray answer = (JSONArray) item;
            require(answer.length() == 3 && answer.getInt(0) > 0 && validIndex(answer.getInt(1))
                    && answer.getInt(2) >= 0 && answer.getInt(2) <= 4);
            round.answers.add(new int[]{answer.getInt(0), answer.getInt(1), answer.getInt(2)});
        }
        require(!round.id.isBlank() && round.questions.size() == 20 && round.pass > 0
                && round.level >= 1 && round.level <= 3 && round.position >= 0
                && round.position <= round.pending.size() && round.firstCorrect >= 0 && round.firstCorrect <= 20
                && round.netNew >= 0 && round.netNew <= 20
                && List.of("OVERVIEW", "LEARN", "TEST", "REVIEW", "COMPLETED", "SUPERSEDED").contains(round.phase)
                && List.of("STUDY", "REPLAY", "ASSESSMENT").contains(round.mode)
                && round.questions.stream().map(q -> q.id).distinct().count() == 20
                && round.pending.stream().allMatch(StudyJson::validIndex)
                && round.wrong.stream().allMatch(StudyJson::validIndex)
                && round.known.stream().allMatch(StudyJson::validIndex));
        if (List.of("LEARN", "TEST", "REVIEW").contains(round.phase)) {
            require(round.position < round.pending.size());
        }
        return round;
    }

    private static boolean validIndex(int index) { return index >= 0 && index < 20; }
    private static void require(boolean valid) {
        if (!valid) { throw new IllegalArgumentException("Invalid guided-study snapshot."); }
    }
}
