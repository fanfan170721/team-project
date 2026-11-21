package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.CatalogController;
import com.wordtrail.adapter.CatalogPresenter;
import com.wordtrail.adapter.PersonalVocabularyPresenter;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.adapter.ReviewController;
import com.wordtrail.adapter.ReviewPresenter;
import com.wordtrail.adapter.ViewModels;
import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.CatalogUseCase;
import com.wordtrail.usecase.ReviewUseCase;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.awt.Component;
import java.awt.Container;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Exercises real table sorting with the vocabulary metadata and row presenters. */
@Timeout(30)
class LibraryReviewOrderTest {
    @Test
    void vocabularySortsByInstantAcrossTheTorontoClockRollback() throws Exception {
        assertRecentReviewOrder(false);
    }

    @Test
    void reviewListSortsByInstantAcrossTheTorontoClockRollback() throws Exception {
        assertRecentReviewOrder(true);
    }

    private void assertRecentReviewOrder(boolean reviewOnly) throws Exception {
        List<VocabularyWord> words = List.of(word("zulu"), word("older"), word("never"), word("alpha"));
        LocalDate day = LocalDate.of(2026, 11, 1);
        LearningProgress progress = LearningProgress.empty()
                .withCompletedReview("older", day, Instant.parse("2026-11-01T05:50:00Z"))
                .withCompletedReview("zulu", day, Instant.parse("2026-11-01T06:10:00Z"))
                .withCompletedReview("alpha", day, Instant.parse("2026-11-01T06:10:00Z"));
        for (VocabularyWord word : words) {
            progress = progress.withReviewWord(word.getId(), true);
        }
        ViewModels.VocabularyProgress metadata = new PersonalVocabularyPresenter(
                ZoneId.of("America/Toronto"), new PersonalVocabularyViewModel()).progress(progress);
        CatalogUseCase catalog = new CatalogUseCase() {
            @Override
            public List<String> topics() {
                return List.of("Review");
            }

            @Override
            public List<VocabularyWord> browse(String topic, String query) {
                return words;
            }

            @Override
            public DictionaryGateway.Entry lookup(String term) {
                throw new AssertionError("Sorting must not perform a dictionary lookup.");
            }
        };
        ReviewUseCase review = new ReviewUseCase() {
            @Override
            public List<VocabularyWord> list() {
                return words;
            }

            @Override
            public Set<String> savedIds() {
                return metadata.savedIds;
            }

            @Override
            public void setSaved(String wordId, boolean saved) {
                throw new AssertionError("Sorting must not change saved vocabulary.");
            }
        };
        AppController controller = new AppController(null, null,
                new CatalogController(catalog, () -> metadata, new CatalogPresenter()), null, null,
                new ReviewController(review, catalog, () -> metadata, new ReviewPresenter()), null, null, null, null);
        JPanel root = edt(JPanel::new);
        UiTasks tasks = new UiTasks(root, message -> { });
        LibraryPanel panel = edt(() -> {
            LibraryPanel result = new LibraryPanel(controller, tasks, reviewOnly);
            root.add(result);
            result.refresh();
            return result;
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (edt(tasks::isBusy) && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertFalse(edt(tasks::isBusy), "Vocabulary loading must finish before sorting.");
        edt(() -> {
            String prefix = reviewOnly ? "review" : "library";
            JComboBox<?> order = (JComboBox<?>) find(panel, prefix + ".order");
            order.setSelectedItem("Recently reviewed");
            JTable table = (JTable) find(panel, prefix + ".table");
            List<String> displayedWords = new ArrayList<>();
            for (int row = 0; row < table.getRowCount(); row++) {
                displayedWords.add((String) table.getValueAt(row, 0));
            }
            assertEquals(List.of("alpha", "zulu", "older", "never"), displayedWords,
                    "Newest instant comes first, ties use word order, and never-reviewed words come last.");
            assertEquals(ViewModels.ReviewTime.class, table.getColumnClass(5));
            assertEquals("2026-11-01 01:10:00", table.getValueAt(0, 5).toString());
            assertEquals("2026-11-01 01:50:00", table.getValueAt(2, 5).toString());
            assertEquals("", table.getValueAt(3, 5).toString());
            order.setSelectedItem("Word A-Z");
            order.setSelectedItem("Recently reviewed");
            assertEquals("alpha", table.getValueAt(0, 0));
            assertEquals("zulu", table.getValueAt(1, 0));
            return null;
        });
    }

    private static VocabularyWord word(String term) {
        return new VocabularyWord(term, term, "Review", "Meaning of " + term, "", "Test vocabulary");
    }

    private static Component find(Component component, String name) {
        Component result = findOrNull(component, name);
        assertNotNull(result, "Missing UI control: " + name);
        return result;
    }

    private static Component findOrNull(Component component, String name) {
        if (name.equals(component.getName())) {
            return component;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                Component result = findOrNull(child, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
