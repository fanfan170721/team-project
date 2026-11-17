package com.wordtrail.adapter;

import java.util.List;

/** Stable Swing-facing facade; use-case controllers own orchestration and presentation. */
public final class AppController {
    private final AuthController auth;
    private final CatalogController catalog;
    private final FlashcardController flashcards;
    private final QuizController quiz;
    private final ReviewController review;
    private final ProgressController progress;
    private final ContentController content;
    private final PersonalVocabularyController personal;

    public AppController(AuthController auth, CatalogController catalog, FlashcardController flashcards,
                         QuizController quiz, ReviewController review, ProgressController progress,
                         ContentController content, PersonalVocabularyController personal) {
        this.auth = auth; this.catalog = catalog; this.flashcards = flashcards; this.quiz = quiz;
        this.review = review; this.progress = progress; this.content = content;
        this.personal = personal;
    }

    public boolean needsSetup() { return auth.needsSetup(); }
    public ViewModels.Account setup(String username, char[] password) {
        return auth.setup(username, password);
    }
    public ViewModels.Account register(String username, char[] password) {
        return auth.register(username, password);
    }
    public ViewModels.Account login(String username, char[] password) {
        return auth.login(username, password);
    }
    public void logout() { auth.logout(); }

    public ViewModels.Library browse(String topic, String search) {
        return catalog.browse(topic, search);
    }
    public ViewModels.Library reviewList() {
        return review.list();
    }
    public ViewModels.Library sharedWords() {
        return content.sharedWords();
    }
    public void saveWord(String id, boolean saved) { review.setSaved(id, saved); }
    public ViewModels.Definition lookup(String term) { return catalog.lookup(term); }
    public ViewModels.Card startCards(String topic, boolean reviewOnly) {
        return flashcards.start(topic, reviewOnly);
    }
    public ViewModels.Card revealCard() { return flashcards.reveal(); }
    public ViewModels.Card studyCard() { return flashcards.study(); }
    public ViewModels.Card nextCard() { return flashcards.next(); }
    public ViewModels.Quiz startQuiz(String topic, int count) { return quiz.start(topic, count); }
    public ViewModels.Quiz answer(int index) { return quiz.answer(index); }
    public ViewModels.Quiz nextQuestion() { return quiz.next(); }
    public ViewModels.Dashboard dashboard() { return progress.dashboard(); }

    public void addWord(String term, String topic, String definition, String example, String source) {
        content.addWord(term, topic, definition, example, source);
    }
    public void addQuestion(String wordId, String prompt, List<String> options, int correct) {
        content.addQuestion(wordId, prompt, options, correct);
    }

    public void addPersonalWord(String term, String topic, String definition, String example, String source) {
        personal.addWord(term, topic, definition, example, source);
    }

    public void changeDefinition(String wordId, String definition) {
        personal.changeDefinition(wordId, definition);
    }
}
