import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Exercises the package-check process with real and deliberately damaged JAR copies. */
public final class TestPackageCheck {
    private final Path jar;
    private final Path cases;
    private int assertions;

    private TestPackageCheck(Path jar, Path cases) {
        this.jar = jar;
        this.cases = cases;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java tools/TestPackageCheck.java <jar> <test-output>");
        }
        Path jar = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Files.createDirectories(output);
        Path result = output.resolve("result.txt");
        Files.writeString(result, "STARTED: package-check regressions have not completed.\n");
        try {
            if (!Files.isRegularFile(jar)) {
                throw new IOException("Build the executable JAR before running package-check regressions.");
            }
            Path cases = Files.createTempDirectory(output, "package-cases-");
            new TestPackageCheck(jar, cases).run();
        } catch (Exception | AssertionError failure) {
            Files.writeString(result, "FAIL: package-check regressions did not complete. See the command output.\n");
            throw failure;
        }
    }

    private void run() throws Exception {
        String originalHash = hash(jar);
        Path output = cases.resolve("reused-report");
        require(check(jar, output, null, "valid") == 0, "The real packaged application must pass.");
        require(report(output).startsWith("PASS:"), "Success must leave a current PASS report.");
        Properties valid = metadata(output);
        require("PASS".equals(valid.getProperty("status")), "Success metadata must say PASS.");
        require(originalHash.equals(valid.getProperty("jar.sha256.before")), "Metadata must identify the JAR.");
        require(originalHash.equals(valid.getProperty("jar.sha256.after")), "The checked JAR must stay unchanged.");

        Path broken = cases.resolve("missing-application.jar");
        removeApplication(jar, broken);
        require(check(broken, output, null, "compile-failure") != 0,
                "A JAR missing Application must fail before the probe's main runs.");
        failed(output, "Compile failure must replace the preceding PASS.");
        require(hash(broken).equals(metadata(output).getProperty("jar.sha256.before")),
                "Failure metadata must identify the broken JAR, not the previous valid JAR.");

        require(check(jar, output, null, "recovery") == 0, "A later valid check must recover.");
        require(report(output).startsWith("PASS:"), "Recovery must replace the failure report.");
        require("PASS".equals(metadata(output).getProperty("status")), "Recovery metadata must say PASS.");

        require(check(cases.resolve("not-present.jar"), output, null, "missing-jar") != 0,
                "A missing JAR must fail.");
        failed(output, "A missing input must not leave the preceding PASS.");

        // Exit code zero alone is not evidence that the application probe completed.
        Path silent = cases.resolve("SilentProbe.java");
        Files.writeString(silent, "public class SilentProbe { public static void main(String[] args) {} }\n");
        require(check(jar, output, silent, "no-result") != 0, "A silent successful child is not a passing probe.");
        failed(output, "Missing probe output must be explicit failure.");

        // A result for an artifact that changed during verification cannot identify a download.
        Path mutable = Files.copy(jar, cases.resolve("mutable.jar"));
        Path mutation = cases.resolve("MutatingProbe.java");
        Files.writeString(mutation,
                "import java.nio.file.*;\n"
                + "public class MutatingProbe { public static void main(String[] args) throws Exception {\n"
                + "  Path output = Path.of(args[0]);\n"
                + "  Files.write(output.getParent().resolve(\"mutable.jar\"), new byte[]{0}, StandardOpenOption.APPEND);\n"
                + "  Files.writeString(output.resolve(\"result.txt\"), \"PASS: synthetic mutation probe.\\n\");\n"
                + "} }\n");
        require(check(mutable, output, mutation, "changed-jar") != 0,
                "Changing the JAR during verification must invalidate a passing child result.");
        failed(output, "A changed artifact must not retain a PASS report.");
        Properties changed = metadata(output);
        require(!changed.getProperty("jar.sha256.before", "").equals(
                changed.getProperty("jar.sha256.after", "")), "Both artifact hashes must expose the change.");
        require(originalHash.equals(hash(jar)), "Regressions must not alter the supplied executable JAR.");

        concurrentChecksCannotBorrowEvidence();

        String summary = "PASS: " + assertions + " package-check regression assertions.\n"
                + "Real JAR, compile failure, recovery, missing input/result, changed artifact and concurrent checks.\n"
                + "Fixtures and child logs are retained in " + cases.getFileName() + ".\n";
        Files.writeString(cases.getParent().resolve("result.txt"), summary, StandardCharsets.UTF_8);
        System.out.print(summary);
    }

    private void concurrentChecksCannotBorrowEvidence() throws Exception {
        Path output = cases.resolve("concurrent-report");
        Files.createDirectories(output);
        Path ready = output.resolve("silent.ready");
        Path release = output.resolve("silent.release");
        Path probe = cases.resolve("WaitingSilentProbe.java");
        Files.writeString(probe,
                "import java.nio.file.*;\n"
                + "public class WaitingSilentProbe { public static void main(String[] args) throws Exception {\n"
                + "  Path output = Path.of(args[0]);\n"
                + "  Files.writeString(output.resolve(\"silent.ready\"), \"ready\");\n"
                + "  long deadline = System.nanoTime() + 60000000000L;\n"
                + "  while (!Files.exists(output.resolve(\"silent.release\"))) {\n"
                + "    if (System.nanoTime() > deadline) throw new IllegalStateException(\"Release timed out\");\n"
                + "    Thread.sleep(20);\n"
                + "  }\n"
                + "} }\n");
        Process silent = start(jar, output, probe, "concurrent-silent");
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(45);
            while (!Files.exists(ready) && silent.isAlive() && System.nanoTime() < deadline) {
                Thread.sleep(20);
            }
            require(Files.exists(ready), "The silent probe must be running before the competing check starts.");
            byte[] initialReport = Files.readAllBytes(output.resolve("result.txt"));
            byte[] initialMetadata = Files.readAllBytes(output.resolve("verification.properties"));
            int competingExit = check(jar, output, null, "concurrent-valid");
            boolean reportUnchanged = Arrays.equals(initialReport, Files.readAllBytes(output.resolve("result.txt")));
            boolean metadataUnchanged = Arrays.equals(initialMetadata,
                    Files.readAllBytes(output.resolve("verification.properties")));
            Files.writeString(release, "release");
            int silentExit = await(silent, "concurrent-silent");
            require(competingExit != 0, "A competing invocation must reject the occupied evidence directory.");
            require(reportUnchanged && metadataUnchanged, "A rejected contender must not overwrite the owner's evidence.");
            require(silentExit != 0, "A silent child must not borrow a concurrent invocation's PASS.");
            failed(output, "The silent owner must end with its own failure report.");
            require(check(jar, output, null, "after-concurrency") == 0,
                    "The output lock must be released after failure for a later valid check.");
            require(report(output).startsWith("PASS:") && "PASS".equals(metadata(output).getProperty("status")),
                    "A later valid invocation must recover with its own evidence.");
        } finally {
            Files.writeString(release, "release");
            if (silent.isAlive()) {
                silent.descendants().forEach(child -> child.destroyForcibly());
                silent.destroyForcibly();
                silent.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private int check(Path candidate, Path output, Path probe, String label) throws Exception {
        return await(start(candidate, output, probe, label), label);
    }

    private Process start(Path candidate, Path output, Path probe, String label) throws IOException {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        ProcessBuilder builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Djava.awt.headless=true", "tools/CheckPackage.java", candidate.toString(), output.toString());
        if (probe != null) {
            builder.command().add(probe.toString());
        }
        builder.redirectErrorStream(true).redirectOutput(cases.resolve(label + ".log").toFile());
        return builder.start();
    }

    private int await(Process process, String label) throws Exception {
        if (!process.waitFor(90, TimeUnit.SECONDS)) {
            process.descendants().forEach(child -> child.destroyForcibly());
            process.destroyForcibly();
            throw new AssertionError("Package check timed out: " + label);
        }
        return process.exitValue();
    }

    private void failed(Path output, String message) throws IOException {
        require(report(output).startsWith("FAIL:"), message);
        require("FAIL".equals(metadata(output).getProperty("status")), "Failure metadata must say FAIL.");
    }

    private static String report(Path output) throws IOException {
        return Files.readString(output.resolve("result.txt"), StandardCharsets.UTF_8);
    }

    private static Properties metadata(Path output) throws IOException {
        Properties result = new Properties();
        try (var reader = Files.newBufferedReader(output.resolve("verification.properties"), StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static void removeApplication(Path source, Path target) throws IOException {
        boolean removed = false;
        try (ZipInputStream input = new ZipInputStream(Files.newInputStream(source));
                ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(target))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().equals("com/wordtrail/app/Application.class")) {
                    removed = true;
                } else {
                    output.putNextEntry(new ZipEntry(entry.getName()));
                    input.transferTo(output);
                    output.closeEntry();
                }
                input.closeEntry();
            }
        }
        if (!removed) {
            throw new IOException("The test input did not contain the required Application class.");
        }
    }

    private static String hash(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }
        StringBuilder value = new StringBuilder();
        for (byte item : digest.digest()) {
            value.append(String.format("%02x", item & 0xff));
        }
        return value.toString();
    }

    private void require(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
