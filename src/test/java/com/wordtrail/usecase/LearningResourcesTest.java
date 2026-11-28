package com.wordtrail.usecase;
import com.wordtrail.entity.PronunciationClip;
import com.wordtrail.entity.Role;
import com.wordtrail.entity.User;
import com.wordtrail.usecase.port.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LearningResourcesTest {
    @Test void recordedAudioValidatesCancelsAndReportsFailureWithoutSyntheticFallback() {
        byte[] audio = {1, 2, 3};
        PronunciationClip clip = new PronunciationClip(audio, "en-GB", "Reader / source", "CC BY 3.0");
        audio[0] = 9;
        assertEquals(1, clip.audio()[0]);
        byte[] received = clip.audio(); received[0] = 8; assertEquals(1, clip.audio()[0]);
        AtomicBoolean played = new AtomicBoolean();
        AtomicBoolean stopped = new AtomicBoolean();
        PronunciationPlayer player = new PronunciationPlayer() {
            public void play(byte[] bytes, java.util.function.BooleanSupplier cancelled) {
                assertArrayEquals(new byte[]{1, 2, 3}, bytes); played.set(true);
            }
            public void stop() { stopped.set(true); }
        };
        PronunciationInteractor input = new PronunciationInteractor((word, accent) -> clip, player);
        List<String> status = new ArrayList<>();
        for (String bad : new String[]{null, "", "a".repeat(101)}) {
            input.speak(bad, "en-US", () -> false, status::add);
            assertTrue(status.get(status.size()-1).contains("Select a word"));
        }
        input.speak("world", "en-US", () -> true, status::add);
        assertFalse(played.get());
        input.speak("world", "en-US", () -> false, status::add);
        assertTrue(played.get()); assertTrue(status.get(status.size()-1).contains("en-GB"));
        input.stop(); assertTrue(stopped.get()); player.close();
        PronunciationInteractor unavailable = new PronunciationInteractor((word, accent) -> {
            throw new java.io.IOException("offline");
        }, player);
        unavailable.speak("world", "en-US", () -> false, status::add);
        assertTrue(status.get(status.size()-1).contains("unavailable"));
        new PronunciationInteractor((word, accent) -> { throw new InterruptedException(); }, player)
                .speak("world", "en-US", () -> false, status::add);
        assertTrue(Thread.interrupted());
    }

    @Test void backupAlwaysUsesSessionIdentityAndRequiresExplicitConfirmation() {
        SessionContext session = new SessionContext();
        List<String> calls = new ArrayList<>();
        BackupRepository repository = new BackupRepository() {
            public void exportLearning(String user, String file) { calls.add("export:" + user + ":" + file); }
            public String inspectLearning(String user, String file) { calls.add("inspect:" + user); return "digest"; }
            public String restoreLearning(String user, String file, String digest) { calls.add("restore:" + user); return "recovery"; }
        };
        BackupInteractor input = new BackupInteractor(repository, session);
        assertThrows(AppException.class, () -> input.exportTo("file"));
        session.signIn(new User("allen", "hash", Role.LEARNER));
        assertThrows(AppException.class, () -> input.exportTo(null));
        assertThrows(AppException.class, () -> input.inspect(" "));
        assertThrows(AppException.class, () -> input.restore("file", "digest", false));
        assertTrue(calls.isEmpty());
        input.exportTo("file"); assertEquals("digest", input.inspect("file"));
        assertEquals("recovery", input.restore("file", "digest", true));
        assertEquals(List.of("export:allen:file", "inspect:allen", "restore:allen"), calls);
    }
}
