package com.wordtrail.adapter;

import com.wordtrail.usecase.PersonalVocabularyUseCase;
import com.wordtrail.usecase.PersonalDefinitionInput;
import com.wordtrail.usecase.PersonalWordInput;

/** Owns private-word edits and the refreshed display metadata consumed by word screens. */
public final class PersonalVocabularyController {
    private final PersonalVocabularyUseCase personal;
    private final PersonalVocabularyPresenter presenter;
    private final PersonalVocabularyViewModel viewModel;

    public PersonalVocabularyController(PersonalVocabularyUseCase personal, PersonalVocabularyPresenter presenter,
                                        PersonalVocabularyViewModel viewModel) {
        this.personal = personal;
        this.presenter = presenter;
        this.viewModel = viewModel;
    }

    public synchronized PersonalVocabularyViewModel.SaveResult addWord(
            String term, String topic, String definition, String example, String source) {
        viewModel.beginRequest();
        personal.addWord(new PersonalWordInput(term, topic, definition, example, source));
        return viewModel.result();
    }

    public synchronized PersonalVocabularyViewModel.SaveResult changeDefinition(String wordId, String definition) {
        viewModel.beginRequest();
        personal.changeDefinition(new PersonalDefinitionInput(wordId, definition));
        return viewModel.result();
    }

    public ViewModels.VocabularyProgress progress() {
        return ControllerCalls.call(() -> presenter.progress(personal.details()));
    }
}
