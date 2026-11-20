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
    private final PersonalVocabularyOutputBoundary output;

    public PersonalVocabularyInteractor(ContentRepository content, ProgressRepository progress,
                                        SessionContext session, PersonalVocabularyOutputBoundary output) {
        this.content = content;
        this.progress = progress;
        this.session = session;
        this.output = output;
    }

    @Override
    public void addWord(PersonalWordInput input) {
        PersonalVocabularyResponse response;
        try {
            response = saveWord(input);
        } catch (AppException failure) {
            output.presentFailure(failure.getMessage());
            return;
        }
        output.presentSuccess(response);
    }

    private PersonalVocabularyResponse saveWord(PersonalWordInput input) {
        String username = session.requireUser().getUsername();
        if (input == null) {
            throw new AppException("Please provide a personal word.");
        }
        LearningProgress current = progress.getProgress(username);
        String cleanTerm = InputValidation.text(input.term, "Word", 80);
        String cleanTopic = InputValidation.text(input.topic, "Topic", 50);
        String cleanDefinition = InputValidation.text(input.definition, "Definition", 2000);
        String cleanExample = input.example == null ? "" : input.example.strip();
        if (cleanExample.length() > 2000) {
            throw new AppException("Example must be at most 2000 characters.");
        }
        String cleanSource = input.source == null || input.source.isBlank() ? "Personal vocabulary"
                : InputValidation.text(input.source, "Source", 500);
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
        return new PersonalVocabularyResponse(PersonalVocabularyResponse.Action.ADDED,
                word.getId(), word.getTerm(), word.getDefinition());
    }

    @Override
    public void changeDefinition(PersonalDefinitionInput input) {
        PersonalVocabularyResponse response;
        try {
            response = saveDefinition(input);
        } catch (AppException failure) {
            output.presentFailure(failure.getMessage());
            return;
        }
        output.presentSuccess(response);
    }

    private PersonalVocabularyResponse saveDefinition(PersonalDefinitionInput input) {
        String username = session.requireUser().getUsername();
        if (input == null) {
            throw new AppException("Please provide a definition change.");
        }
        LearningProgress current = progress.getProgress(username);
        VocabularyWord word = ContentQueries.requireVisibleWord(content, current, input.wordId);
        String definition = InputValidation.text(input.definition, "Definition", 2000);
        progress.saveProgress(username, current.withDefinition(word.getId(), definition));
        return new PersonalVocabularyResponse(PersonalVocabularyResponse.Action.DEFINITION_CHANGED,
                word.getId(), word.getTerm(), definition);
    }

    @Override public LearningProgress details() { return progress.getProgress(session.requireUser().getUsername()); }
}
