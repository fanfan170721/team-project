package com.wordtrail.app;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.AppPresenter;
import com.wordtrail.data.ResilientDictionaryGateway;
import com.wordtrail.data.JsonFileStore;
import com.wordtrail.data.Pbkdf2PasswordHasher;
import com.wordtrail.usecase.*;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Random;

/** Composition root. Only this outer layer knows which adapters implement the ports. */
public final class Application implements AutoCloseable {
    private final JsonFileStore store;
    private final AppController controller;

    public Application(Path directory) { this(directory, new ResilientDictionaryGateway(), Clock.systemDefaultZone()); }

    public Application(Path directory, DictionaryGateway dictionary, Clock clock) {
        store = new JsonFileStore(directory);
        SessionContext session = new SessionContext();
        controller = new AppController(new AuthInteractor(store, new Pbkdf2PasswordHasher(), session),
                new CatalogInteractor(store, store, dictionary, session),
                new FlashcardInteractor(store, store, session, clock, new Random()),
                new QuizInteractor(store, store, session, clock, new Random()),
                new ReviewInteractor(store, store, session), new ProgressInteractor(store, store, session, clock),
                new ContentInteractor(store, session), new PersonalVocabularyInteractor(store, store, session),
                new AppPresenter(clock.getZone()));
    }

    public AppController controller() { return controller; }
    @Override public void close() throws IOException { store.close(); }
}
