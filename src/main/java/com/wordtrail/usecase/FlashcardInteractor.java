package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** US-03: a deck is scoped to the authenticated session and studied explicitly. */
public final class FlashcardInteractor implements FlashcardUseCase {
    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;
    private final Clock clock;
    private final Random random;
    private List<VocabularyWord> deck = Collections.emptyList();
    private int position;
    private boolean revealed;
    private long sessionVersion = -1;

    public FlashcardInteractor(ContentRepository content, ProgressRepository progress, SessionContext session,
                               Clock clock, Random random) {
        this.content = content;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
        this.random = random;
    }

    @Override public Card start(String topic, boolean reviewOnly) {
        String username = session.requireUser().getUsername();
        List<VocabularyWord> candidates = new ArrayList<>(ContentQueries.filter(content.getWords(), topic, ""));
        if (reviewOnly) {
            Set<String> saved = progress.getProgress(username).getReviewWordIds();
            candidates.removeIf(word -> !saved.contains(word.getId()));
        }
        if (candidates.isEmpty()) { throw new AppException("No words match this deck. Save some words or choose another topic."); }
        Collections.shuffle(candidates, random);
        deck = candidates;
        position = 0;
        revealed = false;
        sessionVersion = session.getVersion();
        return current();
    }

    @Override public Card reveal() { requireDeck(); revealed = true; return current(); }

    @Override public Card markStudied() {
        requireDeck();
        if (!revealed) { throw new AppException("Reveal the definition before marking a card studied."); }
        String username = session.requireUser().getUsername();
        LearningProgress updated = progress.getProgress(username)
                .withStudiedWord(deck.get(position).getId(), LocalDate.now(clock));
        progress.saveProgress(username, updated);
        return current();
    }

    @Override public Card next() {
        requireDeck();
        position = (position + 1) % deck.size();
        revealed = false;
        return current();
    }

    private void requireDeck() {
        session.requireUser();
        if (deck.isEmpty() || sessionVersion != session.getVersion()) {
            throw new AppException("Start a new flashcard deck.");
        }
    }

    private Card current() {
        requireDeck();
        Set<String> studied = progress.getProgress(session.requireUser().getUsername()).getStudyDays()
                .getOrDefault(LocalDate.now(clock), Collections.emptySet());
        return new Card(deck.get(position), position + 1, deck.size(), revealed,
                studied.contains(deck.get(position).getId()));
    }
}
