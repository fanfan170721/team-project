package com.wordtrail.usecase;
public interface BackupUseCase {
    void exportTo(String destination);
    String inspect(String source);
    String restore(String source, String digest, boolean confirmed);
}
