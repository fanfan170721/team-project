package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.FlashcardUseCase;
import java.util.Set;

/** Controls what a learner can see before and after revealing a flashcard. */
public final class FlashcardPresenter {
    public ViewModels.Card card(FlashcardUseCase.Card card, Set<String> saved) {
        VocabularyWord word = card.word;
        ViewModels.Word display = new ViewModels.Word(word.getId(), word.getTerm(), word.getTopic(),
                card.revealed ? word.getDefinition() : "Reveal the definition when you are ready.",
                card.revealed ? word.getExample() : "", card.revealed ? word.getSource() : "",
                saved.contains(word.getId()), card.reviewCount, "");
        return new ViewModels.Card(display, "Card " + card.position + " of " + card.total,
                card.revealed, card.studiedToday, card.reviewed);
    }
}
