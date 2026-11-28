package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.app.Application;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Exercises long learning content with real controllers, offline data, and no native windows. */
@Timeout(45)
class LearningScreenLayoutTest {
    private static final String LONG_TERM = "W".repeat(80);
    private static final String LONG_TOPIC = "W".repeat(50);
    private static final String DEFINITION = longText("The full definition", 1800);
    private static final String PROMPT = longText("Explain " + LONG_TERM, 980);
    private static final String CORRECT = longText("The correct answer", 1800);
    private static final List<String> OPTIONS = List.of(CORRECT,
            longText("Alternative A", 1800), longText("Alternative B", 1800),
            longText("Alternative C", 1800));

    @TempDir Path directory;
    private Application application;
    private AppController controller;
    private AppPanel panel;
    private LookAndFeel originalLookAndFeel;

    @BeforeEach void open() throws Exception {
        originalLookAndFeel = edt(() -> {
            LookAndFeel original = UIManager.getLookAndFeel();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return original;
        });
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T16:00:00Z"), ZoneId.of("America/New_York"));
        application = new Application(directory, term -> {
            throw new AssertionError("Layout tests must never contact a dictionary.");
        }, clock);
        controller = application.controller();
        controller.setup("admin", "password123".toCharArray());
        controller.addWord(LONG_TERM, LONG_TOPIC, DEFINITION, "A complete example.", "Offline test fixture");
        String wordId = controller.browse(LONG_TOPIC, "").words.get(0).id;
        controller.addQuestion(wordId, PROMPT, OPTIONS, 0);
        controller.addQuestion(wordId, PROMPT + " Next.", OPTIONS, 0);
        controller.logout();
        // Set the system look and feel before constructing any application components.
        panel = edt(() -> new AppPanel(controller));
    }

    @AfterEach void close() throws Exception {
        if (application != null) {
            application.close();
        }
        if (originalLookAndFeel != null) {
            edt(() -> {
                UIManager.setLookAndFeel(originalLookAndFeel);
                return null;
            });
        }
    }

    @ParameterizedTest(name = "Flashcards remain usable at {0} x {1}")
    @CsvSource({"934, 660", "1080, 780"})
    void longFlashcardsKeepFullContentAndReviewControlsAvailable(int width, int height) throws Exception {
        login(width, height);
        tab("Flashcards");
        selectTopic("flash.topic");
        assertEnabled("flash.reveal", false);
        assertEnabled("flash.studied", false);
        assertEnabled("flash.next", false);
        assertEnabled("flash.save", false);
        assertVisible("flash.topic", "flash.reviewOnly", "flash.start");

        click("flash.start");
        assertEquals(LONG_TERM, edt(() -> component("flash.term", JLabel.class).getText()));
        assertTrue(edt(() -> component("flash.term", JLabel.class).getToolTipText()).contains(LONG_TERM));
        assertEnabled("flash.reveal", true);
        assertEnabled("flash.studied", false);
        assertEnabled("flash.next", true);
        assertEnabled("flash.save", true);
        assertVisible("flash.reveal", "flash.studied", "flash.next", "flash.save");
        assertFalse(edt(() -> component("flash.definition", JTextArea.class).getText()).contains(DEFINITION),
                "The definition must remain hidden until Reveal is selected.");

        click("flash.reveal");
        String displayed = edt(() -> component("flash.definition", JTextArea.class).getText());
        assertTrue(displayed.contains(DEFINITION));
        assertTrue(displayed.contains(LONG_TERM), "The complete term must also be selectable text.");
        assertTrue(displayed.contains(LONG_TOPIC), "The complete topic must remain readable.");
        assertScrollableText("flash.definition");
        assertEnabled("flash.reveal", false);
        assertEnabled("flash.studied", true);
        snapshot("20-flashcards-long-content-" + width);

        click("flash.save");
        assertEquals(1, controller.reviewList().words.size());
        assertEquals("Remove from review later", edt(() -> component("flash.save", JButton.class).getText()));
        click("flash.studied");
        assertEnabled("flash.studied", false);
        assertTrue(edt(() -> component("flash.counter", JLabel.class).getText()).contains("Reviews: 1"));
        click("flash.next");
        assertEnabled("flash.reveal", true);
        assertEnabled("flash.studied", false);
        assertEquals("Remove from review later", edt(() -> component("flash.save", JButton.class).getText()));
        click("flash.reveal");
        assertEnabled("flash.studied", true);
        click("flash.save");
        assertTrue(controller.reviewList().words.isEmpty());
        assertVisible("flash.start", "flash.reveal", "flash.studied", "flash.next", "flash.save");
    }

    @ParameterizedTest(name = "Quiz remains readable after feedback at {0} x {1}")
    @CsvSource({"934, 660", "1080, 780"})
    void longQuizContentRemainsScrollableAfterAnsweringAndNextStillWorks(int width, int height) throws Exception {
        login(width, height);
        tab("Quiz");
        selectTopic("quiz.topic");
        edt(() -> {
            component("quiz.count", JSpinner.class).setValue(2);
            return null;
        });
        assertEnabled("quiz.submit", false);
        assertEnabled("quiz.next", false);
        assertEnabled("quiz.save", false);
        assertVisible("quiz.topic", "quiz.count", "quiz.start");
        click("quiz.start");
        assertLongQuestionContent();
        assertEnabled("quiz.submit", true);
        assertEnabled("quiz.next", false);
        assertEnabled("quiz.save", true);
        assertVisible("quiz.submit", "quiz.next", "quiz.save");
        snapshot("21-quiz-long-content-" + width);

        chooseAnswer(false);
        click("quiz.submit");
        assertEquals("Correct answer: " + CORRECT,
                edt(() -> component("quiz.feedback", JTextArea.class).getText()));
        assertEnabled("quiz.submit", false);
        assertEnabled("quiz.next", true);
        for (int index = 0; index < 4; index++) {
            assertEnabled("quiz.choice" + index, false);
        }
        assertLongQuestionContent();
        assertScrollableText("quiz.feedback");
        assertVisible("quiz.start", "quiz.submit", "quiz.next", "quiz.save");
        snapshot("22-quiz-long-feedback-" + width);
        click("quiz.save");
        assertEquals(1, controller.reviewList().words.size());

        click("quiz.next");
        assertEnabled("quiz.submit", true);
        assertEnabled("quiz.next", false);
        for (int index = 0; index < 4; index++) {
            assertEnabled("quiz.choice" + index, true);
        }
        assertEquals("", edt(() -> component("quiz.feedback", JTextArea.class).getText()));
        chooseAnswer(true);
        click("quiz.submit");
        assertTrue(edt(() -> component("quiz.feedback", JTextArea.class).getText()).contains("Quiz complete: 1 / 2"));
        assertEnabled("quiz.submit", false);
        assertEnabled("quiz.next", false);
        assertEnabled("quiz.save", true);
        assertLongQuestionContent();
        assertVisible("quiz.start", "quiz.submit", "quiz.next", "quiz.save");
    }

    private void assertLongQuestionContent() throws Exception {
        String prompt = edt(() -> component("quiz.prompt", JTextArea.class).getText());
        assertTrue(prompt.equals(PROMPT) || prompt.equals(PROMPT + " Next."));
        assertScrollableText("quiz.prompt");
        for (int index = 0; index < 4; index++) {
            final int option = index;
            String answer = edt(() -> component("quiz.choiceText" + option, JTextArea.class).getText());
            assertTrue(OPTIONS.contains(answer),
                    "Answer choices must retain their full text after rendering.");
            assertEquals(answer, edt(() -> component("quiz.choice" + option, JRadioButton.class)
                    .getAccessibleContext().getAccessibleDescription()));
            assertScrollableText("quiz.choiceText" + index);
            assertVisible("quiz.choice" + index);
        }
    }

    private void assertScrollableText(String name) throws Exception {
        edt(() -> {
            JTextArea text = component(name, JTextArea.class);
            assertFalse(text.isEditable(), name);
            assertTrue(text.getLineWrap(), name + " must wrap long text.");
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, text);
            assertNotNull(scroll, name + " needs a way to reach text outside its visible area.");
            assertFullyVisible(scroll);
            JViewport viewport = scroll.getViewport();
            int lineHeight = text.getFontMetrics(text.getFont()).getHeight();
            assertTrue(viewport.getExtentSize().height >= lineHeight,
                    name + " must show at least one complete line, even with feedback visible.");
            assertTrue(viewport.getExtentSize().width
                    >= text.getFontMetrics(text.getFont()).stringWidth("A readable answer"),
                    name + " must remain wide enough to read.");
            Rectangle lastCharacter = text.modelToView2D(text.getDocument().getLength() - 1).getBounds();
            text.scrollRectToVisible(lastCharacter);
            assertTrue(viewport.getViewRect().intersects(lastCharacter), name + " must scroll to the final character: "
                    + "character=" + lastCharacter + ", viewport=" + viewport.getViewRect()
                    + ", view=" + text.getSize() + ", preferred=" + text.getPreferredSize());
            assertTrue(viewport.getViewPosition().y > 0, name + " long content must scroll beyond its first lines.");
            text.scrollRectToVisible(new Rectangle(0, 0, 1, lineHeight));
            return null;
        });
    }

    private void chooseAnswer(boolean correct) throws Exception {
        int index = edt(() -> {
            for (int option = 0; option < 4; option++) {
                if (component("quiz.choiceText" + option, JTextArea.class).getText().equals(CORRECT) == correct) {
                    return option;
                }
            }
            throw new AssertionError("Expected answer choice is missing.");
        });
        click("quiz.choice" + index);
    }

    private void login(int width, int height) throws Exception {
        edt(() -> {
            panel.setSize(width, height);
            component("auth.username", JTextField.class).setText("admin");
            component("auth.password", JPasswordField.class).setText("password123");
            layout(panel);
            return null;
        });
        click("auth.submit");
        assertNotNull(edt(() -> component("home.tabs", JTabbedPane.class)));
    }

    private void tab(String title) throws Exception {
        edt(() -> {
            JTabbedPane tabs = component("home.tabs", JTabbedPane.class);
            tabs.setSelectedIndex(tabs.indexOfTab(title));
            return null;
        });
        idle();
    }

    private void selectTopic(String name) throws Exception {
        edt(() -> {
            @SuppressWarnings("unchecked")
            JComboBox<Ui.TopicOption> selector = (JComboBox<Ui.TopicOption>) component(name, JComboBox.class);
            Ui.selectTopic(selector, LONG_TOPIC);
            assertEquals(LONG_TOPIC, Ui.topic(selector));
            return null;
        });
        idle();
    }

    private void assertEnabled(String name, boolean expected) throws Exception {
        assertEquals(expected, edt(() -> component(name, JComponent.class).isEnabled()), name);
    }

    private void assertVisible(String... names) throws Exception {
        edt(() -> {
            for (String name : names) {
                assertFullyVisible(component(name, JComponent.class));
            }
            return null;
        });
    }

    private void assertFullyVisible(JComponent control) {
        String name = control.getName() == null ? control.getClass().getSimpleName() : control.getName();
        assertTrue(control.getWidth() > 0 && control.getHeight() > 0, name + " must have usable bounds.");
        Rectangle bounds = new Rectangle(0, 0, control.getWidth(), control.getHeight());
        for (Container parent = control.getParent(); parent != null; parent = parent.getParent()) {
            assertTrue(parent.isVisible(), name + " must have visible ancestors.");
            Rectangle relative = SwingUtilities.convertRectangle(control, bounds, parent);
            assertTrue(new Rectangle(0, 0, parent.getWidth(), parent.getHeight()).contains(relative),
                    name + " is clipped by " + parent.getClass().getSimpleName() + ": " + relative);
        }
        assertTrue(control.isVisible(), name);
    }

    private void click(String name) throws Exception {
        edt(() -> {
            AbstractButton button = component(name, AbstractButton.class);
            assertTrue(button.isEnabled(), name);
            button.doClick(0);
            return null;
        });
        idle();
    }

    private void idle() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (edt(() -> {
                if (panel.isBusy()) {
                    return false;
                }
                layout(panel);
                // Scrollbars change wrapping width; a native validation cycle would revisit the new text height.
                layout(panel);
                return true;
            })) {
                return;
            }
            Thread.sleep(15);
        }
        fail("UI did not become idle.");
    }

    private <T extends Component> T component(String name, Class<T> type) {
        Component found = find(panel, name);
        assertNotNull(found, "Missing control: " + name);
        return type.cast(found);
    }

    private static Component find(Component current, String name) {
        if (name.equals(current.getName())) {
            return current;
        }
        if (current instanceof Container) {
            for (Component child : ((Container) current).getComponents()) {
                Component found = find(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void snapshot(String name) throws Exception {
        Path output = Path.of(System.getProperty("wordtrail.uiSnapshots", "target/ui-snapshots"), name + ".png");
        Files.createDirectories(output.getParent());
        edt(() -> {
            layout(panel);
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            panel.printAll(graphics);
            graphics.dispose();
            ImageIO.write(image, "png", output.toFile());
            return null;
        });
    }

    private static String longText(String prefix, int length) {
        String text = prefix + ". Every part of this long learning content must remain accessible. ".repeat(40);
        return text.substring(0, length).strip();
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layout((Container) child);
            }
        }
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
