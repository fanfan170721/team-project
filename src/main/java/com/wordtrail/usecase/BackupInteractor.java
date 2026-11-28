package com.wordtrail.usecase;
import com.wordtrail.usecase.port.BackupRepository;
/** Account identity is always taken from the signed-in session, never from an imported file. */
public final class BackupInteractor implements BackupUseCase {
    private final BackupRepository repository;
    private final SessionContext session;
    public BackupInteractor(BackupRepository repository, SessionContext session) {
        this.repository = repository; this.session = session;
    }
    private String user() { return session.requireUser().getUsername(); }
    @Override public void exportTo(String destination) {
        repository.exportLearning(user(), required(destination));
    }
    @Override public String inspect(String source) {
        return repository.inspectLearning(user(), required(source));
    }
    @Override public String restore(String source, String digest, boolean confirmed) {
        if (!confirmed) { throw new AppException("Confirm replacement of your learning records first."); }
        return repository.restoreLearning(user(), required(source), required(digest));
    }
    private String required(String value) {
        if (value == null || value.isBlank()) { throw new AppException("Choose a backup file first."); }
        return value;
    }
}
