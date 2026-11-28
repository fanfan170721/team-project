package com.wordtrail.usecase;
import com.wordtrail.entity.PronunciationClip;
import com.wordtrail.usecase.port.PronunciationClient;
import com.wordtrail.usecase.port.PronunciationPlayer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Recorded speech only: unavailable recordings never start an unexpected synthetic voice. */
public final class PronunciationInteractor implements PronunciationUseCase {
    private final PronunciationClient client;
    private final PronunciationPlayer player;
    public PronunciationInteractor(PronunciationClient client, PronunciationPlayer player) {
        this.client = client;
        this.player = player;
    }
    @Override public void speak(String word, String accent, BooleanSupplier cancelled, Consumer<String> status) {
        if (word == null || word.isBlank() || word.length() > 100) {
            status.accept("Select a word of at most 100 characters.");
            return;
        }
        try {
            status.accept("Loading recorded pronunciation...");
            PronunciationClip clip = client.fetch(word, accent);
            if (cancelled.getAsBoolean()) { return; }
            String label = "Recorded voice: " + clip.accent() + " | " + clip.source() + " | " + clip.license();
            status.accept(label);
            player.play(clip.audio(), cancelled);
            if (!cancelled.getAsBoolean()) { status.accept("Finished. " + label); }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Exception failure) {
            if (!cancelled.getAsBoolean()) {
                status.accept("Recording unavailable. You can keep studying and try again later.");
            }
        }
    }
    @Override public void stop() { player.stop(); }
}
