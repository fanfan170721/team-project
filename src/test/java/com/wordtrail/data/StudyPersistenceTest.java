package com.wordtrail.data;

import com.wordtrail.app.Application;
import com.wordtrail.entity.StudyState;
import com.wordtrail.usecase.AppException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class StudyPersistenceTest {
    @TempDir Path directory;

    @Test void legacyProfilesLoadWithoutStudyWritesAndUnrelatedProgressSavesPreserveTheRound() throws Exception {
        try (Application app = new Application(directory)) {
            app.controller().setup("allen", "password123".toCharArray());
        }
        byte[] legacy = Files.readAllBytes(directory.resolve("wordtrail.json"));
        try (JsonFileStore store = new JsonFileStore(directory)) {
            assertNull(store.getStudy("allen").round);
            assertArrayEquals(legacy, Files.readAllBytes(directory.resolve("wordtrail.json")));
            assertThrows(AppException.class, () -> store.saveStudy("missing", new StudyState(), store.getProgress("allen")));
        }
        String token;
        try (Application app = new Application(directory)) {
            app.controller().login("allen", "password123".toCharArray());
            token = app.controller().study().start().round.token;
            String word = app.controller().browse(null, "").words.get(0).id;
            app.controller().saveWord(word, true);
        }
        try (Application app = new Application(directory)) {
            app.controller().login("allen", "password123".toCharArray());
            assertEquals(token, app.controller().study().show().round.token);
            assertEquals(1, app.controller().reviewList().words.size());
        }
    }

    @Test void corruptStudySnapshotIsReportedAndItsOriginalBytesArePreserved() throws Exception {
        try (Application app = new Application(directory)) {
            app.controller().setup("allen", "password123".toCharArray());
            app.controller().study().start();
        }
        Path file = directory.resolve("wordtrail.json");
        JSONObject data = new JSONObject(Files.readString(file));
        data.getJSONObject("guidedStudy").getJSONObject("allen").getJSONObject("round").put("position", 999);
        Files.writeString(file, data.toString());
        byte[] corrupt = Files.readAllBytes(file);
        assertThrows(AppException.class, () -> new JsonFileStore(directory));
        assertArrayEquals(corrupt, Files.readAllBytes(file));
    }
}
