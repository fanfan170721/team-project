package com.wordtrail.usecase;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** US-05: each account owns its review list. Adds/removes are idempotent. */
public final class ReviewInteractor implements ReviewUseCase {
    private final ContentRepository content;
    private final ProgressRepository progress;
    private final SessionContext session;

    public ReviewInteractor(ContentRepository content, ProgressRepository progress, SessionContext session) {
        this.content = content;
        this.progress = progress;
        this.session = session;
    }

    @Override public Set<String> savedIds() {
        return progress.getProgress(session.requireUser().getUsername()).getReviewWordIds();
    }

    @Override public List<VocabularyWord> list() {
        Set<String> ids = savedIds();
        return ContentQueries.filter(content.getWords(), null, "").stream()
                .filter(word -> ids.contains(word.getId())).collect(Collectors.toList());
    }

    @Override public void setSaved(String wordId, boolean saved) {
        String username = session.requireUser().getUsername();
        ContentQueries.requireWord(content, wordId);
        LearningProgress current = progress.getProgress(username);
        progress.saveProgress(username, current.withReviewWord(wordId, saved));
    }
}
