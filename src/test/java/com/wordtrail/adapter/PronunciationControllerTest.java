package com.wordtrail.adapter;
import com.wordtrail.usecase.PronunciationUseCase;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class PronunciationControllerTest {
    @Test void stopAndCloseDiscardAlreadyQueuedCallbacksAndInterruptOldWork() throws Exception {
        List<Runnable> callbacks=new CopyOnWriteArrayList<>();
        List<String> displayed=new CopyOnWriteArrayList<>();
        CountDownLatch started=new CountDownLatch(1), interrupted=new CountDownLatch(1);
        PronunciationUseCase input=new PronunciationUseCase() {
            public void speak(String word,String accent,java.util.function.BooleanSupplier cancelled,
                              java.util.function.Consumer<String> status) {
                status.accept("old recording");
                started.countDown();
                try { new CountDownLatch(1).await(); }
                catch(InterruptedException expected) { interrupted.countDown(); Thread.currentThread().interrupt(); }
            }
            public void stop() { }
        };
        PronunciationController controller=new PronunciationController(input,callbacks::add);
        controller.speak("world","en-US",displayed::add);
        assertTrue(started.await(2,TimeUnit.SECONDS));
        controller.stop(); assertTrue(interrupted.await(2,TimeUnit.SECONDS));
        callbacks.forEach(Runnable::run); assertTrue(displayed.isEmpty());
        controller.close(); controller.speak("other","en-US",displayed::add);
        assertTrue(displayed.isEmpty());
    }
}
