package com.wordtrail.adapter;

import com.wordtrail.usecase.PersonalVocabularyUseCase;

/** Owns private-word edits and the refreshed display metadata consumed by word screens. */
public final class PersonalVocabularyController {
    private final PersonalVocabularyUseCase personal;
    private final PersonalVocabularyPresenter presenter;

    public PersonalVocabularyController(PersonalVocabularyUseCase personal, PersonalVocabularyPresenter presenter) {
        this.personal = personal;
        this.presenter = presenter;
    }

    public void addWord(String term, String topic, String definition, String example, String source) {
        ControllerCalls.call(() -> { personal.addWord(term, topic, definition, example, source); return null; });
    }

    public void changeDefinition(String wordId, String definition) {
        ControllerCalls.call(() -> { personal.changeDefinition(wordId, definition); return null; });
    }

    public ViewModels.VocabularyProgress progress() {
        return ControllerCalls.call(() -> presenter.progress(personal.details()));
    }
}
