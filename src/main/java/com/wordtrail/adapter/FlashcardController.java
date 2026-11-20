package com.wordtrail.adapter;

import com.wordtrail.usecase.FlashcardUseCase;
import com.wordtrail.usecase.ReviewUseCase;
import java.util.function.Supplier;

/** Presents each card transition together with the current saved-for-review state. */
public final class FlashcardController {
    private final FlashcardUseCase flashcards;
    private final ReviewUseCase review;
    private final FlashcardPresenter presenter;

    public FlashcardController(FlashcardUseCase flashcards, ReviewUseCase review, FlashcardPresenter presenter) {
        this.flashcards = flashcards;
        this.review = review;
        this.presenter = presenter;
    }

    public ViewModels.Card start(String topic, boolean reviewOnly) {
        return present(() -> flashcards.start(topic, reviewOnly));
    }

    public ViewModels.Card reveal() { return present(flashcards::reveal); }
    public ViewModels.Card study() { return present(flashcards::markStudied); }
    public ViewModels.Card next() { return present(flashcards::next); }

    private ViewModels.Card present(Supplier<FlashcardUseCase.Card> transition) {
        return ControllerCalls.call(() -> presenter.card(transition.get(), review.savedIds()));
    }
}
