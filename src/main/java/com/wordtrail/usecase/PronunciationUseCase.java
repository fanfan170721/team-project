package com.wordtrail.usecase;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
public interface PronunciationUseCase {
    void speak(String word, String accent, BooleanSupplier cancelled, Consumer<String> status);
    void stop();
}
