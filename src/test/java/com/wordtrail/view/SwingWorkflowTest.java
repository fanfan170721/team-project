package com.wordtrail.view;

import com.wordtrail.adapter.ViewModels;
import com.wordtrail.app.Application;
import com.wordtrail.usecase.port.DictionaryGateway;
import com.wordtrail.usecase.AppException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Clicks real Swing controls against real use cases and JSON persistence, without a display server. */
@Timeout(45)
class SwingWorkflowTest {
    @TempDir Path directory;
    private Application application;
    private AppPanel panel;
    private volatile String lastLookup;
    private volatile boolean failLookup;
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/New_York"));

    @BeforeEach void open() throws Exception { reopen(); }
    @AfterEach void close() throws Exception { if (application != null) { application.close(); } }

    private void reopen() throws Exception {
        application = new Application(directory, term -> {
            lastLookup = term;
            if (failLookup) { throw new AppException("Online lookup failed: HTTP 530. Your saved definition is unchanged."); }
            return new DictionaryGateway.Entry(term, "Definition from test dictionary.", "A test example.", "Local test server");
        }, clock);
        panel = edt(() -> { AppPanel root = new AppPanel(application.controller()); root.setSize(1080, 780); layout(root); return root; });
    }

    @Test void allSevenStoriesWorkThroughTheUIAndSurviveRestart() throws Exception {
        authenticate("admin", true);
        assertHomeTabs(true);
        assertTrue(edt(() -> allEnabled(component("library.topic", JComboBox.class))));
        snapshot("01-vocabulary");
        edt(() -> { Ui.selectTopic(topicSelector("library.topic"), "Nature"); return null; });
        idle();
        text("library.search", "climate"); click("library.find");
        assertEquals(1, edt(() -> component("library.table", JTable.class).getRowCount()));
        click("library.save");
        tab(3);
        assertEquals(1, edt(() -> component("review.table", JTable.class).getRowCount()));
        snapshot("02-review-list");
        tab(1);
        edt(() -> { component("flash.reviewOnly", JCheckBox.class).setSelected(true); return null; });
        click("flash.start");
        assertEquals("climate", edt(() -> component("flash.term", JLabel.class).getText()));
        assertFalse(edt(() -> component("flash.studied", JButton.class).isEnabled()));
        click("flash.reveal"); snapshot("03-flashcards"); click("flash.studied");
        tab(2);
        edt(() -> { component("quiz.count", JSpinner.class).setValue(1); return null; });
        click("quiz.start");
        click("quiz.submit");
        assertTrue(status().contains("Select one answer"));
        click("quiz.choice0"); click("quiz.submit");
        assertTrue(edt(() -> component("quiz.feedback", JTextArea.class).getText()).contains("Quiz complete"));
        assertFalse(edt(() -> component("quiz.submit", JButton.class).isEnabled()));
        tab(5);
        text("admin.term", "resilient"); text("admin.topic", "Mindset");
        click("admin.lookup");
        assertEquals("Definition from test dictionary.", edt(() -> component("admin.definition", JTextArea.class).getText()));
        text("admin.definition", "Able to recover after difficulty.");
        text("admin.example", "The resilient learner tried again.");
        text("admin.source", "WordTrail original demo content");
        snapshot("04-admin"); click("admin.addWord");
        edt(() -> {
            component("admin.tabs", JTabbedPane.class).setSelectedIndex(1);
            JComboBox<?> words = component("admin.linkedWord", JComboBox.class);
            for (int i = 0; i < words.getItemCount(); i++) {
                if (((ViewModels.Word) words.getItemAt(i)).term.equals("resilient")) { words.setSelectedIndex(i); break; }
            }
            return null;
        });
        text("admin.prompt", "What does resilient mean?");
        text("admin.option0", "Able to recover after difficulty.");
        text("admin.option1", "Likely to arrive late.");
        text("admin.option2", "Unable to speak clearly.");
        text("admin.option3", "Made of glass.");
        click("admin.addQuestion");
        tab(2);
        edt(() -> { Ui.selectTopic(topicSelector("quiz.topic"), "Mindset"); return null; });
        click("quiz.start");
        for (int i = 0; i < 4; i++) {
            final int option = i;
            if (edt(() -> component("quiz.choiceText" + option, JTextArea.class).getText()).startsWith("Able to recover")) {
                click("quiz.choice" + i); break;
            }
        }
        click("quiz.submit");
        assertTrue(edt(() -> component("quiz.feedback", JTextArea.class).getText()).contains("1 / 1 (100%)"));
        snapshot("05-quiz");
        tab(4);
        assertTrue(edt(() -> component("progress.summary", JTextArea.class).getText()).contains("Words studied: 1 / 17"));
        assertEquals(2, edt(() -> component("progress.history", JTable.class).getRowCount()));
        snapshot("06-progress");
        click("account.logout");
        edt(() -> { component("auth.mode", JComboBox.class).setSelectedIndex(1); return null; });
        authenticate("learner", true);
        assertHomeTabs(false);
        tab(3);
        assertEquals(0, edt(() -> component("review.table", JTable.class).getRowCount()));
        tab(4);
        assertEquals(0, edt(() -> component("progress.history", JTable.class).getRowCount()));
        application.close();
        reopen();
        authenticate("admin", false);
        tab(3);
        assertEquals(1, edt(() -> component("review.table", JTable.class).getRowCount()));
        tab(4);
        assertEquals(2, edt(() -> component("progress.history", JTable.class).getRowCount()));
        assertTrue(edt(() -> component("progress.summary", JTextArea.class).getText()).contains("Words studied: 1 / 17"));
    }

    @Test void mismatchedPasswordsShowAnErrorAndDoNotLeaveTheUIBusy() throws Exception {
        text("auth.username", "admin"); text("auth.password", "password123"); text("auth.confirm", "different");
        click("auth.submit");
        assertTrue(status().contains("do not match"));
        assertFalse(panel.isBusy());
        assertTrue(application.controller().needsSetup());
    }

    @Test void personalEditingReviewCountsSortingAndQuizWorkForLearnersAndPersist() throws Exception {
        authenticate("admin", true); click("account.logout");
        edt(() -> { component("auth.mode", JComboBox.class).setSelectedIndex(1); return null; });
        authenticate("alice", true);
        click("library.add");
        text("library.editor.term", "serendipity"); text("library.editor.topic", "My words");
        text("library.editor.definition", "A fortunate discovery.");
        text("library.editor.example", "A moment of serendipity.");
        snapshot("07-personal-editor"); click("library.editor.save");
        assertEquals(17, edt(() -> component("library.table", JTable.class).getRowCount()));
        text("library.search", "serendipity"); click("library.find");
        click("library.edit"); text("library.editor.definition", "My lucky discovery."); click("library.editor.save");
        text("library.search", "serendipity"); click("library.find");
        assertEquals("My lucky discovery.", edt(() -> component("library.table", JTable.class).getValueAt(0, 2)));
        click("library.save");
        tab(1);
        edt(() -> { component("flash.reviewOnly", JCheckBox.class).setSelected(true); return null; });
        click("flash.start"); click("flash.reveal"); click("flash.studied");
        assertFalse(edt(() -> component("flash.studied", JButton.class).isEnabled()));
        click("flash.next"); click("flash.reveal");
        assertTrue(edt(() -> component("flash.studied", JButton.class).isEnabled()));
        click("flash.studied");
        assertTrue(edt(() -> component("flash.counter", JLabel.class).getText()).contains("Reviews: 2"));
        tab(0); text("library.search", "climate"); click("library.find"); click("library.save");
        click("library.edit"); text("library.editor.definition", "My description of long-term weather.");
        click("library.editor.save");
        tab(3);
        assertEquals(2, edt(() -> component("review.table", JTable.class).getRowCount()));
        assertEquals("climate", edt(() -> component("review.table", JTable.class).getValueAt(0, 0)));
        edt(() -> { component("review.order", JComboBox.class).setSelectedIndex(2); return null; });
        assertEquals("serendipity", edt(() -> component("review.table", JTable.class).getValueAt(0, 0)));
        assertEquals(2, edt(() -> component("review.table", JTable.class).getValueAt(0, 4)));
        assertEquals(Integer.class, edt(() -> component("review.table", JTable.class).getColumnClass(4)));
        snapshot("08-review-counts");
        tab(2);
        edt(() -> {
            Ui.selectTopic(topicSelector("quiz.topic"), "My words");
            component("quiz.count", JSpinner.class).setValue(1); return null;
        });
        click("quiz.start");
        int correct = -1;
        for (int i = 0; i < 4; i++) {
            final int index = i;
            if (edt(() -> component("quiz.choiceText" + index, JTextArea.class).getText()).equals("My lucky discovery.")) { correct = i; }
        }
        assertTrue(correct >= 0); click("quiz.choice" + correct); click("quiz.submit");
        assertTrue(edt(() -> component("quiz.feedback", JTextArea.class).getText()).contains("1 / 1 (100%)"));
        application.close(); reopen(); authenticate("alice", false); tab(3);
        edt(() -> { component("review.order", JComboBox.class).setSelectedIndex(2); return null; });
        assertEquals("serendipity", edt(() -> component("review.table", JTable.class).getValueAt(0, 0)));
        assertEquals(2, edt(() -> component("review.table", JTable.class).getValueAt(0, 4)));
        click("account.logout"); authenticate("admin", false);
        text("library.search", "serendipity"); click("library.find");
        assertEquals(0, edt(() -> component("library.table", JTable.class).getRowCount()));
        text("library.search", "climate"); click("library.find");
        assertFalse(edt(() -> component("library.table", JTable.class).getValueAt(0, 2)).equals("My description of long-term weather."));
    }

    @Test void lookupUsesTheSelectedSortedRowAndFailuresLeaveControlsUsable() throws Exception {
        authenticate("admin", true);
        String word = edt(() -> {
            JTable table = component("library.table", JTable.class);
            table.getRowSorter().setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
            table.setRowSelectionInterval(0, 0);
            return (String) table.getValueAt(0, 0);
        });
        String saved = edt(() -> (String) component("library.table", JTable.class).getValueAt(0, 2));
        click("library.lookup");
        assertEquals(word, lastLookup);
        String online = edt(() -> component("library.detail", JTextArea.class).getText());
        assertTrue(online.contains(word + " — online definition"));
        assertTrue(online.contains("Definition from test dictionary."));
        assertTrue(online.contains("Your saved definition (unchanged):\n" + saved));
        snapshot("09-online-definition");
        failLookup = true; click("library.lookup");
        assertTrue(status().contains("HTTP 530"));
        assertTrue(edt(() -> component("library.lookup", JButton.class).isEnabled()));
        assertTrue(edt(() -> component("library.detail", JTextArea.class).getText()).contains(saved));
        assertFalse(edt(() -> component("library.detail", JTextArea.class).getText()).contains("Definition from test dictionary."));
        text("library.search", "no-such-word"); click("library.find");
        assertFalse(edt(() -> component("library.lookup", JButton.class).isEnabled()));
    }

    @Test void personalSaveFailuresPreserveInputAndSuccessSelectsTheSavedWord() throws Exception {
        authenticate("admin", true);
        click("library.add");
        text("library.editor.topic", "My words");
        text("library.editor.definition", "A fortunate discovery.");
        click("library.editor.save");
        assertTrue(status().contains("Word is required"));
        assertEquals("My words", edt(() -> component("library.editor.topic", JTextField.class).getText()));
        assertEquals("A fortunate discovery.",
                edt(() -> component("library.editor.definition", JTextArea.class).getText()));
        assertTrue(edt(() -> visibleInTree(component("library.editor.save", JButton.class))));
        assertEquals(16, edt(() -> component("library.table", JTable.class).getRowCount()));

        text("library.editor.term", "serendipity");
        click("library.editor.save");
        assertEquals("Added \"serendipity\" to your vocabulary.", status());
        assertEquals("serendipity", edt(() -> {
            JTable table = component("library.table", JTable.class);
            return table.getValueAt(table.getSelectedRow(), 0);
        }));
        assertTrue(edt(() -> {
            JTable table = component("library.table", JTable.class);
            return table.getVisibleRect().intersects(table.getCellRect(table.getSelectedRow(), 0, true));
        }), "The saved word must be scrolled into view.");
        click("library.edit");
        text("library.editor.definition", " ");
        click("library.editor.save");
        assertTrue(status().contains("Definition is required"));
        assertEquals(" ", edt(() -> component("library.editor.definition", JTextArea.class).getText()));
        assertTrue(edt(() -> visibleInTree(component("library.editor.save", JButton.class))));
        assertEquals("A fortunate discovery.", application.controller().browse(null, "serendipity").words.get(0).definition);

        text("library.editor.definition", "My own lucky discovery.");
        click("library.editor.save");
        assertEquals("Updated your definition of \"serendipity\".", status());
        assertEquals("My own lucky discovery.", application.controller().browse(null, "serendipity").words.get(0).definition);
        snapshot("10-personal-save-result");
    }

    private boolean visibleInTree(Component component) {
        for (Component current = component; current != null; current = current.getParent()) {
            if (!current.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private void authenticate(String username, boolean creating) throws Exception {
        text("auth.username", username); text("auth.password", "password123");
        if (creating) { text("auth.confirm", "password123"); }
        click("auth.submit");
        assertNotNull(edt(() -> component("home.tabs", JTabbedPane.class)), status());
    }

    /** Existing tabs keep their positions; Admin only for administrators; other features may append tabs. */
    private void assertHomeTabs(boolean admin) throws Exception {
        java.util.List<String> expected = java.util.List.of("Vocabulary", "Flashcards", "Quiz", "Review later", "Progress");
        edt(() -> {
            JTabbedPane tabs = component("home.tabs", JTabbedPane.class);
            for (int i = 0; i < expected.size(); i++) { assertEquals(expected.get(i), tabs.getTitleAt(i)); }
            assertEquals(admin ? 5 : -1, tabs.indexOfTab("Admin"));
            assertTrue(tabs.indexOfTab("Export results") > tabs.indexOfTab("Progress"));
            return null;
        });
    }

    private void tab(int index) throws Exception {
        edt(() -> { component("home.tabs", JTabbedPane.class).setSelectedIndex(index); return null; }); idle();
    }

    private void text(String name, String value) throws Exception {
        edt(() -> {
            Component control = component(name, Component.class);
            if (control instanceof JTextField) { ((JTextField) control).setText(value); }
            else { ((JTextArea) control).setText(value); }
            return null;
        });
    }

    private void click(String name) throws Exception {
        edt(() -> { AbstractButton button = component(name, AbstractButton.class); assertTrue(button.isEnabled(), name); button.doClick(0); return null; }); idle();
    }

    private String status() throws Exception { return edt(() -> component("app.status", JTextArea.class).getText()); }

    private void idle() throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (edt(() -> {
                if (panel.isBusy()) { return false; }
                // No native window performs validation for this headless component tree.
                layout(panel);
                return true;
            })) { return; }
            Thread.sleep(15);
        }
        fail("UI did not become idle: " + status());
    }

    private <T extends Component> T component(String name, Class<T> type) {
        Component found = find(panel, name);
        assertNotNull(found, "Missing control: " + name);
        return type.cast(found);
    }

    private JComboBox<Ui.TopicOption> topicSelector(String name) {
        JComboBox<?> selector = component(name, JComboBox.class);
        for (int index = 0; index < selector.getItemCount(); index++) {
            assertTrue(selector.getItemAt(index) instanceof Ui.TopicOption, name);
        }
        @SuppressWarnings("unchecked")
        JComboBox<Ui.TopicOption> typed = (JComboBox<Ui.TopicOption>) selector;
        return typed;
    }

    private static Component find(Component current, String name) {
        if (name.equals(current.getName())) { return current; }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) {
                Component result = find(child, name);
                if (result != null) { return result; }
            }
        }
        return null;
    }

    private static boolean allEnabled(Component current) {
        if (!current.isEnabled()) { return false; }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) { if (!allEnabled(child)) { return false; } }
        }
        return true;
    }

    private void snapshot(String name) throws Exception {
        Path output = Path.of(System.getProperty("wordtrail.uiSnapshots", "target/ui-snapshots"), name + ".png");
        Files.createDirectories(output.getParent());
        edt(() -> {
            layout(panel);
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            panel.printAll(graphics); graphics.dispose();
            ImageIO.write(image, "png", output.toFile()); return null;
        });
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
