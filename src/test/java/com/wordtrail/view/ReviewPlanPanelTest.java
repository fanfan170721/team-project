package com.wordtrail.view;

import com.wordtrail.adapter.ReviewPlanController;
import com.wordtrail.adapter.ReviewPlanPresenter;
import com.wordtrail.adapter.ReviewPlanViewModel;
import com.wordtrail.adapter.UiException;
import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.User;
import com.wordtrail.entity.VocabularyWord;
import com.wordtrail.usecase.AuthInteractor;
import com.wordtrail.usecase.ReviewPlanInputBoundary;
import com.wordtrail.usecase.ReviewPlanInteractor;
import com.wordtrail.usecase.ReviewPlanResponse;
import com.wordtrail.usecase.SessionContext;
import com.wordtrail.usecase.port.AccountRepository;
import com.wordtrail.usecase.port.ContentRepository;
import com.wordtrail.usecase.port.PasswordHasher;
import com.wordtrail.usecase.port.ProgressRepository;
import java.awt.Component;
import java.awt.Container;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Timeout(30)
class ReviewPlanPanelTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T17:00:00Z"), ZoneOffset.UTC);
    private static final String LONG_DEFINITION = "The final letter of an internationally recognized radio alphabet. "
            + "This extended definition can be read in the selection details even when its table cell is narrow.";

    private final ReviewPlanViewModel viewModel = new ReviewPlanViewModel();
    private final ReviewPlanPresenter presenter = new ReviewPlanPresenter(viewModel);
    private final ReviewPlanInputBoundary realPlan = createPlan(presenter);
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private volatile ReviewPlanInputBoundary input = realPlan;
    private volatile List<String> availableTopics = List.of("All topics", "Nature");
    private ReviewPlanPanel panel;
    private UiTasks tasks;
    private String applicationStatus;

    @BeforeEach
    void openPanel() throws Exception {
        ReviewPlanController controller = new ReviewPlanController((topic, limit) -> {
            requests.add(new Request(topic, limit, SwingUtilities.isEventDispatchThread()));
            input.execute(topic, limit);
        }, viewModel);
        edt(() -> {
            JPanel root = new JPanel();
            tasks = new UiTasks(root, text -> applicationStatus = text);
            panel = new ReviewPlanPanel(() -> availableTopics, controller::generate, tasks);
            root.add(panel);
            panel.refresh();
            return null;
        });
        idle();
    }

    @Test
    void clickingGenerateCommitsTheTypedLimitAndDisplaysThePlanInPriorityOrder() throws Exception {
        edt(() -> {
            Ui.selectTopic(topic(), "All topics");
            limitEditor().setText("2");
            assertEquals(10, limit().getValue(), "The uncommitted text should still be pending.");
            return null;
        });

        generate();

        assertEquals(1, requests.size());
        Request request = requests.get(0);
        assertEquals("All topics", request.topic);
        assertEquals(2, request.limit);
        assertFalse(request.onEdt);
        edt(() -> {
            JTable table = table();
            assertEquals(2, limit().getValue());
            assertEquals(2, table.getRowCount());
            assertEquals("Word", table.getColumnName(0));
            assertEquals("Definition", table.getColumnName(1));
            assertEquals("Due date", table.getColumnName(2));
            assertEquals("Reason", table.getColumnName(3));
            assertEquals("zulu", table.getValueAt(0, 0));
            assertEquals(LONG_DEFINITION, table.getValueAt(0, 1));
            assertEquals(TODAY.toString(), table.getValueAt(0, 2));
            assertTrue(table.getValueAt(0, 3).toString().contains("No recorded review date"));
            assertEquals("alpha", table.getValueAt(1, 0));
            assertFalse(table.isCellEditable(0, 1));
            assertNull(table.getRowSorter());
            JTextArea detail = control("reviewPlan.detail", JTextArea.class);
            assertTrue(detail.getLineWrap());
            assertTrue(detail.getText().contains(LONG_DEFINITION));
            assertTrue(detail.getText().contains(table.getValueAt(0, 3).toString()));
            return null;
        });
    }

    @Test
    void refreshAndFilterChangesRequireAnExplicitNewPlanAndPreserveTheSelectedTopic() throws Exception {
        edt(() -> {
            Ui.selectTopic(topic(), "All topics");
            return null;
        });
        generate();
        assertRows(2);

        availableTopics = List.of("Nature", "All topics");
        edt(() -> {
            panel.refresh();
            return null;
        });
        idle();
        assertRows(0);
        assertEquals(1, requests.size());

        generate();
        assertEquals("All topics", requests.get(1).topic);
        assertRows(2);
        edt(() -> {
            limit().setValue(1);
            assertEquals(0, table().getRowCount());
            assertTrue(message().getText().contains("Generate"));
            return null;
        });
        assertEquals(2, requests.size());

        generate();
        assertRows(1);
        edt(() -> {
            Ui.selectTopic(topic(), "Nature");
            assertEquals(0, table().getRowCount());
            return null;
        });
        assertEquals(3, requests.size());
    }

    @Test
    void failuresAndEmptyPlansClearEarlierResultsAndRestoreTheControls() throws Exception {
        generate();
        assertRows(2);
        input = (topic, limit) -> presenter.presentFailure("Review history could not be read.");
        generate();
        assertRows(0);
        assertEquals("Review history could not be read.", edt(() -> message().getText()));

        input = realPlan;
        generate();
        assertRows(2);
        assertEquals("2 words are due for review.", edt(() -> message().getText()));
        input = (topic, limit) -> presenter.presentPlan(new ReviewPlanResponse(List.of()));
        generate();
        assertRows(0);
        assertEquals("No due words match these filters.", edt(() -> message().getText()));

        input = realPlan;
        generate();
        input = (topic, limit) -> {
            throw new UiException("Review plan is unavailable.", null);
        };
        generate();
        assertRows(0);
        assertEquals("", edt(() -> message().getText()));
        assertEquals("Review plan is unavailable.", edt(() -> applicationStatus));
        assertTrue(edt(() -> button().isEnabled() && topic().isEnabled() && limit().isEnabled()));
    }

    @Test
    void malformedAndOutOfRangeNumbersDoNotReachTheController() throws Exception {
        generate();
        assertRows(2);
        for (String entry : List.of("0", "21", "2xyz", "1.5", "")) {
            edt(() -> {
                limitEditor().setText(entry);
                button().doClick(0);
                assertFalse(tasks.isBusy(), "Invalid text must not start a background request.");
                assertEquals(0, table().getRowCount());
                assertEquals("Enter a whole number from 1 to 20.", message().getText());
                return null;
            });
        }
        assertEquals(1, requests.size());
    }

    @Test
    void repeatedClickWhileWorkIsPendingDoesNotStartASecondRequest() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        input = (topic, limit) -> {
            started.countDown();
            awaitRelease(release);
            realPlan.execute(topic, limit);
        };
        try {
            edt(() -> {
                button().doClick(0);
                assertTrue(tasks.isBusy());
                assertFalse(button().isEnabled());
                assertFalse(topic().isEnabled());
                assertFalse(limit().isEnabled());
                button().doClick(0);
                return null;
            });
            assertTrue(started.await(5, TimeUnit.SECONDS), "The first generation should reach the background worker.");
            assertEquals(1, requests.size());
        } finally {
            release.countDown();
        }
        idle();
        assertRows(2);
        assertTrue(edt(() -> button().isEnabled() && topic().isEnabled() && limit().isEnabled()));
        assertEquals(1, requests.size());
    }

    private void generate() throws Exception {
        edt(() -> {
            assertTrue(button().isEnabled());
            button().doClick(0);
            return null;
        });
        idle();
    }

    private void assertRows(int expected) throws Exception {
        assertEquals(expected, edt(() -> table().getRowCount()));
    }

    private void idle() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (System.nanoTime() < deadline) {
            if (!edt(tasks::isBusy)) {
                return;
            }
            Thread.sleep(10);
        }
        fail("The review plan task did not finish: " + edt(() -> applicationStatus));
    }

    private JTable table() {
        return control("reviewPlan.table", JTable.class);
    }

    private JLabel message() {
        return control("reviewPlan.message", JLabel.class);
    }

    private JComboBox<?> topic() {
        return control("reviewPlan.topic", JComboBox.class);
    }

    private JSpinner limit() {
        return control("reviewPlan.limit", JSpinner.class);
    }

    private JFormattedTextField limitEditor() {
        return ((JSpinner.DefaultEditor) limit().getEditor()).getTextField();
    }

    private JButton button() {
        return control("reviewPlan.generate", JButton.class);
    }

    private <T extends Component> T control(String name, Class<T> type) {
        Component component = find(panel, name);
        assertNotNull(component, "Missing control: " + name);
        return type.cast(component);
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

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }

    private static void awaitRelease(CountDownLatch release) {
        try {
            assertTrue(release.await(8, TimeUnit.SECONDS), "The test should release the pending generation.");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new AssertionError("The pending generation was interrupted.", failure);
        }
    }

    private static ReviewPlanInputBoundary createPlan(ReviewPlanPresenter presenter) {
        TestRepositories repositories = new TestRepositories();
        SessionContext session = new SessionContext();
        AuthInteractor auth = new AuthInteractor(repositories, new PasswordHasher() {
            @Override
            public String hash(char[] password) {
                return "test-hash";
            }

            @Override
            public boolean matches(char[] password, String encodedHash) {
                return "test-hash".equals(encodedHash);
            }
        }, session);
        auth.setupAdministrator("tester", "password123".toCharArray());
        return new ReviewPlanInteractor(repositories, repositories, session, CLOCK, presenter);
    }

    private static final class Request {
        private final String topic;
        private final int limit;
        private final boolean onEdt;

        private Request(String topic, int limit, boolean onEdt) {
            this.topic = topic;
            this.limit = limit;
            this.onEdt = onEdt;
        }
    }

    private static final class TestRepositories implements AccountRepository, ContentRepository, ProgressRepository {
        private User account;

        @Override
        public Optional<User> findUser(String username) {
            return Optional.ofNullable(account).filter(user -> user.getUsername().equals(username));
        }

        @Override
        public boolean hasAccounts() {
            return account != null;
        }

        @Override
        public void createUser(User user) {
            account = user;
        }

        @Override
        public List<VocabularyWord> getWords() {
            return List.of(new VocabularyWord("reviewed", "alpha", "All topics", "The first letter.", "", ""),
                    new VocabularyWord("new", "zulu", "All topics", LONG_DEFINITION, "", ""));
        }

        @Override
        public List<QuizQuestion> getQuestions() {
            return List.of();
        }

        @Override
        public void addWord(VocabularyWord word) {
            throw new AssertionError("Review plan generation must not add words.");
        }

        @Override
        public void addQuestion(QuizQuestion question) {
            throw new AssertionError("Review plan generation must not add questions.");
        }

        @Override
        public LearningProgress getProgress(String username) {
            return LearningProgress.empty().withCompletedReview(
                    "reviewed", TODAY.minusDays(1), CLOCK.instant().minusSeconds(86_400));
        }

        @Override
        public void saveProgress(String username, LearningProgress progress) {
            throw new AssertionError("Review plan generation must not save progress.");
        }
    }
}
