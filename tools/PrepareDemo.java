import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ExportResultsViewModel;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.adapter.ReviewPlanViewModel;
import com.wordtrail.adapter.ViewModels;
import com.wordtrail.app.Application;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/** Creates a new synthetic rehearsal profile through the packaged application's public API. */
public final class PrepareDemo {
    private static final String PASSWORD = "WordTrailDemo123!";
    private static final String TOPIC = "Demo";
    private static final String DUE_TERM = "demoanchor";
    private static final String RECENT_TERM = "demopractice";
    private static final String EDITED = "A personal reminder that a small example makes a design easier to explain.";
    private static final String CSV_HEADER = "completed_at_utc,topic,correct,total\r\n";

    private PrepareDemo() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Pass one BRAND NEW demo directory; its parent must already exist.");
        }
        Path directory = newDirectory(args[0]);
        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = LocalDate.now(zone);
        Clock clock = Clock.fixed(date.atTime(12, 0).atZone(zone).toInstant(), zone);
        String dueId;
        String recentId;
        try (Application app = open(directory, clock)) {
            AppController controller = app.controller();
            controller.setup("demo_admin", PASSWORD.toCharArray());
            controller.logout();
            controller.register("demo_allen", PASSWORD.toCharArray());
            PersonalVocabularyViewModel.SaveResult due = controller.addPersonalWord(
                    DUE_TERM, TOPIC, "A temporary starting definition.", "", "Synthetic rehearsal fixture");
            require(due.success, "Could not create the due demo word.");
            dueId = due.wordId;
            require(controller.changeDefinition(dueId, EDITED).success, "Could not edit the demo definition.");
            PersonalVocabularyViewModel.SaveResult recent = controller.addPersonalWord(
                    RECENT_TERM, TOPIC, "A word reviewed today, so it is due tomorrow.", "", "Synthetic rehearsal fixture");
            require(recent.success, "Could not create the reviewed demo word.");
            recentId = recent.wordId;
            controller.saveWord(dueId, true);
            ViewModels.Card card = controller.startCards(TOPIC, false);
            if (!recentId.equals(card.word.id)) {
                card = controller.nextCard();
            }
            require(recentId.equals(card.word.id), "The demo flashcard deck is incomplete.");
            controller.revealCard();
            require(controller.studyCard().reviewed, "Could not record today's demo review.");
            completeQuiz(controller);
            completeQuiz(controller);
            controller.logout();
            controller.register("demo_guest", PASSWORD.toCharArray());
        }

        // Reopen the real store before declaring the prepared profile usable.
        try (Application app = open(directory, clock)) {
            AppController controller = app.controller();
            controller.login("demo_allen", PASSWORD.toCharArray());
            byte[] before = Files.readAllBytes(directory.resolve("wordtrail.json"));
            require(controller.browse(TOPIC, "").words.size() == 2, "The personal words did not survive reopening.");
            require(controller.dashboard().quizzes == 2 && controller.dashboard().reviews == 1,
                    "The demo quiz/review history did not survive reopening.");
            ReviewPlanViewModel.Result plan = controller.generateReviewPlan(TOPIC, 20);
            require(plan.success && plan.rows.size() == 1 && dueId.equals(plan.rows.get(0).wordId)
                            && EDITED.equals(plan.rows.get(0).definition)
                            && date.toString().equals(plan.rows.get(0).dueDate),
                    "The plan must contain only the unreviewed word and its edited definition.");
            String row = clock.instant() + ",Demo,1,1\r\n";
            export(controller, directory.resolve("demo-results.csv"), 2, CSV_HEADER + row + row);
            controller.logout();
            controller.login("demo_guest", PASSWORD.toCharArray());
            require(controller.browse(TOPIC, "").words.isEmpty() && controller.dashboard().quizzes == 0,
                    "The guest can see the owner's private data.");
            ReviewPlanViewModel.Result guestPlan = controller.generateReviewPlan(TOPIC, 20);
            require(guestPlan.success && guestPlan.rows.isEmpty(), "The guest can see the owner's review plan.");
            export(controller, directory.resolve("guest-results.csv"), 0, CSV_HEADER);
            controller.logout();
            controller.login("demo_admin", PASSWORD.toCharArray());
            require(controller.browse(TOPIC, "").words.isEmpty() && controller.dashboard().quizzes == 0,
                    "The administrator's learner history contains another account's private data.");
            require(Arrays.equals(before, Files.readAllBytes(directory.resolve("wordtrail.json"))),
                    "Reading the plans or exports changed the prepared profile.");
        }

        String notes = "SYNTHETIC WORDTRAIL REHEARSAL DATA\n"
                + "Prepared for: " + date + "\nTime zone: " + zone + "\n"
                + "These are fake local accounts and generated activity, not real team contributions.\n"
                + "Public demo password for all three accounts: " + PASSWORD + "\n\n"
                + "demo_admin: administrator; no private Demo words or completed quizzes.\n"
                + "demo_allen: learner; two private words in topic Demo; two completed 1/1 quizzes.\n"
                + "  " + DUE_TERM + ": edited personal definition, saved for review, never reviewed; due today.\n"
                + "  " + RECENT_TERM + ": reviewed once on " + date + "; first due on " + date.plusDays(1) + ".\n"
                + "demo_guest: learner; no private Demo words, completed quizzes or Demo plan.\n\n"
                + "Open Review plan as demo_allen, select Demo, then Generate plan.\n"
                + "On the preparation date, only " + DUE_TERM + " should appear.\n"
                + "demo-results.csv contains two quiz rows; guest-results.csv contains only the header.\n"
                + "The app uses the actual date when launched: the plan changes as dates advance.\n"
                + "For a fresh rehearsal on another date, prepare a different new directory.\n"
                + "Launch from the project root when you are ready:\n"
                + "java -jar target/wordtrail.jar --data-dir \"" + directory + "\"\n\n"
                + "This helper never opens a window, calls the dictionary API, plays audio, or deletes existing data.\n";
        Files.writeString(directory.resolve("DEMO_DATA.txt"), notes, StandardCharsets.UTF_8);
        System.out.println("PASS: prepared and reopened synthetic UC-08/09/10 demo data at " + directory);
        System.out.println("Read DEMO_DATA.txt for the public demo accounts and expected results.");
    }

    private static Path newDirectory(String argument) throws IOException {
        Path project = Path.of("").toAbsolutePath().normalize().toRealPath();
        Path directory = Path.of(argument).toAbsolutePath().normalize();
        if (directory.startsWith(project.resolve("target"))) {
            throw new IOException("Demo data must stay outside the project's target directory.");
        }
        if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Refusing an existing path, even an empty directory: " + directory);
        }
        Path parent = directory.getParent();
        if (parent == null || !Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The demo directory's parent must already exist: " + parent);
        }
        for (Path ancestor = parent; ancestor != null; ancestor = ancestor.getParent()) {
            BasicFileAttributes attributes = Files.readAttributes(
                    ancestor, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isSymbolicLink() || attributes.isOther() || !ancestor.toRealPath().equals(ancestor)) {
                throw new IOException("Refusing a linked or redirected parent directory: " + ancestor);
            }
        }
        // Atomic creation also refuses a path that appears after the preceding checks.
        return Files.createDirectory(directory);
    }

    private static Application open(Path directory, Clock clock) {
        return new Application(directory, term -> {
            throw new AssertionError("Demo preparation must not contact a dictionary service.");
        }, clock);
    }

    private static void completeQuiz(AppController controller) {
        ViewModels.Quiz question = controller.startQuiz(TOPIC, 1);
        String answer = controller.browse(TOPIC, "").words.stream()
                .filter(word -> word.id.equals(question.wordId)).findFirst().orElseThrow().definition;
        int correct = question.options.indexOf(answer);
        require(correct >= 0 && controller.answer(correct).finished, "Could not complete a demo quiz.");
    }

    private static void export(AppController controller, Path destination, int count, String csv) throws IOException {
        ExportResultsViewModel.Result result = controller.exportResults(destination.toString());
        require(result.success && result.exportedCount == count
                        && csv.equals(Files.readString(destination, StandardCharsets.UTF_8)),
                "The demo CSV does not match the account's completed quizzes.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
