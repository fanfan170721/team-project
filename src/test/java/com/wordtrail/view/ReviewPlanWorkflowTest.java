package com.wordtrail.view;

import com.wordtrail.app.Application;
import com.wordtrail.data.JsonFileStore;
import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizAttempt;
import com.wordtrail.entity.VocabularyWord;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Timeout(45)
class ReviewPlanWorkflowTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-18T01:00:00Z"), ZoneId.of("America/Toronto"));
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final String PASSWORD = "test-password";
    private static final String NAMED_TOPIC = "All topics";
    private static final String PRIVATE_TOPIC = "Private notes";
    private static final String PRIVATE_TERM = "private beacon";
    private static final String PRIVATE_DEFINITION = "A private reference signal.";
    private static final String PUBLIC_DEFINITION = "A fixed navigation reference.";
    private static final String ALICE_DEFINITION = "Alice's route marker.";

    @TempDir
    Path directory;

    private Application application;
    private AppPanel panel;

    @BeforeEach
    void openApplicationWithSavedHistory() throws Exception {
        try (Application setup = openApplication()) {
            setup.controller().setup("admin", PASSWORD.toCharArray());
            setup.controller().register("alice", PASSWORD.toCharArray());
            setup.controller().register("bob", PASSWORD.toCharArray());
        }

        VocabularyWord anchor = word("anchor", "alpha anchor", NAMED_TOPIC, PUBLIC_DEFINITION);
        VocabularyWord marker = word("marker", "zulu marker", NAMED_TOPIC, "A visible guide mark.");
        VocabularyWord fragment = word("fragment", "map fragment", NAMED_TOPIC, "Part of a route map.");
        VocabularyWord waiting = word("waiting", "waiting token", "Waiting words", "A reminder for later.");
        VocabularyWord personal = word("private", PRIVATE_TERM, PRIVATE_TOPIC, PRIVATE_DEFINITION);
        try (JsonFileStore store = new JsonFileStore(directory)) {
            for (VocabularyWord shared : List.of(anchor, marker, fragment, waiting)) {
                store.addWord(shared);
            }
            LearningProgress history = LearningProgress.empty()
                    .withPersonalWord(personal)
                    .withDefinition(anchor.getId(), ALICE_DEFINITION)
                    .withCompletedReview(anchor.getId(), TODAY.minusDays(1), Instant.parse("2026-11-17T00:00:00Z"))
                    .withCompletedReview(fragment.getId(), TODAY.minusDays(3), Instant.parse("2026-11-14T17:00:00Z"))
                    .withCompletedReview(fragment.getId(), TODAY.minusDays(2), Instant.parse("2026-11-15T17:00:00Z"))
                    .withCompletedReview(waiting.getId(), TODAY, CLOCK.instant())
                    .withAttempt(new QuizAttempt(CLOCK.instant(), NAMED_TOPIC, 1, 2));
            store.saveProgress("alice", history);
        }
        application = openApplication();
        panel = onEdt(() -> new AppPanel(application.controller()));
    }

    @AfterEach
    void closeApplication() throws Exception {
        if (application != null) {
            application.close();
        }
    }

    @Test
    void generatesTheDuePlanWithTopicAndQuantityControlsWithoutWritingProgress() throws Exception {
        login("alice");
        selectTab("Review plan");
        assertTrue(rows().isEmpty());
        assertNull(selectedPlanTopic());
        setLimit(20);
        Path savedData = directory.resolve("wordtrail.json");
        byte[] beforeGeneration = Files.readAllBytes(savedData);

        click("reviewPlan.generate");
        List<List<String>> allTopics = rows();
        assertTrue(terms(allTopics).containsAll(List.of("zulu marker", "alpha anchor", PRIVATE_TERM)));
        assertFalse(terms(allTopics).contains("map fragment"),
                "A second review on November 15 makes it due on November 18.");
        assertFalse(terms(allTopics).contains("waiting token"));
        click("reviewPlan.generate");
        assertEquals(allTopics, rows());
        assertArrayEquals(beforeGeneration, Files.readAllBytes(savedData),
                "Generating twice must preserve saved review dates, counts, scores, and all other progress.");

        selectPlanTopic(NAMED_TOPIC);
        assertEquals(NAMED_TOPIC, selectedPlanTopic());
        click("reviewPlan.generate");
        assertEquals(List.of("Word", "Definition", "Due date", "Reason"), columnNames());
        List<List<String>> namedTopic = rows();
        assertEquals(List.of("zulu marker", "alpha anchor"), terms(namedTopic),
                "The new word must precede the reviewed word despite their alphabetical order.");
        assertEquals(List.of("zulu marker", "A visible guide mark.", TODAY.toString()),
                namedTopic.get(0).subList(0, 3));
        assertTrue(namedTopic.get(0).get(3).contains("Eligible today"));
        assertEquals(List.of("alpha anchor", ALICE_DEFINITION, TODAY.toString()), namedTopic.get(1).subList(0, 3));
        assertTrue(namedTopic.get(1).get(3).contains("1 day after"));

        setLimit(1);
        click("reviewPlan.generate");
        assertEquals(List.of("zulu marker"), terms(rows()));
        selectPlanTopic("Waiting words");
        click("reviewPlan.generate");
        assertTrue(rows().isEmpty());
        assertTrue(onEdt(() -> control("reviewPlan.message", JLabel.class).getText()).contains("No due words"));
    }

    @Test
    void regeneratesWithUpdatedPersonalDefinitionAndClearsThePlanBetweenAccounts() throws Exception {
        login("alice");
        selectTab("Review plan");
        selectPlanTopic(PRIVATE_TOPIC);
        click("reviewPlan.generate");
        assertEquals(List.of(PRIVATE_TERM), terms(rows()));
        assertEquals(PRIVATE_DEFINITION, rows().get(0).get(1));

        selectTab("Vocabulary");
        SwingUtilities.invokeAndWait(() -> control("library.search", JTextComponent.class).setText(PRIVATE_TERM));
        click("library.find");
        assertEquals(PRIVATE_TERM, onEdt(() -> control("library.table", JTable.class).getValueAt(0, 0)));
        click("library.edit");
        String revisedDefinition = "My revised meaning for this beacon.";
        SwingUtilities.invokeAndWait(() ->
                control("library.editor.definition", JTextComponent.class).setText(revisedDefinition));
        click("library.editor.save");

        selectTab("Review plan");
        assertTrue(rows().isEmpty());
        assertEquals(PRIVATE_TOPIC, selectedPlanTopic());
        click("reviewPlan.generate");
        assertEquals(List.of(PRIVATE_TERM), terms(rows()));
        assertEquals(revisedDefinition, rows().get(0).get(1));

        click("account.logout");
        login("bob");
        selectTab("Review plan");
        assertTrue(rows().isEmpty());
        assertNull(selectedPlanTopic());
        selectPlanTopic(PRIVATE_TOPIC);
        assertNull(selectedPlanTopic(), "Another learner's private topic must be absent.");
        setLimit(20);
        click("reviewPlan.generate");
        List<List<String>> bobPlan = rows();
        assertTrue(terms(bobPlan).contains("map fragment"), "Bob must see his own review history.");
        assertFalse(terms(bobPlan).contains(PRIVATE_TERM));
        assertEquals(PUBLIC_DEFINITION, bobPlan.stream()
                .filter(row -> "alpha anchor".equals(row.get(0)))
                .findFirst().orElseThrow().get(1));
    }

    private Application openApplication() {
        return new Application(directory, term -> {
            throw new AssertionError("Review planning must not look up a word online.");
        }, CLOCK);
    }

    private static VocabularyWord word(String id, String term, String topic, String definition) {
        return new VocabularyWord("review-workflow-" + id, term, topic, definition, "", "Workflow test fixture");
    }

    private void login(String username) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            control("auth.username", JTextComponent.class).setText(username);
            control("auth.password", JTextComponent.class).setText(PASSWORD);
        });
        click("auth.submit");
        onEdt(() -> control("home.tabs", JTabbedPane.class));
    }

    private void selectTab(String title) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = control("home.tabs", JTabbedPane.class);
            int selected = tabs.indexOfTab(title);
            assertTrue(selected >= 0, "Missing tab " + title);
            tabs.setSelectedIndex(selected);
        });
        awaitIdle();
    }

    private void selectPlanTopic(String topic) throws Exception {
        SwingUtilities.invokeAndWait(() -> Ui.selectTopic(control("reviewPlan.topic", JComboBox.class), topic));
        awaitIdle();
    }

    @SuppressWarnings("unchecked")
    private String selectedPlanTopic() throws Exception {
        return onEdt(() -> Ui.topic(control("reviewPlan.topic", JComboBox.class)));
    }

    private void setLimit(int limit) throws Exception {
        SwingUtilities.invokeAndWait(() -> control("reviewPlan.limit", JSpinner.class).setValue(limit));
    }

    private List<String> columnNames() throws Exception {
        return onEdt(() -> {
            JTable table = control("reviewPlan.table", JTable.class);
            List<String> names = new ArrayList<>();
            for (int column = 0; column < table.getColumnCount(); column++) {
                names.add(table.getColumnName(column));
            }
            return names;
        });
    }

    private List<List<String>> rows() throws Exception {
        return onEdt(() -> {
            JTable table = control("reviewPlan.table", JTable.class);
            List<List<String>> rows = new ArrayList<>();
            for (int row = 0; row < table.getRowCount(); row++) {
                List<String> cells = new ArrayList<>();
                for (int column = 0; column < table.getColumnCount(); column++) {
                    cells.add((String) table.getValueAt(row, column));
                }
                rows.add(cells);
            }
            return rows;
        });
    }

    private static List<String> terms(List<List<String>> rows) {
        List<String> words = new ArrayList<>();
        for (List<String> row : rows) {
            words.add(row.get(0));
        }
        return words;
    }

    private void click(String name) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton button = control(name, JButton.class);
            assertTrue(button.isEnabled(), "Expected enabled control " + name);
            button.doClick(0);
        });
        awaitIdle();
    }

    private void awaitIdle() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (onEdt(() -> !panel.isBusy())) {
                return;
            }
            Thread.sleep(15);
        }
        fail("UI did not become idle. " + onEdt(() -> control("app.status", JTextArea.class).getText()));
    }

    private <T extends Component> T control(String name, Class<T> type) {
        Component found = find(panel, name);
        assertNotNull(found, "Missing control " + name);
        return type.cast(found);
    }

    private static Component find(Component current, String name) {
        if (name.equals(current.getName())) {
            return current;
        }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) {
                Component match = find(child, name);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
