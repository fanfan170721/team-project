package com.wordtrail.adapter;

import com.wordtrail.usecase.CatalogUseCase;
import com.wordtrail.usecase.ReviewUseCase;
import java.util.function.Supplier;

/** Routes review-list mutations and joins the saved result with current display metadata. */
public final class ReviewController {
    private final ReviewUseCase review;
    private final CatalogUseCase catalog;
    private final Supplier<ViewModels.VocabularyProgress> personalProgress;
    private final ReviewPresenter presenter;

    public ReviewController(ReviewUseCase review, CatalogUseCase catalog,
                            Supplier<ViewModels.VocabularyProgress> personalProgress, ReviewPresenter presenter) {
        this.review = review;
        this.catalog = catalog;
        this.personalProgress = personalProgress;
        this.presenter = presenter;
    }

    public ViewModels.Library list() {
        return ControllerCalls.call(() -> presenter.list(catalog.topics(), review.list(), personalProgress.get()));
    }

    public void setSaved(String id, boolean saved) {
        ControllerCalls.call(() -> { review.setSaved(id, saved); return null; });
    }
}
