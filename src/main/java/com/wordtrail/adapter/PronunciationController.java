package com.wordtrail.adapter;
import com.wordtrail.usecase.PronunciationUseCase;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Cancels stale playback and also rejects status callbacks queued before a word change. */
public final class PronunciationController implements AutoCloseable {
    private final PronunciationUseCase input;
    private final Executor delivery;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "wordtrail-pronunciation");
        thread.setDaemon(true);
        return thread;
    });
    private volatile long generation;
    private volatile boolean closed;
    private Future<?> request;
    public PronunciationController(PronunciationUseCase input, Executor delivery) {
        this.input = input; this.delivery = delivery;
    }
    public synchronized void speak(String word, String accent, Consumer<String> status) {
        stop();
        if (closed) { return; }
        long token = generation;
        request = worker.submit(() -> input.speak(word, accent,
                () -> closed || generation != token || Thread.currentThread().isInterrupted(),
                text -> delivery.execute(() -> {
                    if (!closed && generation == token) { status.accept(text); }
                })));
    }
    public synchronized void stop() {
        generation++;
        if (request != null) { request.cancel(true); request = null; }
        input.stop();
    }
    @Override public synchronized void close() {
        closed = true;
        stop();
        worker.shutdownNow();
    }
}
