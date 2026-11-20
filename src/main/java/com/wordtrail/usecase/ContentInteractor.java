package com.wordtrail.usecase;

import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** US-07: content forms are authorized here as well as being hidden from learners. */
public final class ContentInteractor implements ContentUseCase {
    private final ContentRepository content;
    private final SessionContext session;

    public ContentInteractor(ContentRepository content, SessionContext session) {
        this.content = content;
        this.session = session;
    }

    @Override
    public List<VocabularyWord> sharedWords() {
        session.requireAdmin();
        return content.getWords();
    }

    @Override
    public VocabularyWord addWord(String term, String topic, String definition, String example, String source) {
        session.requireAdmin();
        String cleanTerm = InputValidation.text(term, "Word", 80);
        String cleanTopic = InputValidation.text(topic, "Topic", 50);
        // Reuse existing capitalization rather than creating visually duplicate topics.
        for (VocabularyWord existing : content.getWords()) {
            if (existing.getTopic().equalsIgnoreCase(cleanTopic)) {
                cleanTopic = existing.getTopic();
                break;
            }
        }
        String cleanDefinition = InputValidation.text(definition, "Definition", 2000);
        String cleanExample = example == null ? "" : example.strip();
        if (cleanExample.length() > 2000) {
            throw new AppException("Example must be at most 2000 characters.");
        }
        String cleanSource = source == null || source.isBlank() ? "Admin-created content"
                : InputValidation.text(source, "Source", 500);
        VocabularyWord word = new VocabularyWord(UUID.randomUUID().toString(), cleanTerm, cleanTopic,
                cleanDefinition, cleanExample, cleanSource);
        content.addWord(word);
        return word;
    }

    @Override
    public QuizQuestion addQuestion(String wordId, String prompt, List<String> options, int correctIndex) {
        session.requireAdmin();
        ContentQueries.requireWord(content, wordId);
        String cleanPrompt = InputValidation.text(prompt, "Question", 1000);
        if (options == null || options.size() != 4 || correctIndex < 0 || correctIndex >= 4) {
            throw new AppException("Supply four choices and select the correct answer.");
        }
        List<String> cleanOptions = new ArrayList<>();
        for (String option : options) {
            cleanOptions.add(InputValidation.text(option, "Answer choice", 2000));
        }
        HashSet<String> distinct = new HashSet<>();
        for (String option : cleanOptions) {
            if (!distinct.add(option.toLowerCase(Locale.ROOT))) {
                throw new AppException("Answer choices must be distinct.");
            }
        }
        QuizQuestion question = new QuizQuestion(UUID.randomUUID().toString(), wordId,
                cleanPrompt, cleanOptions, correctIndex);
        content.addQuestion(question);
        return question;
    }
}
