package com.wordtrail.usecase;

import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/** US-04: randomized multiple-choice quizzes with a single durable completion record. */
public final class QuizInteractor implements QuizUseCase {
    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;
    private final Clock clock;
    private final Random random;
    private List<QuizQuestion> questions = Collections.emptyList();
    private long sessionVersion = -1;
    private int position;
    private int score;
    private int selected = -1;
    private String topic;

    public QuizInteractor(ContentRepository content, ProgressRepository progress, SessionContext session,
                          Clock clock, Random random) {
        this.content = content;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
        this.random = random;
    }

    @Override public Question start(String selectedTopic, int maximumQuestions) {
        session.requireUser();
        if (maximumQuestions < 1 || maximumQuestions > 20) { throw new AppException("Choose 1-20 questions."); }
        Set<String> wordIds = ContentQueries.filter(content.getWords(), selectedTopic, "").stream()
                .map(word -> word.getId()).collect(Collectors.toSet());
        List<QuizQuestion> candidates = content.getQuestions().stream()
                .filter(question -> wordIds.contains(question.getWordId())).collect(Collectors.toList());
        if (candidates.isEmpty()) { throw new AppException("No quiz questions for this topic yet. Ask an admin to add one."); }
        Collections.shuffle(candidates, random);
        List<QuizQuestion> randomized = new ArrayList<>();
        for (QuizQuestion original : candidates.subList(0, Math.min(maximumQuestions, candidates.size()))) {
            String answer = original.getOptions().get(original.getCorrectIndex());
            List<String> options = new ArrayList<>(original.getOptions());
            Collections.shuffle(options, random);
            randomized.add(new QuizQuestion(original.getId(), original.getWordId(), original.getPrompt(),
                    options, options.indexOf(answer)));
        }
        questions = randomized;
        position = 0;
        score = 0;
        selected = -1;
        topic = selectedTopic == null || selectedTopic.isBlank() ? "All topics" : selectedTopic;
        sessionVersion = session.getVersion();
        return current();
    }

    @Override public Question answer(int optionIndex) {
        requireQuiz();
        if (selected != -1) { throw new AppException("This question has already been answered."); }
        if (optionIndex < 0 || optionIndex >= 4) { throw new AppException("Select one answer first."); }
        int nextScore = score + (optionIndex == questions.get(position).getCorrectIndex() ? 1 : 0);
        if (position == questions.size() - 1) {
            String username = session.requireUser().getUsername();
            progress.saveProgress(username, progress.getProgress(username)
                    .withAttempt(new QuizAttempt(Instant.now(clock), topic, nextScore, questions.size())));
        }
        // Only advance in-memory state after a successful final save, allowing safe retries.
        score = nextScore;
        selected = optionIndex;
        return current();
    }

    @Override public Question next() {
        requireQuiz();
        if (selected == -1) { throw new AppException("Submit an answer before continuing."); }
        if (position == questions.size() - 1) { throw new AppException("Quiz complete. Start a new quiz."); }
        position++;
        selected = -1;
        return current();
    }

    private void requireQuiz() {
        session.requireUser();
        if (questions.isEmpty() || sessionVersion != session.getVersion()) { throw new AppException("Start a new quiz."); }
    }

    private Question current() {
        requireQuiz();
        QuizQuestion question = questions.get(position);
        boolean answered = selected != -1;
        return new Question(question.getWordId(), question.getPrompt(), question.getOptions(), position + 1,
                questions.size(), answered, answered && position == questions.size() - 1,
                selected, answered ? question.getCorrectIndex() : -1, score);
    }
}
