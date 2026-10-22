package com.wordtrail.adapter;

import com.wordtrail.entity.*;
import com.wordtrail.usecase.*;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** Converts use-case output into display data without a Swing dependency. */
public final class AppPresenter {
    private final ZoneId zone;

    public AppPresenter(ZoneId zone) { this.zone = zone; }

    public ViewModels.Account account(User user) {
        return new ViewModels.Account(user.getUsername(), user.getRole() == Role.ADMIN);
    }

    private ViewModels.Word word(VocabularyWord word, LearningProgress data) {
        String time = data.getLastReviewed().containsKey(word.getId())
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zone)
                        .format(data.getLastReviewed().get(word.getId())) : "";
        return new ViewModels.Word(word.getId(), word.getTerm(), word.getTopic(), word.getDefinition(),
                word.getExample(), word.getSource(), data.getReviewWordIds().contains(word.getId()),
                data.getReviewCounts().getOrDefault(word.getId(), 0), time);
    }

    public ViewModels.Library library(List<String> topics, List<VocabularyWord> words, LearningProgress data) {
        return new ViewModels.Library(topics, words.stream().map(w -> word(w, data))
                .collect(Collectors.toList()));
    }

    public ViewModels.Card card(FlashcardUseCase.Card card, Set<String> saved) {
        VocabularyWord word = card.word;
        ViewModels.Word display = new ViewModels.Word(word.getId(), word.getTerm(), word.getTopic(),
                card.revealed ? word.getDefinition() : "Reveal the definition when you are ready.",
                card.revealed ? word.getExample() : "", card.revealed ? word.getSource() : "",
                saved.contains(word.getId()), card.reviewCount, "");
        return new ViewModels.Card(display, "Card " + card.position + " of " + card.total,
                card.revealed, card.studiedToday, card.reviewed);
    }

    public ViewModels.Quiz quiz(QuizUseCase.Question question) {
        String feedback = "";
        if (question.answered) {
            feedback = question.selectedIndex == question.correctIndex ? "Correct!" :
                    "Correct answer: " + question.options.get(question.correctIndex);
        }
        if (question.finished) {
            feedback += "\nQuiz complete: " + question.score + " / " + question.total + " ("
                    + Math.round(100.0 * question.score / question.total) + "%). Saved to your progress.";
        }
        return new ViewModels.Quiz(question.wordId, question.prompt, question.options,
                "Question " + question.position + " of " + question.total, feedback, question.answered, question.finished);
    }

    public ViewModels.Dashboard dashboard(ProgressUseCase.Report report) {
        LearningProgress data = report.progress;
        int correct = data.getAttempts().stream().mapToInt(QuizAttempt::getCorrect).sum();
        int total = data.getAttempts().stream().mapToInt(QuizAttempt::getTotal).sum();
        List<String[]> daily = new ArrayList<>();
        for (int offset = 13; offset >= 0; offset--) {
            LocalDate date = report.today.minusDays(offset);
            daily.add(new String[]{date.toString(),
                    Integer.toString(data.getStudyDays().getOrDefault(date, Collections.emptySet()).size())});
        }
        List<QuizAttempt> attempts = new ArrayList<>(data.getAttempts());
        Collections.reverse(attempts);
        attempts.sort(Comparator.comparing(QuizAttempt::getCompletedAt).reversed());
        List<String[]> rows = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone);
        for (QuizAttempt attempt : attempts) {
            rows.add(new String[]{formatter.format(attempt.getCompletedAt()), attempt.getTopic(),
                    attempt.getCorrect() + " / " + attempt.getTotal(),
                    Math.round(100.0 * attempt.getCorrect() / attempt.getTotal()) + "%"});
        }
        return new ViewModels.Dashboard(data.getStudiedWordIds().size(), report.totalWords,
                data.getReviewWordIds().size(), attempts.size(), total == 0 ? 0 : (int) Math.round(100.0 * correct / total),
                report.studyStreak, data.getReviewCounts().values().stream().mapToInt(Integer::intValue).sum(), daily, rows);
    }

    public ViewModels.Definition definition(DictionaryGateway.Entry entry) {
        return new ViewModels.Definition(entry.term, entry.definition, entry.example, entry.source);
    }
}
