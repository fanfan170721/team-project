package com.wordtrail.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuizValidationTest {
    @Test
    void completedResultsRejectImpossibleScoresAndAllowBothValidEndpoints() {
        Instant completion = Instant.parse("2026-11-17T12:00:00Z");
        assertThrows(IllegalArgumentException.class,
                () -> new QuizAttempt(completion, "Nature", 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizAttempt(completion, "Nature", -1, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizAttempt(completion, "Nature", 5, 4));
        assertEquals(0, new QuizAttempt(completion, "Nature", 0, 4).getCorrect());
        assertEquals(4, new QuizAttempt(completion, "Nature", 4, 4).getCorrect());
    }

    @Test
    void questionsRejectAmbiguousChoicesAndOutOfRangeAnswers() {
        List<String> valid = Arrays.asList("a", "b", "c", "d");
        for (List<String> invalid : Arrays.asList(
                Arrays.asList("a", "b", "c"),
                Arrays.asList("a", "a", "c", "d"),
                Arrays.asList("a", null, "c", "d"),
                Arrays.asList("a", " ", "c", "d"))) {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuizQuestion("q", "w", "Meaning?", invalid, 0));
        }
        assertThrows(IllegalArgumentException.class,
                () -> new QuizQuestion("q", "w", "Meaning?", valid, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizQuestion("q", "w", "Meaning?", valid, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizQuestion("q", "w", " ", valid, 0));
    }

    @Test
    void questionChoicesStayStableWhenTheOriginalListChanges() {
        List<String> choices = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        QuizQuestion question = new QuizQuestion("q", "w", "Meaning?", choices, 3);
        choices.set(3, "changed elsewhere");
        assertEquals("d", question.getOptions().get(question.getCorrectIndex()));
        assertThrows(UnsupportedOperationException.class,
                () -> question.getOptions().set(0, "changed through getter"));
    }
}
