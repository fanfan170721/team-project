package com.wordtrail.view;

import com.wordtrail.adapter.StudyViewModel;
import com.wordtrail.app.Application;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(90)
class StudyPanelWorkflowTest {
    @TempDir Path directory;
    private Application app;
    private AppPanel panel;
    private final LookAndFeel previousLookAndFeel = UIManager.getLookAndFeel();

    @AfterEach void close() throws Exception {
        if (app != null) { app.close(); }
        edt(() -> { UIManager.setLookAndFeel(previousLookAndFeel); return null; });
    }

    private void open(int width, int height, boolean setup) throws Exception {
        app = new Application(directory, term -> { throw new AssertionError("No network in UI tests."); },
                Clock.systemUTC());
        if (setup) { app.controller().setup("allen", "password123".toCharArray()); app.controller().logout(); }
        panel = edt(() -> {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            AppPanel root = new AppPanel(app.controller());
            root.setSize(width, height);
            return root;
        });
        edt(() -> {
            ((JTextField) find("auth.username")).setText("allen");
            ((JPasswordField) find("auth.password")).setText("password123");
            return null;
        });
        click("auth.submit");
        edt(() -> {
            JTabbedPane tabs = (JTabbedPane) find("home.tabs");
            tabs.setSelectedIndex(tabs.indexOfTab("Study"));
            return null;
        });
        idle();
    }

    @ParameterizedTest
    @CsvSource({"934,660", "1080,780"})
    void fullRoundUnknownRetryReopenAndIntegrationRemainUsable(int width, int height) throws Exception {
        open(width, height, true);
        click("study.start");
        assertEquals(20, edt(() -> ((JTable) find("study.overview")).getRowCount()));
        edt(() -> { ((JTable) find("study.overview")).setValueAt(true, 0, 0); return null; });
        idle();
        snapshot("guided-overview-" + width);
        click("study.advance");
        readable("study.meaning");
        snapshot("guided-learning-" + width);
        while (edt(() -> find("study.advance")) != null) { click("study.advance"); }
        assertVisible("study.submit", "study.unknown", "study.start", "study.assess", "study.goal.save");
        snapshot("guided-test-" + width);
        for (int index = 0; index < 4; index++) {
            readable("study.choiceText" + index);
            assertVisible("study.choice" + index);
        }
        snapshot("guided-test-" + width);
        click("study.submit");
        assertTrue(edt(() -> ((JTextArea) find("app.status")).getText()).contains("Select an answer"));
        click("study.unknown");
        String token = app.controller().study().show().round.token;
        app.close();
        app = null;
        open(width, height, false);
        assertEquals(token, app.controller().study().show().round.token);
        while (edt(() -> find("study.submit")) != null) { correct(); }
        assertTrue(app.controller().study().show().round.phase.equals("REVIEW"));
        click("study.advance");
        correct();
        StudyViewModel done = app.controller().study().show();
        assertEquals("COMPLETED", done.round.phase);
        assertTrue(done.summary.contains("Mastered: 20"));
        assertTrue(done.summary.contains("Net-new learned: 19"));
        assertTrue(done.mistakes.contains("Unknown: 1"));
        assertTrue(done.mistakes.contains("Retests: 1"));
        assertEquals(1, app.controller().dashboard().quizRows.size());
        assertEquals(36, app.controller().browse(null, "").words.size());
        Path csv = directory.resolve("guided.csv");
        assertTrue(app.controller().exportResults(csv.toString()).success);
        assertTrue(Files.readString(csv).contains("Guided study L1"));
        snapshot("guided-complete-" + width);
        click("study.start");
        String original = app.controller().study().show().round.token;
        click("study.assess");
        for (int index = 0; index < 20; index++) { click("study.unknown"); }
        assertVisible("study.adopt", "study.start", "study.goal.save");
        click("study.adopt");
        assertEquals(20, edt(() -> ((JTable) find("study.overview")).getRowCount()));
        assertNotEquals(original, app.controller().study().show().round.token);
        assertTrue(app.controller().study().show().summary.contains("Mastered: 20"));
        snapshot("guided-reassessed-" + width);
    }

    private void correct() throws Exception {
        StudyViewModel.RoundView round = app.controller().study().show().round;
        click("study.choice" + round.options.indexOf(round.definition));
        click("study.submit");
    }

    private void readable(String name) throws Exception {
        edt(() -> {
            JTextArea text = (JTextArea) find(name);
            JViewport viewport = (JViewport) text.getParent();
            assertTrue(viewport.getExtentSize().height >= text.getFontMetrics(text.getFont()).getHeight(),
                    name + " viewport=" + viewport.getExtentSize() + " line=" + text.getFontMetrics(text.getFont()).getHeight());
            assertTrue(viewport.getExtentSize().width >= 150, name);
            return null;
        });
    }

    private void assertVisible(String... names) throws Exception {
        edt(() -> {
            for (String name : names) {
                Component control = find(name);
                assertNotNull(control, name);
                Rectangle bounds = new Rectangle(0, 0, control.getWidth(), control.getHeight());
                assertTrue(bounds.width > 0 && bounds.height > 0, name);
                for (Container ancestor = control.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
                    Rectangle relative = SwingUtilities.convertRectangle(control, bounds, ancestor);
                    assertTrue(new Rectangle(0, 0, ancestor.getWidth(), ancestor.getHeight()).contains(relative), name);
                }
            }
            return null;
        });
    }

    private void click(String name) throws Exception {
        edt(() -> {
            AbstractButton button = (AbstractButton) find(name);
            assertNotNull(button, name);
            assertTrue(button.isEnabled(), name);
            button.doClick(0);
            return null;
        });
        idle();
    }

    private Component find(String name) { return find(panel, name); }
    private Component find(Component current, String name) {
        if (name.equals(current.getName())) { return current; }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) {
                Component found = find(child, name);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    private void idle() throws Exception {
        for (int attempt = 0; attempt < 1000; attempt++) {
            if (edt(() -> {
                if (panel.isBusy()) { return false; }
                layout(panel);
                layout(panel);
                return true;
            })) { return; }
            Thread.sleep(10);
        }
        fail("UI did not finish its operation.");
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) {
            if (child instanceof Container) { layout((Container) child); }
        }
    }

    private void snapshot(String name) throws Exception {
        Path output = Path.of("target/ui-snapshots", name + ".png");
        Files.createDirectories(output.getParent());
        edt(() -> {
            layout(panel);
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            panel.printAll(graphics);
            graphics.dispose();
            ImageIO.write(image, "png", output.toFile());
            return null;
        });
    }

    private static <T> T edt(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
