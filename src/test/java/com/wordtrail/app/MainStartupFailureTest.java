package com.wordtrail.app;

import java.awt.GraphicsEnvironment;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks real process termination without exiting the test runner or opening a window. */
public final class MainStartupFailureTest {
    @TempDir
    Path directory;

    @Test
    @Timeout(30)
    void startupFailureLogsAndExitsOneWhenTheErrorDialogCannotOpen() throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Path output = directory.resolve("startup-failure.log");
        Process child = new ProcessBuilder(java.toString(), "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8",
                "-cp", classPath, MainStartupFailureTest.class.getName())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            assertTrue(child.waitFor(10, TimeUnit.SECONDS), "The startup-failure process must terminate promptly.");
            String log = Files.readString(output, StandardCharsets.UTF_8);
            assertEquals(1, child.exitValue(), log);
            assertTrue(log.contains("Headless startup-failure probe"), log);
            assertTrue(log.contains("Startup failed"), log);
            assertTrue(log.contains("Simulated initialization failure."), log);
            assertTrue(log.contains("Could not display startup error"), log);
            assertTrue(log.contains("java.awt.HeadlessException"), log);
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                assertTrue(child.waitFor(5, TimeUnit.SECONDS), "The timed-out child process must be stopped.");
            }
        }
    }

    /** Child-process entry point; the headless guard prevents native dialogs or sounds. */
    public static void main(String[] args) {
        if (!GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("This test process must be headless.");
        }
        System.out.println("Headless startup-failure probe");
        Main.reportStartupFailure(new IllegalStateException("Simulated initialization failure."));
    }
}
