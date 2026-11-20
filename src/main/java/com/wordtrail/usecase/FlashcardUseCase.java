package com.wordtrail.usecase;

import com.wordtrail.entity.VocabularyWord;

public interface FlashcardUseCase {
    Card start(String topic, boolean reviewOnly);
    Card reveal();
    Card markStudied();
    Card next();

    final class Card {
        public final VocabularyWord word;
        public final int position;
        public final int total;
        public final boolean revealed;
        public final boolean studiedToday;
        public final boolean reviewed;
        public final int reviewCount;

        public Card(VocabularyWord word, int position, int total, boolean revealed, boolean studiedToday) {
            this(word, position, total, revealed, studiedToday, false, 0);
        }

        public Card(VocabularyWord word, int position, int total, boolean revealed, boolean studiedToday,
                    boolean reviewed, int reviewCount) {
            this.word = word;
            this.position = position;
            this.total = total;
            this.revealed = revealed;
            this.studiedToday = studiedToday;
            this.reviewed = reviewed;
            this.reviewCount = reviewCount;
        }
    }
}
