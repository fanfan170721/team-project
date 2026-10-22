package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.util.List;
import java.util.UUID;

public final class PersonalVocabularyInteractor implements PersonalVocabularyUseCase {
    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;

    public PersonalVocabularyInteractor(ContentRepository content, ProgressRepository progress, SessionContext session) {
        this.content = content; this.progress = progress; this.session = session;
    }

    @Override public VocabularyWord addWord(String term, String topic, String definition, String example, String source) {
        String username = session.requireUser().getUsername();
        LearningProgress current = progress.getProgress(username);
        String cleanTerm = InputValidation.text(term, "Word", 80);
        String cleanTopic = InputValidation.text(topic, "Topic", 50);
        String cleanDefinition = InputValidation.text(definition, "Definition", 2000);
        String cleanExample = example == null ? "" : example.strip();
        if (cleanExample.length() > 2000) { throw new AppException("Example must be at most 2000 characters."); }
        String cleanSource = source == null || source.isBlank() ? "Personal vocabulary"
                : InputValidation.text(source, "Source", 500);
        List<VocabularyWord> visible = ContentQueries.visibleWords(content, current);
        for (VocabularyWord existing : visible) {
            if (existing.getTopic().equalsIgnoreCase(cleanTopic)) {
                cleanTopic = existing.getTopic();
                if (existing.getTerm().equalsIgnoreCase(cleanTerm)) {
                    throw new AppException("That word already exists in this topic. Use Edit my definition instead.");
                }
            }
        }
        VocabularyWord word = new VocabularyWord(UUID.randomUUID().toString(), cleanTerm, cleanTopic,
                cleanDefinition, cleanExample, cleanSource);
        progress.saveProgress(username, current.withPersonalWord(word));
        return word;
    }

    @Override public void changeDefinition(String wordId, String definition) {
        String username = session.requireUser().getUsername();
        LearningProgress current = progress.getProgress(username);
        ContentQueries.requireVisibleWord(content, current, wordId);
        progress.saveProgress(username, current.withDefinition(wordId,
                InputValidation.text(definition, "Definition", 2000)));
    }

    @Override public LearningProgress details() { return progress.getProgress(session.requireUser().getUsername()); }
}
