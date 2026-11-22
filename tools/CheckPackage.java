import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Properties;

/** Invalidates old evidence before compiling and running the packaged-application probe. */
public final class CheckPackage {
    private CheckPackage() {
    }

    /** Usage: java tools/CheckPackage.java JAR OUTPUT_DIRECTORY [PROBE_SOURCE]. */
    public static void main(String[] args) {
        Path report = null;
        Path manifest = null;
        Process child = null;
        FileChannel lockChannel = null;
        FileLock outputLock = null;
        int wrapperExit = 0;
        String step = "checking command arguments";
        Properties evidence = initialEvidence();
        try {
            if (args.length < 2 || args.length > 3) {
                throw new IllegalArgumentException("Pass JAR, output directory, and optionally the probe source.");
            }
            step = "initializing the evidence directory";
            Path output = Path.of(args[1]).toAbsolutePath().normalize();
            Files.createDirectories(output);
            step = "acquiring exclusive access to the evidence directory";
            lockChannel = FileChannel.open(output.resolve(".package-check.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            outputLock = lockChannel.tryLock();
            if (outputLock == null) {
                throw new IOException("Another package check is using this directory; use a different directory or retry later.");
            }
            // Set these paths only after acquiring the lock: a rejected contender must not
            // overwrite the active invocation's STARTED, PASS, FAIL or metadata in the catch block.
            report = output.resolve("result.txt");
            manifest = output.resolve("verification.properties");
            Files.writeString(report, "STARTED: this run has not verified the package.\n", StandardCharsets.UTF_8);
            writeEvidence(manifest, evidence);

            step = "checking the JAR and probe paths";
            Path jar = Path.of(args[0]).toAbsolutePath().normalize();
            Path probe = Path.of(args.length == 3 ? args[2] : "tools/VerifyPackage.java")
                    .toAbsolutePath().normalize();
            evidence.setProperty("jar.file", jar.getFileName().toString());
            evidence.setProperty("probe.file", probe.getFileName().toString());
            if (!Files.isRegularFile(jar)) {
                throw new IllegalArgumentException("The supplied JAR is not an existing regular file.");
            }
            if (!Files.isRegularFile(probe)) {
                throw new IllegalArgumentException("The verification probe is not an existing regular file.");
            }
            step = "hashing the JAR before verification";
            String before = sha256(jar);
            evidence.setProperty("jar.sha256.before", before);
            writeEvidence(manifest, evidence);

            step = "running the packaged-application probe";
            Path java = Path.of(System.getProperty("java.home"), "bin",
                    File.separatorChar == '\\' ? "java.exe" : "java");
            child = new ProcessBuilder(java.toString(), "-Djava.awt.headless=true", "--class-path",
                    jar.toString(), probe.toString(), output.toString()).inheritIO().start();
            int exitCode = child.waitFor();
            evidence.setProperty("child.exit", Integer.toString(exitCode));

            step = "hashing the JAR after verification";
            String after = sha256(jar);
            evidence.setProperty("jar.sha256.after", after);
            step = "checking the probe exit code";
            if (exitCode != 0) {
                throw new IllegalStateException("The verification probe exited with code " + exitCode + ".");
            }
            step = "checking that the verified JAR did not change";
            if (!before.equals(after)) {
                throw new IllegalStateException("The JAR changed while verification was running.");
            }
            step = "checking the probe PASS report";
            if (!Files.isRegularFile(report)
                    || !Files.readString(report, StandardCharsets.UTF_8).startsWith("PASS:")) {
                throw new IllegalStateException("The probe did not produce a PASS report for this run.");
            }
            step = "saving the successful verification evidence";
            evidence.setProperty("status", "PASS");
            writeEvidence(manifest, evidence);
            System.out.println("PASS: verification evidence binds this result to JAR SHA-256 " + after);
        } catch (Exception failure) {
            if (child != null && child.isAlive()) {
                child.destroyForcibly();
                child.onExit().join();
            }
            String reason = step + " (" + failure.getClass().getSimpleName() + ")";
            evidence.setProperty("status", "FAIL");
            evidence.setProperty("error", reason);
            if (report != null) {
                try {
                    Files.writeString(report, "FAIL: " + reason + ". See the command output.\n",
                            StandardCharsets.UTF_8);
                } catch (Exception writeFailure) {
                    System.err.println("Could not replace the verification report: " + writeFailure);
                }
            }
            if (manifest != null) {
                try {
                    writeEvidence(manifest, evidence);
                } catch (Exception writeFailure) {
                    System.err.println("Could not save failure metadata: " + writeFailure);
                }
            }
            System.err.println("Package verification failed while " + step + ": " + failure);
            if (failure instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            wrapperExit = 1;
        } finally {
            // Keep the lock through all success/failure writes. Never unlink the lock file:
            // replacing its filesystem identity can let two processes own different locks.
            try {
                if (outputLock != null) {
                    outputLock.release();
                }
            } catch (IOException failure) {
                System.err.println("Could not release the evidence lock: " + failure);
                wrapperExit = 1;
            } finally {
                try {
                    if (lockChannel != null) {
                        lockChannel.close();
                    }
                } catch (IOException failure) {
                    System.err.println("Could not close the evidence lock: " + failure);
                    wrapperExit = 1;
                }
            }
        }
        if (wrapperExit != 0) {
            System.exit(wrapperExit);
        }
    }

    private static Properties initialEvidence() {
        Properties evidence = new Properties();
        evidence.setProperty("status", "NOT_RUN");
        evidence.setProperty("jar.sha256.before", "unavailable");
        evidence.setProperty("jar.sha256.after", "unavailable");
        evidence.setProperty("child.exit", "NOT_RUN");
        String revision = System.getenv("WORDTRAIL_REVISION");
        if (revision == null || revision.isBlank()) {
            revision = System.getenv("GITHUB_SHA");
        }
        evidence.setProperty("revision", revision == null || revision.isBlank() ? "unknown" : revision.strip());
        evidence.setProperty("java.version", System.getProperty("java.version"));
        evidence.setProperty("os.name", System.getProperty("os.name"));
        evidence.setProperty("os.arch", System.getProperty("os.arch"));
        evidence.setProperty("checked.at", Instant.now().toString());
        return evidence;
    }

    private static void writeEvidence(Path manifest, Properties evidence) throws Exception {
        try (Writer writer = Files.newBufferedWriter(manifest, StandardCharsets.UTF_8)) {
            evidence.store(writer, "WordTrail package verification; revision is supplied by the caller or CI.");
        }
    }

    private static String sha256(Path jar) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(jar)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte value : digest.digest()) {
            hex.append(Character.forDigit((value >>> 4) & 15, 16));
            hex.append(Character.forDigit(value & 15, 16));
        }
        return hex.toString();
    }
}
