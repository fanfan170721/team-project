package com.wordtrail.usecase;

import com.wordtrail.entity.*;
import com.wordtrail.usecase.port.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/** Fast in-memory test doubles keep business-rule tests independent of disk and HTTP. */
final class UseCaseFixture {
    final MemoryStore store = new MemoryStore();
    final SessionContext session = new SessionContext();
    final Clock clock = Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/New_York"));
    final AuthInteractor auth = new AuthInteractor(store, new PasswordHasher() {
        public String hash(char[] password) { return "test-hash:" + new String(password); }
        public boolean matches(char[] password, String hash) { return hash.equals(hash(password)); }
    }, session);
    final CatalogInteractor catalog = new CatalogInteractor(store,
            term -> new DictionaryGateway.Entry(term, "API definition", "An example.", "test service"), session);
    final ReviewInteractor review = new ReviewInteractor(store, store, session);
    final FlashcardInteractor flash = new FlashcardInteractor(store, store, session, clock, new Random(7));
    final QuizInteractor quiz = new QuizInteractor(store, store, session, clock, new Random(7));
    final ProgressInteractor progress = new ProgressInteractor(store, store, session, clock);
    final ContentInteractor content = new ContentInteractor(store, session);

    UseCaseFixture() {
        for (int i = 0; i < 4; i++) {
            store.words.add(new VocabularyWord("w" + i, "word" + i, i < 3 ? "Travel" : "Nature",
                    "definition" + i, "example", "original"));
            store.questions.add(new QuizQuestion("q" + i, "w" + i, "Question " + i,
                    Arrays.asList("correct" + i, "wrong a", "wrong b", "wrong c"), 0));
        }
    }

    void admin() { auth.setupAdministrator("admin", "password123".toCharArray()); }
    void learner(String name) { auth.register(name, "password123".toCharArray()); }
    void loginAdmin() { auth.login("admin", "password123".toCharArray()); }

    static final class MemoryStore implements AccountRepository, ContentRepository, ProgressRepository {
        final Map<String, User> users = new LinkedHashMap<>();
        final List<VocabularyWord> words = new ArrayList<>();
        final List<QuizQuestion> questions = new ArrayList<>();
        final Map<String, LearningProgress> progress = new HashMap<>();
        boolean failSave;
        public Optional<User> findUser(String username) { return Optional.ofNullable(users.get(username)); }
        public boolean hasAccounts() { return !users.isEmpty(); }
        public void createUser(User user) {
            if (users.containsKey(user.getUsername())) { throw new AppException("Duplicate account."); }
            users.put(user.getUsername(), user);
        }
        public List<VocabularyWord> getWords() { return new ArrayList<>(words); }
        public List<QuizQuestion> getQuestions() { return new ArrayList<>(questions); }
        public void addWord(VocabularyWord word) {
            if (words.stream().anyMatch(w -> w.getTerm().equalsIgnoreCase(word.getTerm())
                    && w.getTopic().equalsIgnoreCase(word.getTopic()))) { throw new AppException("Duplicate word."); }
            words.add(word);
        }
        public void addQuestion(QuizQuestion question) { questions.add(question); }
        public LearningProgress getProgress(String username) { return progress.getOrDefault(username, LearningProgress.empty()); }
        public void saveProgress(String username, LearningProgress data) {
            if (failSave) { throw new AppException("Simulated disk failure."); }
            progress.put(username, data);
        }
    }
}
