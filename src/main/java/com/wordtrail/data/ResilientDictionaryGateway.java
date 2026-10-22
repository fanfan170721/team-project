package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.port.DictionaryGateway;

/** Tries another live provider on failure; never substitutes local seed definitions. */
public final class ResilientDictionaryGateway implements DictionaryGateway {
    private final DictionaryGateway primary;
    private final DictionaryGateway backup;

    public ResilientDictionaryGateway() { this(new DictionaryApiClient(), new DatamuseDictionaryClient()); }

    public ResilientDictionaryGateway(DictionaryGateway primary, DictionaryGateway backup) {
        this.primary = primary; this.backup = backup;
    }

    @Override public Entry lookup(String term) {
        try { return primary.lookup(term); }
        catch (AppException primaryFailure) {
            if (Thread.currentThread().isInterrupted()) { throw primaryFailure; }
            try { return backup.lookup(term); }
            catch (AppException backupFailure) {
                AppException failure = new AppException("Online lookup failed. " + primaryFailure.getMessage()
                        + " " + backupFailure.getMessage() + " Your saved definition is unchanged.", primaryFailure);
                failure.addSuppressed(backupFailure);
                throw failure;
            }
        }
    }
}
