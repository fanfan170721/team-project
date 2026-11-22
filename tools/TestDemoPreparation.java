import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Process regressions for fresh demo creation and protection of existing profiles. */
public final class TestDemoPreparation {
    private final Path jar;
    private final Path root;
    private int assertions;
    private int skipped;

    private TestDemoPreparation(Path jar, Path root) {
        this.jar = jar;
        this.root = root;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !Files.isRegularFile(Path.of(args[0]))) {
            throw new IllegalArgumentException("Usage: java tools/TestDemoPreparation.java <built-jar>");
        }
        Path root = Files.createTempDirectory("wordtrail-demo-test-").toRealPath();
        try {
            new TestDemoPreparation(Path.of(args[0]).toAbsolutePath().normalize(), root).run();
        } finally {
            // Remove only this invocation's generated temporary root, without following links.
            Path temporaryParent = Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
            if (!root.getParent().equals(temporaryParent)
                    || !root.getFileName().toString().startsWith("wordtrail-demo-test-")
                    || Files.isSymbolicLink(root) || !root.toRealPath().equals(root)) {
                throw new IOException("Refusing cleanup outside the generated test directory.");
            }
            List<Path> paths;
            try (Stream<Path> stream = Files.walk(root)) {
                paths = stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            }
            for (Path path : paths) {
                if (!path.toAbsolutePath().normalize().startsWith(root)) {
                    throw new IOException("Refusing cleanup outside the generated test directory.");
                }
                Files.delete(path);
            }
        }
    }

    private void run() throws Exception {
        Path profile = root.resolve("fresh profile");
        require(check(profile, "fresh") == 0, "A fresh profile must pass creation and the built-in reopen/isolation checks.");
        require(Files.isRegularFile(profile.resolve("wordtrail.json")), "The real JSON store must be written.");
        String notes = Files.readString(profile.resolve("DEMO_DATA.txt"));
        require(notes.contains("SYNTHETIC") && notes.contains("demo_allen") && notes.contains("demo_guest"),
                "The notes must identify synthetic accounts and the date-dependent demo.");
        require(Files.readAllLines(profile.resolve("demo-results.csv")).size() == 3,
                "The owner's CSV must contain a header and two completed quizzes.");
        require(Files.readAllLines(profile.resolve("guest-results.csv")).size() == 1,
                "The guest's CSV must contain only the header.");
        Map<String, String> before = snapshot(profile);
        require(check(profile, "existing-profile") != 0, "An existing profile must be refused.");
        require(before.equals(snapshot(profile)), "Refusal must preserve every file and byte of the existing profile.");

        Path empty = Files.createDirectory(root.resolve("existing-empty"));
        require(check(empty, "existing-empty") != 0, "Even an empty existing directory must be refused.");
        try (Stream<Path> entries = Files.list(empty)) {
            require(entries.count() == 0, "Refusal must leave an existing empty directory empty.");
        }
        Path file = root.resolve("ordinary-file");
        Files.writeString(file, "Keep my data.", StandardCharsets.UTF_8);
        require(check(file, "existing-file") != 0, "An ordinary file must be refused.");
        require("Keep my data.".equals(Files.readString(file)), "The ordinary file's contents must not change.");

        Path missingParent = root.resolve("absent-parent").resolve("profile");
        require(check(missingParent, "missing-parent") != 0, "The helper must not silently create parent directories.");
        require(!Files.exists(missingParent.getParent()), "A missing parent must remain absent.");
        Path buildData = Path.of("target", root.getFileName().toString()).toAbsolutePath().normalize();
        require(!Files.exists(buildData, LinkOption.NOFOLLOW_LINKS), "The build-path fixture must start unused.");
        require(check(buildData, "under-target") != 0, "A data directory inside target must be rejected.");
        require(!Files.exists(buildData, LinkOption.NOFOLLOW_LINKS), "Rejecting a target path must not create it.");

        Path linked = root.resolve("linked-parent");
        Path destination = Files.createDirectory(root.resolve("link-destination"));
        try {
            Files.createSymbolicLink(linked, destination);
        } catch (UnsupportedOperationException | IOException unavailable) {
            skipped++;
            System.out.println("SKIP: linked-parent regression; this host cannot create a symbolic link.");
        }
        if (Files.isSymbolicLink(linked)) {
            require(check(linked.resolve("profile"), "linked-parent") != 0, "A linked parent must be refused.");
            require(!Files.exists(destination.resolve("profile")), "The link destination must remain untouched.");
        }
        System.out.println("PASS: " + assertions + " demo-preparation assertions; " + skipped + " optional link case skipped.");
        System.out.println("No network, GUI or audio; only this test's generated temporary files are removed.");
    }

    private int check(Path directory, String label) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path log = root.resolve(label + ".log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Djava.awt.headless=true", "--class-path", jar.toString(), "tools/PrepareDemo.java", directory.toString())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            throw new AssertionError("Timed out: " + label);
        }
        if (label.equals("fresh") && process.exitValue() != 0) {
            System.err.print(Files.readString(log));
        }
        return process.exitValue();
    }

    private static Map<String, String> snapshot(Path directory) throws Exception {
        Map<String, String> result = new TreeMap<>();
        try (Stream<Path> stream = Files.walk(directory)) {
            for (Path file : stream.filter(Files::isRegularFile).collect(Collectors.toList())) {
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
                result.put(directory.relativize(file).toString(), java.util.Arrays.toString(digest));
            }
        }
        return result;
    }

    private void require(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
