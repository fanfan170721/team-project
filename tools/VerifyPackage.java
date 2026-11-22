import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ExportResultsViewModel;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.adapter.ReviewPlanViewModel;
import com.wordtrail.adapter.ViewModels;
import com.wordtrail.app.Application;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Run with Java 11+ source-file mode and only the packaged JAR on the classpath. */
public final class VerifyPackage {
    private static final String TOPIC = "Package check";
    private static final String TERM = "packagedword";
    private static final String DEFINITION = "Edited by the offline package check.";
    private static final String COMPLETED_AT = "2026-11-19T12:00:00Z";
    private static final String CSV_HEADER = "completed_at_utc,topic,correct,total\r\n";

    private VerifyPackage() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Pass an output directory for the package verification report.");
        }
        Path output = Path.of(args[0]).toAbsolutePath().normalize();
        Files.createDirectories(output);
        Path report = output.resolve("result.txt");
        Files.writeString(report, "Package verification started; no success recorded yet.\n", StandardCharsets.UTF_8);
        Path data = Files.createTempDirectory(output, "test-profile-");
        DictionaryGateway offline = term -> {
            throw new AssertionError("Package verification must not call a dictionary service.");
        };
        Clock clock = Clock.fixed(Instant.parse(COMPLETED_AT), ZoneOffset.UTC);
        try {
            String id;
            int score;
            try (Application app = new Application(data, offline, clock)) {
                AppController controller = app.controller();
                require(controller.needsSetup(), "A fresh isolated profile must require setup.");
                controller.setup("package_check", "PackageCheck123!".toCharArray());
                require(!controller.browse(null, "").words.isEmpty(), "The packaged seed vocabulary is missing.");
                require(!controller.startQuiz(null, 1).finished, "The packaged seed questions are missing.");
                require(controller.answer(0).finished, "The packaged quiz could not finish.");
                ViewModels.Dashboard dashboard = controller.dashboard();
                require(dashboard.quizzes == 1 && (dashboard.accuracy == 0 || dashboard.accuracy == 100),
                        "The one-question quiz was not saved with a valid result.");
                score = dashboard.accuracy / 100;
                PersonalVocabularyViewModel.SaveResult saved = controller.addPersonalWord(
                        TERM, TOPIC, "Saved by the offline package check.", "", "Test fixture");
                require(saved.success, "The packaged personal-vocabulary flow could not save.");
                id = saved.wordId;
                require(controller.changeDefinition(id, DEFINITION).success,
                        "The packaged personal-definition flow could not save.");
                verifyReadOnlyFeatures(controller, data, id, score, "initial-results.csv");
            }
            require(Files.isRegularFile(data.resolve("wordtrail.json")), "The JSON data file was not written.");
            try (Application reopened = new Application(data, offline, clock)) {
                AppController controller = reopened.controller();
                require(!controller.needsSetup(), "The saved account did not survive reopening the application.");
                controller.login("package_check", "PackageCheck123!".toCharArray());
                require(controller.browse(null, TERM).words.stream().anyMatch(word -> id.equals(word.id)
                                && DEFINITION.equals(word.definition)),
                        "The packaged JSON dependency or saved vocabulary could not be loaded.");
                require(controller.dashboard().quizzes == 1, "The completed quiz did not survive reopening.");
                verifyReadOnlyFeatures(controller, data, id, score, "reopened-results.csv");
                require(Arrays.equals(Files.readAllBytes(data.resolve("initial-results.csv")),
                                Files.readAllBytes(data.resolve("reopened-results.csv"))),
                        "The exported quiz changed after reopening the application.");

                controller.logout();
                controller.register("package_other", "PackageOther123!".toCharArray());
                byte[] before = Files.readAllBytes(data.resolve("wordtrail.json"));
                require(controller.browse(null, TERM).words.isEmpty(),
                        "A different account can see the first account's personal word.");
                ReviewPlanViewModel.Result plan = controller.generateReviewPlan(TOPIC, 1);
                require(plan.success && plan.rows.isEmpty(),
                        "A different account's plan includes the first account's personal word.");
                require(controller.dashboard().quizzes == 0,
                        "A different account can see the first account's completed quiz.");
                verifyExport(controller, data.resolve("other-results.csv"), 0, CSV_HEADER);
                require(Arrays.equals(before, Files.readAllBytes(data.resolve("wordtrail.json"))),
                        "The empty review plan or export changed the second account's profile.");
            }
        } finally {
            deleteTestProfile(data, output);
        }
        String result = "PASS: packaged application wiring, seed vocabulary/questions, JSON dependency, "
                + "UC-08 personal edits, UC-09 CSV export and UC-10 review planning.\n"
                + "Verified reopen persistence, account isolation and unchanged profile bytes after planning/export.\n"
                + "No network access or native window; the isolated test profile was removed.\n";
        Files.writeString(report, result, StandardCharsets.UTF_8);
        System.out.print(result);
    }

    private static void verifyReadOnlyFeatures(AppController controller, Path data, String id,
                                              int score, String csvName) throws IOException {
        byte[] before = Files.readAllBytes(data.resolve("wordtrail.json"));
        ReviewPlanViewModel.Result plan = controller.generateReviewPlan(TOPIC, 1);
        require(plan.success && plan.rows.size() == 1, "The personal-topic review plan is missing.");
        ReviewPlanViewModel.Row row = plan.rows.get(0);
        require(id.equals(row.wordId) && TERM.equals(row.term) && DEFINITION.equals(row.definition),
                "The review plan did not use the saved personal word and edited definition.");
        require("2026-11-19".equals(row.dueDate), "The unreviewed personal word is not due today.");
        String expectedCsv = CSV_HEADER + COMPLETED_AT + ",All topics," + score + ",1\r\n";
        verifyExport(controller, data.resolve(csvName), 1, expectedCsv);
        require(Arrays.equals(before, Files.readAllBytes(data.resolve("wordtrail.json"))),
                "Generating a review plan or exporting results changed the saved profile.");
    }

    private static void verifyExport(AppController controller, Path destination,
                                     int count, String expectedCsv) throws IOException {
        ExportResultsViewModel.Result result = controller.exportResults(destination.toString());
        require(result.success && result.exportedCount == count,
                "The packaged export did not report the correct completed-quiz count.");
        require(destination.equals(Path.of(result.destination).toAbsolutePath().normalize()),
                "The export result reported a different destination.");
        require(expectedCsv.equals(Files.readString(destination, StandardCharsets.UTF_8)),
                "The packaged CSV header, timestamp, topic or quiz result is incorrect.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void deleteTestProfile(Path data, Path output) throws IOException {
        if (!data.getParent().equals(output) || !data.getFileName().toString().startsWith("test-profile-")) {
            throw new IOException("Refusing to remove a path outside the generated test profile.");
        }
        List<Path> paths;
        try (Stream<Path> stream = Files.walk(data)) {
            paths = stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
