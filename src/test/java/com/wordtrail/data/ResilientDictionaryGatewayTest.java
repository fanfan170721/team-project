package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResilientDictionaryGatewayTest {
    @Test void preservesPrimaryResultWithoutContactingBackup() {
        AtomicBoolean called = new AtomicBoolean();
        DictionaryGateway gateway = new ResilientDictionaryGateway(
                term -> new DictionaryGateway.Entry(term, "Primary definition", "", "primary"),
                term -> { called.set(true); throw new AppException("Should not run."); });
        assertEquals("primary", gateway.lookup("hello").source);
        assertFalse(called.get());
    }

    @Test void primary530UsesAnotherLiveProviderAndPreservesItsSource() {
        DictionaryGateway gateway = new ResilientDictionaryGateway(
                term -> { throw new AppException("Dictionary service returned HTTP 530."); },
                term -> new DictionaryGateway.Entry(term, "Backup definition", "", "https://api.datamuse.com/words"));
        DictionaryGateway.Entry result = gateway.lookup("climate");
        assertEquals("Backup definition", result.definition);
        assertEquals("https://api.datamuse.com/words", result.source);
    }

    @Test void bothFailuresAreReportedWithoutClaimingLocalDataIsOnline() {
        DictionaryGateway gateway = new ResilientDictionaryGateway(
                term -> { throw new AppException("HTTP 530"); }, term -> { throw new AppException("Backup unavailable"); });
        AppException failure = assertThrows(AppException.class, () -> gateway.lookup("hello"));
        assertTrue(failure.getMessage().contains("HTTP 530"));
        assertTrue(failure.getMessage().contains("Backup unavailable"));
        assertTrue(failure.getMessage().contains("unchanged"));
    }
}
