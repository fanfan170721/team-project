package com.wordtrail.app;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.StudyController;
import com.wordtrail.adapter.StudyPresenter;
import com.wordtrail.data.StudyContent;
import com.wordtrail.adapter.AuthController;
import com.wordtrail.adapter.AuthPresenter;
import com.wordtrail.adapter.CatalogController;
import com.wordtrail.adapter.CatalogPresenter;
import com.wordtrail.adapter.ContentController;
import com.wordtrail.adapter.ContentPresenter;
import com.wordtrail.adapter.ExportResultsController;
import com.wordtrail.adapter.ExportResultsPresenter;
import com.wordtrail.adapter.ExportResultsViewModel;
import com.wordtrail.adapter.FlashcardController;
import com.wordtrail.adapter.FlashcardPresenter;
import com.wordtrail.adapter.PersonalVocabularyController;
import com.wordtrail.adapter.PersonalVocabularyPresenter;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.adapter.ProgressController;
import com.wordtrail.adapter.ProgressPresenter;
import com.wordtrail.adapter.QuizController;
import com.wordtrail.adapter.QuizPresenter;
import com.wordtrail.adapter.ReviewController;
import com.wordtrail.adapter.ReviewPlanController;
import com.wordtrail.adapter.ReviewPlanPresenter;
import com.wordtrail.adapter.ReviewPlanViewModel;
import com.wordtrail.adapter.ReviewPresenter;
import com.wordtrail.data.CsvQuizResultsExporter;
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
        CatalogUseCase catalog = new CatalogInteractor(store, store, dictionary, session);
        ReviewUseCase review = new ReviewInteractor(store, store, session);
        ReviewPlanViewModel reviewPlanViewModel = new ReviewPlanViewModel();
        ReviewPlanPresenter reviewPlanPresenter = new ReviewPlanPresenter(reviewPlanViewModel);
        ReviewPlanController reviewPlan = new ReviewPlanController(
                new ReviewPlanInteractor(store, store, session, clock, reviewPlanPresenter), reviewPlanViewModel);
        PersonalVocabularyViewModel personalViewModel = new PersonalVocabularyViewModel();
        PersonalVocabularyPresenter personalPresenter = new PersonalVocabularyPresenter(
                clock.getZone(), personalViewModel);
        PersonalVocabularyController personal = new PersonalVocabularyController(
                new PersonalVocabularyInteractor(store, store, session, personalPresenter),
                personalPresenter, personalViewModel);
        ExportResultsViewModel exportViewModel = new ExportResultsViewModel();
        ExportResultsController export = new ExportResultsController(new ExportResultsInteractor(
                store, new CsvQuizResultsExporter(), session, new ExportResultsPresenter(exportViewModel)),
                exportViewModel);
        controller = new AppController(
                new AuthController(new AuthInteractor(store, new Pbkdf2PasswordHasher(), session), new AuthPresenter()),
                reviewPlan,
                new CatalogController(catalog, personal::progress, new CatalogPresenter()),
                new FlashcardController(new FlashcardInteractor(store, store, session, clock, new Random()),
                        review, new FlashcardPresenter()),
                new QuizController(new QuizInteractor(store, store, session, clock, new Random()), new QuizPresenter()),
                new ReviewController(review, catalog, personal::progress, new ReviewPresenter()),
                new ProgressController(new ProgressInteractor(store, store, session, clock),
                        new ProgressPresenter(clock.getZone())),
                new ContentController(new ContentInteractor(store, session), catalog, personal::progress,
                        new ContentPresenter()),
                personal, export);
        StudyPresenter studyPresenter = new StudyPresenter();
        controller.setStudy(new StudyController(new StudyInteractor(StudyContent.load(), store, store,
                session, clock, new Random(), studyPresenter), studyPresenter));
    }

    public AppController controller() { return controller; }
    @Override public void close() throws IOException { store.close(); }
}
