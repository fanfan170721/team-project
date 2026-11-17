package com.wordtrail.adapter;

import com.wordtrail.usecase.CatalogUseCase;
import com.wordtrail.usecase.ContentUseCase;
import java.util.List;
import java.util.function.Supplier;

/** Coordinates admin word/question commands and the shared-word chooser. */
public final class ContentController {
    private final ContentUseCase content;
    private final CatalogUseCase catalog;
    private final Supplier<ViewModels.VocabularyProgress> personalProgress;
    private final ContentPresenter presenter;

    public ContentController(ContentUseCase content, CatalogUseCase catalog,
                             Supplier<ViewModels.VocabularyProgress> personalProgress, ContentPresenter presenter) {
        this.content = content;
        this.catalog = catalog;
        this.personalProgress = personalProgress;
        this.presenter = presenter;
    }

    public ViewModels.Library sharedWords() {
        return ControllerCalls.call(() -> presenter.sharedWords(catalog.topics(), content.sharedWords(),
                personalProgress.get()));
    }

    public void addWord(String term, String topic, String definition, String example, String source) {
        ControllerCalls.call(() -> { content.addWord(term, topic, definition, example, source); return null; });
    }

    public void addQuestion(String wordId, String prompt, List<String> options, int correct) {
        ControllerCalls.call(() -> { content.addQuestion(wordId, prompt, options, correct); return null; });
    }
}
