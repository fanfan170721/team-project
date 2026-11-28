package com.wordtrail.usecase.port;
/** Portable learning data; never exports passwords or changes account privileges. */
public interface BackupRepository {
    void exportLearning(String username, String destination);
    String inspectLearning(String username, String source);
    String restoreLearning(String username, String source, String expectedDigest);
}
