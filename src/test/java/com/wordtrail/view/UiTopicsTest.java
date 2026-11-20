package com.wordtrail.view;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UiTopicsTest {
    @Test
    void aRealAllTopicsNameIsDistinctFromTheUnfilteredSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<Ui.TopicOption> selector = Ui.topicSelector();
            Ui.topics(selector, List.of("All topics", "Nature"));
            assertNull(Ui.topic(selector));
            String allLabel = selector.getSelectedItem().toString();
            AtomicInteger selections = new AtomicInteger();
            selector.addActionListener(event -> selections.incrementAndGet());

            selector.setSelectedIndex(1);

            assertEquals("All topics", Ui.topic(selector));
            assertEquals(1, selector.getSelectedIndex());
            assertNotEquals(allLabel, selector.getSelectedItem().toString());
            assertEquals(1, selections.get());
            selector.setSelectedIndex(0);
            assertNull(Ui.topic(selector));
            assertEquals(2, selections.get());
        });
    }

    @Test
    void refreshingTopicsPreservesTheActualNameInsteadOfMatchingDisplayText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<Ui.TopicOption> selector = Ui.topicSelector();
            Ui.topics(selector, List.of("All topics", "Topic: All topics", "Nature"));
            Ui.selectTopic(selector, "All topics");

            Ui.topics(selector, List.of("Nature", "Topic: All topics", "All topics"));

            assertEquals("All topics", Ui.topic(selector));
            assertEquals(3, selector.getSelectedIndex());
            Ui.selectTopic(selector, "Topic: All topics");
            assertEquals("Topic: All topics", Ui.topic(selector));
            assertEquals(2, selector.getSelectedIndex());
        });
    }

    @Test
    void removingTheSelectedTopicReturnsToTheUnfilteredSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<Ui.TopicOption> selector = Ui.topicSelector();
            Ui.topics(selector, List.of("All topics", "Nature"));
            Ui.selectTopic(selector, "All topics");

            Ui.topics(selector, List.of("Nature"));

            assertNull(Ui.topic(selector));
            assertEquals(0, selector.getSelectedIndex());
            Ui.selectTopic(selector, "Nature");
            Ui.topics(selector, List.of());
            assertNull(Ui.topic(selector));
            assertEquals(1, selector.getItemCount());
        });
    }
}
