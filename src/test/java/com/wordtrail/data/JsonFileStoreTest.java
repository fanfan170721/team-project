package com.wordtrail.data;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.AppException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Instant;
import org.json.JSONObject;
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

    @Test void personalContentOverridesAndReviewHistorySurviveRestart() throws Exception {
        String id = "personal-test";
        Instant time = Instant.parse("2026-10-22T17:00:00Z");
        try (JsonFileStore store = new JsonFileStore(directory)) {
            store.createUser(new User("alice", "hash", Role.LEARNER));
            store.createUser(new User("bob", "hash", Role.LEARNER));
            LearningProgress progress = LearningProgress.empty()
                    .withPersonalWord(new VocabularyWord(id, "resilient", "Mindset", "Recovers.", "", "Personal"))
                    .withDefinition("seed-1", "My journey definition.").withReviewWord(id, true)
                    .withCompletedReview(id, LocalDate.of(2026, 10, 22), time)
                    .withCompletedReview(id, LocalDate.of(2026, 10, 22), time.plusSeconds(10));
            store.saveProgress("alice", progress);
        }
        try (JsonFileStore store = new JsonFileStore(directory)) {
            LearningProgress progress = store.getProgress("alice");
            assertEquals(1, progress.getPersonalWords().size());
            assertEquals("My journey definition.", progress.getDefinitions().get("seed-1"));
            assertEquals(2, progress.getReviewCounts().get(id));
            assertEquals(time.plusSeconds(10), progress.getLastReviewed().get(id));
            assertTrue(progress.getReviewWordIds().contains(id));
            assertTrue(store.getProgress("bob").getPersonalWords().isEmpty());
        }
    }

    @Test void oldVersionOneProgressWithoutNewFieldsStillLoads() throws Exception {
        Path file = directory.resolve("wordtrail.json");
        try (JsonFileStore store = new JsonFileStore(directory)) {
            store.createUser(new User("alice", "hash", Role.LEARNER));
            store.saveProgress("alice", LearningProgress.empty().withStudiedWord("seed-1", LocalDate.of(2026, 10, 20)));
        }
        JSONObject json = new JSONObject(Files.readString(file));
        JSONObject progress = json.getJSONObject("progress").getJSONObject("alice");
        for (String key : new String[]{"personalWords", "definitions", "reviewCounts", "lastReviewed"}) { progress.remove(key); }
        Files.writeString(file, json.toString());
        try (JsonFileStore store = new JsonFileStore(directory)) {
            assertTrue(store.getProgress("alice").getStudiedWordIds().contains("seed-1"));
            assertTrue(store.getProgress("alice").getReviewCounts().isEmpty());
            assertTrue(store.getProgress("alice").getPersonalWords().isEmpty());
        }
    }
}
