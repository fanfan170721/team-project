package com.wordtrail.data;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.port.AccountRepository;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.ProgressRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * One local unit of work for accounts, content and progress. Each mutation writes a
 * complete snapshot before publishing it in memory. A process lock prevents lost
 * updates from two app instances sharing the same directory.
 */
public final class JsonFileStore implements AccountRepository, ContentRepository,
        ProgressRepository, AutoCloseable {
    private final Path file;
    private FileChannel lockChannel;
    private FileLock lock;
    private JSONObject root;
    private boolean closed;

    public JsonFileStore(Path directory) {
        file = directory.toAbsolutePath().normalize().resolve("wordtrail.json");
        try {
            Files.createDirectories(file.getParent());
            lockChannel = FileChannel.open(file.resolveSibling("wordtrail.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            if (lock == null) { throw new AppException("This data folder is already open in another WordTrail window."); }
            if (Files.exists(file)) {
                try {
                    root = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
                    validate(root);
                }
                catch (RuntimeException ex) {
                    throw new AppException("The data file is invalid or from an unsupported version. "
                            + "It has been preserved: " + file, ex);
                }
            }
            else {
                try (InputStream stream = JsonFileStore.class.getResourceAsStream("/seed.json")) {
                    if (stream == null) { throw new IOException("Missing seed.json resource."); }
                    root = new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                }
                validate(root);
                write(root);
            }
        }
        catch (IOException | RuntimeException ex) {
            closeAfterFailure();
            if (ex instanceof AppException) { throw (AppException) ex; }
            if (ex instanceof OverlappingFileLockException) {
                throw new AppException("This data folder is already open in another WordTrail window.", ex);
            }
            throw new AppException("Cannot open the data folder: " + file.getParent(), ex);
        }
    }

    @Override
    public synchronized Optional<User> findUser(String username) {
        ensureOpen();
        for (Object item : root.getJSONArray("users")) {
            User user = readUser((JSONObject) item);
            if (user.getUsername().equals(username)) { return Optional.of(user); }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean hasAccounts() { ensureOpen(); return !root.getJSONArray("users").isEmpty(); }

    @Override
    public synchronized void createUser(User user) {
        if (findUser(user.getUsername()).isPresent()) { throw new AppException("That username is already taken."); }
        update(next -> next.getJSONArray("users").put(new JSONObject()
                .put("username", user.getUsername()).put("passwordHash", user.getPasswordHash())
                .put("role", user.getRole().name())));
    }

    @Override
    public synchronized List<VocabularyWord> getWords() {
        ensureOpen();
        List<VocabularyWord> words = new ArrayList<>();
        for (Object item : root.getJSONArray("words")) { words.add(readWord((JSONObject) item)); }
        return words;
    }

    @Override
    public synchronized List<QuizQuestion> getQuestions() {
        ensureOpen();
        List<QuizQuestion> questions = new ArrayList<>();
        for (Object item : root.getJSONArray("questions")) { questions.add(readQuestion((JSONObject) item)); }
        return questions;
    }

    @Override
    public synchronized void addWord(VocabularyWord word) {
        if (getWords().stream().anyMatch(existing -> existing.getId().equals(word.getId())
                || (existing.getTerm().equalsIgnoreCase(word.getTerm())
                && existing.getTopic().equalsIgnoreCase(word.getTopic())))) {
            throw new AppException("That word already exists in this topic.");
        }
        update(next -> next.getJSONArray("words").put(new JSONObject().put("id", word.getId())
                .put("term", word.getTerm()).put("topic", word.getTopic())
                .put("definition", word.getDefinition()).put("example", word.getExample())
                .put("source", word.getSource())));
    }

    @Override
    public synchronized void addQuestion(QuizQuestion question) {
        if (getWords().stream().noneMatch(word -> word.getId().equals(question.getWordId()))) {
            throw new AppException("Choose a word that still exists.");
        }
        if (getQuestions().stream().anyMatch(existing -> existing.getId().equals(question.getId()))) {
            throw new AppException("That question already exists.");
        }
        update(next -> next.getJSONArray("questions").put(new JSONObject()
                .put("id", question.getId()).put("wordId", question.getWordId())
                .put("prompt", question.getPrompt()).put("options", question.getOptions())
                .put("correctIndex", question.getCorrectIndex())));
    }

    @Override
    public synchronized LearningProgress getProgress(String username) {
        ensureOpen();
        JSONObject json = root.getJSONObject("progress").optJSONObject(username);
        return json == null ? LearningProgress.empty() : readProgress(json);
    }

    @Override
    public synchronized void saveProgress(String username, LearningProgress progress) {
        if (findUser(username).isEmpty()) { throw new AppException("The account no longer exists."); }
        update(next -> next.getJSONObject("progress").put(username, writeProgress(progress)));
    }

    private void update(Consumer<JSONObject> mutation) {
        ensureOpen();
        JSONObject next = new JSONObject(root.toString());
        mutation.accept(next);
        try { write(next); }
        catch (IOException ex) { throw new AppException("Could not save changes. Check disk space and folder permissions.", ex); }
        root = next;
    }

    private void write(JSONObject snapshot) throws IOException {
        Path temporary = Files.createTempFile(file.getParent(), "wordtrail-", ".tmp");
        try {
            byte[] bytes = snapshot.toString(2).getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) { channel.write(buffer); }
                channel.force(true);
            }
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally { Files.deleteIfExists(temporary); }
    }

    private static User readUser(JSONObject json) {
        return new User(json.getString("username"), json.getString("passwordHash"), Role.valueOf(json.getString("role")));
    }

    private static VocabularyWord readWord(JSONObject json) {
        return new VocabularyWord(json.getString("id"), json.getString("term"), json.getString("topic"),
                json.getString("definition"), json.getString("example"), json.getString("source"));
    }

    private static QuizQuestion readQuestion(JSONObject json) {
        List<String> options = new ArrayList<>();
        JSONArray array = json.getJSONArray("options");
        for (int i = 0; i < array.length(); i++) { options.add(array.getString(i)); }
        return new QuizQuestion(json.getString("id"), json.getString("wordId"), json.getString("prompt"),
                options, json.getInt("correctIndex"));
    }

    private static LearningProgress readProgress(JSONObject json) {
        Set<String> review = new LinkedHashSet<>();
        JSONArray reviewArray = json.getJSONArray("reviewWordIds");
        for (int i = 0; i < reviewArray.length(); i++) { review.add(reviewArray.getString(i)); }
        Map<LocalDate, Set<String>> days = new LinkedHashMap<>();
        JSONObject dayJson = json.getJSONObject("studyDays");
        for (String date : dayJson.keySet()) {
            Set<String> ids = new LinkedHashSet<>();
            JSONArray array = dayJson.getJSONArray(date);
            for (int i = 0; i < array.length(); i++) { ids.add(array.getString(i)); }
            days.put(LocalDate.parse(date), ids);
        }
        List<QuizAttempt> attempts = new ArrayList<>();
        for (Object item : json.getJSONArray("attempts")) {
            JSONObject attempt = (JSONObject) item;
            attempts.add(new QuizAttempt(Instant.parse(attempt.getString("completedAt")), attempt.getString("topic"),
                    attempt.getInt("correct"), attempt.getInt("total")));
        }
        return new LearningProgress(review, days, attempts);
    }

    private static JSONObject writeProgress(LearningProgress progress) {
        JSONObject days = new JSONObject();
        progress.getStudyDays().forEach((date, ids) -> days.put(date.toString(), new JSONArray(ids)));
        JSONArray attempts = new JSONArray();
        for (QuizAttempt attempt : progress.getAttempts()) {
            attempts.put(new JSONObject().put("completedAt", attempt.getCompletedAt().toString())
                    .put("topic", attempt.getTopic()).put("correct", attempt.getCorrect()).put("total", attempt.getTotal()));
        }
        return new JSONObject().put("reviewWordIds", new JSONArray(progress.getReviewWordIds()))
                .put("studyDays", days).put("attempts", attempts);
    }

    private static void validate(JSONObject json) {
        if (json.getInt("schemaVersion") != 1) { throw new IllegalArgumentException("Unsupported schema."); }
        Set<String> users = new HashSet<>();
        for (Object item : json.getJSONArray("users")) {
            User user = readUser((JSONObject) item);
            if (user.getUsername().isBlank() || user.getPasswordHash().isBlank()
                    || !users.add(user.getUsername())) { throw new IllegalArgumentException("Invalid account."); }
        }
        Set<String> words = new HashSet<>();
        for (Object item : json.getJSONArray("words")) {
            VocabularyWord word = readWord((JSONObject) item);
            if (word.getId().isBlank() || word.getTerm().isBlank() || word.getTopic().isBlank()
                    || word.getDefinition().isBlank() || !words.add(word.getId())) {
                throw new IllegalArgumentException("Invalid word.");
            }
        }
        Set<String> questions = new HashSet<>();
        for (Object item : json.getJSONArray("questions")) {
            QuizQuestion question = readQuestion((JSONObject) item);
            if (!words.contains(question.getWordId()) || !questions.add(question.getId())) {
                throw new IllegalArgumentException("Invalid question reference.");
            }
        }
        JSONObject progress = json.getJSONObject("progress");
        for (String username : progress.keySet()) {
            LearningProgress data = readProgress(progress.getJSONObject(username));
            if (!users.contains(username) || !words.containsAll(data.getReviewWordIds())
                    || !words.containsAll(data.getStudiedWordIds())) {
                throw new IllegalArgumentException("Invalid progress reference.");
            }
        }
    }

    private void ensureOpen() {
        if (closed) { throw new IllegalStateException("The data store is closed."); }
    }

    private void closeAfterFailure() {
        try { close(); }
        catch (IOException ignored) { /* Preserve the original failure. */ }
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        try { if (lock != null && lock.isValid()) { lock.release(); } }
        finally { if (lockChannel != null) { lockChannel.close(); } }
    }
}
