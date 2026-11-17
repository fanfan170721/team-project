package com.wordtrail.adapter;

import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.CatalogUseCase;
import com.wordtrail.usecase.QuizUseCase;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UseCaseControllersTest {
    @Test void browsingReadsFreshAccountMetadataAndLookupDoesNotReplaceSavedDefinition() {
        VocabularyWord saved = new VocabularyWord("word", "hello", "Greetings", "Saved meaning", "", "Saved source");
        CatalogUseCase catalog = new CatalogUseCase() {
            @Override public List<String> topics() { return List.of("Greetings"); }
            @Override public List<VocabularyWord> browse(String topic, String search) { return List.of(saved); }
            @Override public DictionaryGateway.Entry lookup(String term) {
                return new DictionaryGateway.Entry(term, "Online meaning", "", "https://dictionary.example/hello");
            }
        };
        AtomicReference<ViewModels.VocabularyProgress> metadata = new AtomicReference<>(
                new ViewModels.VocabularyProgress(Set.of(), Map.of(), Map.of()));
        CatalogController controller = new CatalogController(catalog, metadata::get, new CatalogPresenter());
        assertFalse(controller.browse(null, "").words.get(0).saved);
        metadata.set(new ViewModels.VocabularyProgress(Set.of("word"), Map.of("word", 2), Map.of()));
        ViewModels.Word refreshed = controller.browse(null, "").words.get(0);
        assertTrue(refreshed.saved);
        assertEquals(2, refreshed.reviewCount);
        ViewModels.Definition online = controller.lookup("hello");
        assertEquals("Online meaning", online.definition);
        assertEquals("https://dictionary.example/hello", online.source);
        assertEquals("Saved meaning", controller.browse(null, "").words.get(0).definition);
    }

    @Test void expectedUseCaseFailuresCrossTheControllerAsDisplayErrors() {
        AppException failure = new AppException("Select one answer first.");
        QuizUseCase quiz = new QuizUseCase() {
            @Override public Question start(String topic, int maximumQuestions) { throw failure; }
            @Override public Question answer(int optionIndex) { throw failure; }
            @Override public Question next() { throw failure; }
        };
        QuizController controller = new QuizController(quiz, new QuizPresenter());
        UiException displayed = assertThrows(UiException.class, () -> controller.answer(-1));
        assertEquals("Select one answer first.", displayed.getMessage());
        assertSame(failure, displayed.getCause());
    }
}
