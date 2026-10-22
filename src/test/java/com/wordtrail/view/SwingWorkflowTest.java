package com.wordtrail.view;

import com.wordtrail.adapter.ViewModels;
import com.wordtrail.app.Application;
import com.wordtrail.usecase.port.DictionaryGateway;
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
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/New_York"));

    @BeforeEach void open() throws Exception { reopen(); }
    @AfterEach void close() throws Exception { if (application != null) { application.close(); } }

    private void reopen() throws Exception {
        application = new Application(directory,
                term -> new DictionaryGateway.Entry(term, "Definition from test dictionary.", "A test example.", "Local test server"), clock);
        panel = edt(() -> { AppPanel root = new AppPanel(application.controller()); root.setSize(1080, 780); layout(root); return root; });
    }

    @Test void allSevenStoriesWorkThroughTheUIAndSurviveRestart() throws Exception {
        authenticate("admin", true);
        assertEquals(6, edt(() -> component("home.tabs", JTabbedPane.class).getTabCount()));
        assertTrue(edt(() -> allEnabled(component("library.topic", JComboBox.class))));
        snapshot("01-vocabulary");
        edt(() -> { component("library.topic", JComboBox.class).setSelectedItem("Nature"); return null; });
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
        edt(() -> { component("quiz.topic", JComboBox.class).setSelectedItem("Mindset"); return null; });
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
        assertEquals(5, edt(() -> component("home.tabs", JTabbedPane.class).getTabCount()));
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

    private void authenticate(String username, boolean creating) throws Exception {
        text("auth.username", username); text("auth.password", "password123");
        if (creating) { text("auth.confirm", "password123"); }
        click("auth.submit");
        assertNotNull(edt(() -> component("home.tabs", JTabbedPane.class)), status());
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
            if (edt(() -> !panel.isBusy())) { return; }
            Thread.sleep(15);
        }
        fail("UI did not become idle: " + status());
    }

    private <T extends Component> T component(String name, Class<T> type) {
        Component found = find(panel, name);
        assertNotNull(found, "Missing control: " + name);
        return type.cast(found);
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
        Path output = Path.of("target", "ui-snapshots", name + ".png");
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
