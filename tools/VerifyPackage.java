import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.PersonalVocabularyViewModel;
import com.wordtrail.app.Application;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Run with Java 11+ source-file mode and only the packaged JAR on the classpath. */
public final class VerifyPackage {
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
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
        try {
            String id;
            try (Application app = new Application(data, offline, clock)) {
                AppController controller = app.controller();
                require(controller.needsSetup(), "A fresh isolated profile must require setup.");
                controller.setup("package_check", "PackageCheck123!".toCharArray());
                require(!controller.browse(null, "").words.isEmpty(), "The packaged seed vocabulary is missing.");
                require(!controller.startQuiz(null, 1).finished, "The packaged seed questions are missing.");
                PersonalVocabularyViewModel.SaveResult saved = controller.addPersonalWord(
                        "packagedword", "Package check", "Saved by the offline package check.", "", "Test fixture");
                require(saved.success, "The packaged personal-vocabulary flow could not save.");
                id = saved.wordId;
            }
            require(Files.isRegularFile(data.resolve("wordtrail.json")), "The JSON data file was not written.");
            try (Application reopened = new Application(data, offline, clock)) {
                AppController controller = reopened.controller();
                require(!controller.needsSetup(), "The saved account did not survive reopening the application.");
                controller.login("package_check", "PackageCheck123!".toCharArray());
                require(controller.browse(null, "packagedword").words.stream().anyMatch(word -> id.equals(word.id)
                                && "Saved by the offline package check.".equals(word.definition)),
                        "The packaged JSON dependency or saved vocabulary could not be loaded.");
            }
        } finally {
            deleteTestProfile(data, output);
        }
        String result = "PASS: packaged application wiring, seed vocabulary/questions, JSON dependency, "
                + "and account/personal-word persistence.\n"
                + "No network access or native window; the isolated test profile was removed.\n";
        Files.writeString(report, result, StandardCharsets.UTF_8);
        System.out.print(result);
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
