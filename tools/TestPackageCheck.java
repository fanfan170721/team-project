import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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

        String summary = "PASS: " + assertions + " package-check regression assertions.\n"
                + "Real JAR, compile-before-main failure, recovery, missing input, missing result and changed artifact.\n"
                + "Fixtures and child logs are retained in " + cases.getFileName() + ".\n";
        Files.writeString(cases.getParent().resolve("result.txt"), summary, StandardCharsets.UTF_8);
        System.out.print(summary);
    }

    private int check(Path candidate, Path output, Path probe, String label) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        ProcessBuilder builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Djava.awt.headless=true", "tools/CheckPackage.java", candidate.toString(), output.toString());
        if (probe != null) {
            builder.command().add(probe.toString());
        }
        builder.redirectErrorStream(true).redirectOutput(cases.resolve(label + ".log").toFile());
        Process process = builder.start();
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
