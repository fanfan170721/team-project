package com.wordtrail.data;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;
import com.wordtrail.usecase.AppException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JsonFileStoreTest {
    @TempDir Path directory;

    @Test void accountsAndLearningDataSurviveRestartAndStayIsolated() throws Exception {
        String id;
        try (JsonFileStore store = new JsonFileStore(directory)) {
            assertEquals(16, store.getWords().size());
            assertEquals(16, store.getQuestions().size());
            store.createUser(new User("alice", "hash", Role.ADMIN));
            store.createUser(new User("bob", "other-hash", Role.LEARNER));
            id = store.getWords().get(0).getId();
            store.saveProgress("alice", LearningProgress.empty().withReviewWord(id, true)
                    .withStudiedWord(id, LocalDate.of(2026, 10, 22)));
        }
        try (JsonFileStore store = new JsonFileStore(directory)) {
            assertEquals(Role.ADMIN, store.findUser("alice").orElseThrow().getRole());
            assertTrue(store.getProgress("alice").getReviewWordIds().contains(id));
            assertEquals(1, store.getProgress("alice").getStudiedWordIds().size());
            assertTrue(store.getProgress("bob").getReviewWordIds().isEmpty());
            assertThrows(AppException.class, () -> store.createUser(new User("alice", "x", Role.LEARNER)));
        }
    }

    @Test void secondInstanceCannotOverwriteFirst() throws Exception {
        try (JsonFileStore ignored = new JsonFileStore(directory)) {
            assertThrows(AppException.class, () -> new JsonFileStore(directory));
        }
        try (JsonFileStore reopened = new JsonFileStore(directory)) { assertFalse(reopened.hasAccounts()); }
    }

    @Test void corruptedFileIsNeverSilentlyReset() throws Exception {
        Path file = directory.resolve("wordtrail.json");
        Files.writeString(file, "broken data");
        assertThrows(AppException.class, () -> new JsonFileStore(directory));
        assertEquals("broken data", Files.readString(file));
    }

    @Test void failedWriteDoesNotPublishAnAccountInMemory() throws Exception {
        try (JsonFileStore store = new JsonFileStore(directory)) {
            Path file = directory.resolve("wordtrail.json");
            Path backup = directory.resolve("backup.json");
            Files.move(file, backup);
            Files.createDirectory(file);
            Files.writeString(file.resolve("blocker"), "prevents replacing this directory");
            assertThrows(AppException.class, () -> store.createUser(new User("alice", "hash", Role.LEARNER)));
            assertFalse(store.findUser("alice").isPresent());
            assertTrue(Files.exists(backup));
        }
    }
}
