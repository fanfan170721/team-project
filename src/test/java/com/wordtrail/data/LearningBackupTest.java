package com.wordtrail.data;

import com.wordtrail.entity.*;
import com.wordtrail.usecase.AppException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LearningBackupTest {
    @TempDir Path directory;
    private JsonFileStore open(String folder) {
        JsonFileStore store = new JsonFileStore(directory.resolve(folder));
        store.createUser(new User("allen", "private-hash", Role.ADMIN));
        store.createUser(new User("peer", "peer-hash", Role.LEARNER));
        return store;
    }
    private String digest(JsonFileStore store, String user, Path file) {
        return store.inspectLearning(user, file.toString()).split("\n")[0];
    }
    private String exported(JsonFileStore store, Path file) throws Exception {
        store.exportLearning("allen", file.toString()); return Files.readString(file);
    }

    @Test void accountBackupIsPortablePreservesOtherAccountsAndRestoresCheckpointWithRecovery() throws Exception {
        Path archive = directory.resolve("portable.json");
        try (JsonFileStore store = open("source")) {
            String shared = store.getWords().get(0).getId();
            LearningProgress progress = store.getProgress("allen").withReviewWord(shared, true)
                    .withPersonalWord(new VocabularyWord("my-word", "rainy", "Weather", "with falling rain", "", "personal"));
            StudyState state = new StudyState(); state.goalTarget = 44;
            state.round = new StudyState.Round(); state.round.id = "old-token";
            state.round.questions.addAll(StudyContent.load().subList(0,20));
            for (int i=0;i<20;i++) { state.round.pending.add(i); }
            store.saveStudy("allen", state, progress);
            String text = exported(store, archive);
            assertFalse(text.contains("private-hash")); assertFalse(text.contains("peer-hash"));
            assertFalse(text.contains("passwordHash")); assertFalse(text.contains("\"role\""));
        }
        try (JsonFileStore destination = open("destination")) {
            String peerWord = destination.getWords().get(1).getId();
            destination.saveProgress("peer", destination.getProgress("peer").withReviewWord(peerWord, true));
            String checked = digest(destination, "peer", archive);
            String recovery = destination.restoreLearning("peer", archive.toString(), checked);
            assertTrue(Files.isRegularFile(Path.of(recovery)));
            assertEquals(Role.LEARNER, destination.findUser("peer").orElseThrow().getRole());
            assertEquals("peer-hash", destination.findUser("peer").orElseThrow().getPasswordHash());
            assertTrue(destination.getProgress("allen").getReviewWordIds().isEmpty());
            assertEquals("rainy", destination.getProgress("peer").getPersonalWords().get(0).getTerm());
            assertEquals(44, destination.getStudy("peer").goalTarget);
            assertNotEquals("old-token", destination.getStudy("peer").round.id);
            assertEquals("reference-1", destination.getStudy("peer").round.questions.get(0).id);
            // The automatically generated recovery file is a working inverse.
            destination.restoreLearning("peer", recovery, digest(destination, "peer", Path.of(recovery)));
            assertEquals(java.util.Set.of(peerWord), destination.getProgress("peer").getReviewWordIds());
            assertNull(destination.getStudy("peer").round);
        }
        try (JsonFileStore reopened = new JsonFileStore(directory.resolve("destination"))) {
            assertEquals(1, reopened.getProgress("peer").getReviewWordIds().size());
        }
    }

    @Test void rejectsCorruptionChangedFilesInvalidReferencesAndNeverOverwritesExistingFiles() throws Exception {
        Path archive=directory.resolve("backup.json");
        try (JsonFileStore store=open("source")) {
            exported(store,archive);
            byte[] original=Files.readAllBytes(directory.resolve("source/wordtrail.json"));
            String checked=digest(store,"allen",archive);
            Files.writeString(archive,Files.readString(archive)+" ");
            assertThrows(AppException.class,()->store.restoreLearning("allen",archive.toString(),checked));
            assertThrows(AppException.class,()->store.exportLearning("allen",archive.toString()));
            JSONObject doc=new JSONObject(Files.readString(archive));
            doc.getJSONObject("payload").getJSONObject("study").put("level",99);
            Files.writeString(archive,doc.toString());
            assertThrows(AppException.class,()->store.inspectLearning("allen",archive.toString()));
            JSONObject payload=doc.getJSONObject("payload");
            Files.writeString(archive,LearningBackupFiles.envelope(payload).toString());
            assertThrows(AppException.class,()->store.inspectLearning("allen",archive.toString()));
            Files.writeString(archive,"not json");
            assertThrows(AppException.class,()->store.inspectLearning("allen",archive.toString()));
            assertArrayEquals(original,Files.readAllBytes(directory.resolve("source/wordtrail.json")));
            assertThrows(AppException.class,()->store.inspectLearning("allen",directory.toString()));
            assertThrows(AppException.class,()->store.exportLearning("missing",directory.resolve("missing.json").toString()));
        }
    }

    @Test void sharedContentConflictsBecomePrivateAndInvalidReferencesCannotBeSmuggledIn() throws Exception {
        Path archive=directory.resolve("backup.json");
        try (JsonFileStore store=open("source")) {
            VocabularyWord word=store.getWords().get(0);
            store.saveProgress("allen",store.getProgress("allen").withReviewWord(word.getId(),true));
            exported(store,archive);
            JSONObject payload=new JSONObject(Files.readString(archive)).getJSONObject("payload");
            payload.getJSONArray("referencedWords").getJSONObject(0).put("definition","The earlier meaning");
            Files.writeString(archive,LearningBackupFiles.envelope(payload).toString());
            store.restoreLearning("allen",archive.toString(),digest(store,"allen",archive));
            assertEquals(word.getDefinition(),store.getWords().get(0).getDefinition());
            LearningProgress restored=store.getProgress("allen");
            assertEquals("The earlier meaning",restored.getPersonalWords().get(0).getDefinition());
            assertEquals(java.util.Set.of(restored.getPersonalWords().get(0).getId()),restored.getReviewWordIds());
            payload.getJSONObject("progress").getJSONArray("reviewWordIds").put("does-not-exist");
            Files.writeString(archive,LearningBackupFiles.envelope(payload).toString());
            assertThrows(AppException.class,()->store.inspectLearning("allen",archive.toString()));
        }
    }

    @Test void recoveryWriteFailureAndOversizedInputLeaveCurrentDataIntact() throws Exception {
        Path archive=directory.resolve("backup.json");
        try (JsonFileStore store=open("source")) {
            exported(store,archive);
            String checked=digest(store,"allen",archive);
            byte[] before=Files.readAllBytes(directory.resolve("source/wordtrail.json"));
            Files.writeString(directory.resolve("source/backups"),"blocks directory");
            assertThrows(AppException.class,()->store.restoreLearning("allen",archive.toString(),checked));
            assertArrayEquals(before,Files.readAllBytes(directory.resolve("source/wordtrail.json")));
            try (java.io.RandomAccessFile large=new java.io.RandomAccessFile(archive.toFile(),"rw")) {
                large.setLength(LearningBackupFiles.LIMIT+1);
            }
            assertThrows(AppException.class,()->store.inspectLearning("allen",archive.toString()));
        }
    }
}
