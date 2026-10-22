package com.wordtrail.adapter;

import com.wordtrail.usecase.*;
import java.util.List;
import java.util.function.Supplier;

/** Input adapter. All view actions enter the core through use-case interfaces. */
public final class AppController {
    private final AuthUseCase auth;
    private final CatalogUseCase catalog;
    private final FlashcardUseCase flashcards;
    private final QuizUseCase quiz;
    private final ReviewUseCase review;
    private final ProgressUseCase progress;
    private final ContentUseCase content;
    private final AppPresenter presenter;
    private final PersonalVocabularyUseCase personal;

    public AppController(AuthUseCase auth, CatalogUseCase catalog, FlashcardUseCase flashcards,
                         QuizUseCase quiz, ReviewUseCase review, ProgressUseCase progress,
                         ContentUseCase content, PersonalVocabularyUseCase personal, AppPresenter presenter) {
        this.auth = auth; this.catalog = catalog; this.flashcards = flashcards; this.quiz = quiz;
        this.review = review; this.progress = progress; this.content = content; this.presenter = presenter;
        this.personal = personal;
    }

    public boolean needsSetup() { return call(auth::needsSetup); }
    public ViewModels.Account setup(String username, char[] password) {
        return call(() -> presenter.account(auth.setupAdministrator(username, password)));
    }
    public ViewModels.Account register(String username, char[] password) {
        return call(() -> presenter.account(auth.register(username, password)));
    }
    public ViewModels.Account login(String username, char[] password) {
        return call(() -> presenter.account(auth.login(username, password)));
    }
    public void logout() { call(() -> { auth.logout(); return null; }); }

    public ViewModels.Library browse(String topic, String search) {
        return call(() -> presenter.library(catalog.topics(), catalog.browse(topic, search), personal.details()));
    }
    public ViewModels.Library reviewList() {
        return call(() -> presenter.library(catalog.topics(), review.list(), personal.details()));
    }
    public ViewModels.Library sharedWords() {
        return call(() -> presenter.library(catalog.topics(), content.sharedWords(), personal.details()));
    }
    public void saveWord(String id, boolean saved) { call(() -> { review.setSaved(id, saved); return null; }); }
    public ViewModels.Definition lookup(String term) { return call(() -> presenter.definition(catalog.lookup(term))); }
    public ViewModels.Card startCards(String topic, boolean reviewOnly) {
        return call(() -> presenter.card(flashcards.start(topic, reviewOnly), review.savedIds()));
    }
    public ViewModels.Card revealCard() { return call(() -> presenter.card(flashcards.reveal(), review.savedIds())); }
    public ViewModels.Card studyCard() { return call(() -> presenter.card(flashcards.markStudied(), review.savedIds())); }
    public ViewModels.Card nextCard() { return call(() -> presenter.card(flashcards.next(), review.savedIds())); }
    public ViewModels.Quiz startQuiz(String topic, int count) { return call(() -> presenter.quiz(quiz.start(topic, count))); }
    public ViewModels.Quiz answer(int index) { return call(() -> presenter.quiz(quiz.answer(index))); }
    public ViewModels.Quiz nextQuestion() { return call(() -> presenter.quiz(quiz.next())); }
    public ViewModels.Dashboard dashboard() { return call(() -> presenter.dashboard(progress.report())); }

    public void addWord(String term, String topic, String definition, String example, String source) {
        call(() -> { content.addWord(term, topic, definition, example, source); return null; });
    }
    public void addQuestion(String wordId, String prompt, List<String> options, int correct) {
        call(() -> { content.addQuestion(wordId, prompt, options, correct); return null; });
    }

    public void addPersonalWord(String term, String topic, String definition, String example, String source) {
        call(() -> { personal.addWord(term, topic, definition, example, source); return null; });
    }

    public void changeDefinition(String wordId, String definition) {
        call(() -> { personal.changeDefinition(wordId, definition); return null; });
    }

    private <T> T call(Supplier<T> action) {
        try { return action.get(); }
        catch (AppException ex) { throw new UiException(ex.getMessage(), ex); }
    }
}
