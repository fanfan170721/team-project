package com.wordtrail.adapter;

import com.wordtrail.usecase.CatalogUseCase;
import java.util.function.Supplier;

/** Coordinates catalog results with current personal metadata and separate live lookup output. */
public final class CatalogController {
    private final CatalogUseCase catalog;
    private final Supplier<ViewModels.VocabularyProgress> personalProgress;
    private final CatalogPresenter presenter;

    public CatalogController(CatalogUseCase catalog, Supplier<ViewModels.VocabularyProgress> personalProgress,
                             CatalogPresenter presenter) {
        this.catalog = catalog;
        this.personalProgress = personalProgress;
        this.presenter = presenter;
    }

    public ViewModels.Library browse(String topic, String search) {
        return ControllerCalls.call(() -> presenter.library(catalog.topics(), catalog.browse(topic, search),
                personalProgress.get()));
    }

    public ViewModels.Definition lookup(String term) {
        return ControllerCalls.call(() -> presenter.definition(catalog.lookup(term)));
    }
}
