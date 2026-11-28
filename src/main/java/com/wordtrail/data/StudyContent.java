package com.wordtrail.data;

import com.wordtrail.entity.StudyState.Question;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** The reference's 60 English sample words, without changing existing saved library data. */
public final class StudyContent {
    private StudyContent() { }

    public static List<Question> load() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                StudyContent.class.getResourceAsStream("/study-content.csv"), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.lines().skip(1).map(line -> line.split(",", -1))
                    .collect(Collectors.toList());
            List<Question> result = new ArrayList<>();
            for (String[] row : rows) {
                List<String> choices = new ArrayList<>();
                choices.add(row[2]);
                for (String[] other : rows) {
                    if (!choices.contains(other[2]) && choices.size() < 4) { choices.add(other[2]); }
                }
                result.add(new Question("reference-" + row[0], row[1], row[2],
                        Integer.parseInt(row[3].substring(1)), choices));
            }
            return result;
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot read the bundled study curriculum.", failure);
        }
    }
}
