package com.wordtrail.data;

import com.wordtrail.usecase.AppException;
import com.wordtrail.usecase.ExportResultsRow;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CsvQuizResultsExporterTest {
    private static final String HEADER = "completed_at_utc,topic,correct,total\r\n";

    @TempDir Path dir;

    private final CsvQuizResultsExporter exporter = new CsvQuizResultsExporter();

    private static ExportResultsRow row(String time, String topic, int correct, int total) {
        return new ExportResultsRow(time, topic, correct, total);
    }

    private static String readUtf8(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private Set<String> filesInDir() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        }
    }

    @Test void writesHeaderAndRowsInGivenOrder() throws IOException {
        Path target = dir.resolve("results.csv");

        exporter.export(target.toString(), List.of(
                row("2026-03-05T09:15:00Z", "Travel", 4, 5),
                row("2026-10-22T16:00:00Z", "Nature", 0, 2)));

        assertEquals(HEADER
                + "2026-03-05T09:15:00Z,Travel,4,5\r\n"
                + "2026-10-22T16:00:00Z,Nature,0,2\r\n", readUtf8(target));
        assertEquals(Set.of("results.csv"), filesInDir(), "no temporary file may be left behind");
    }

    @Test void writesUnicodeAsUtf8() throws IOException {
        Path target = dir.resolve("unicode.csv");

        exporter.export(target.toString(), List.of(row("2026-01-01T00:00:00Z", "日本語 · café 🌍", 1, 1)));

        byte[] expected = (HEADER + "2026-01-01T00:00:00Z,日本語 · café 🌍,1,1\r\n")
                .getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, Files.readAllBytes(target));
    }

    @Test void quotesCommasQuotesAndLineBreaks() throws IOException {
        Path target = dir.resolve("escaped.csv");

        exporter.export(target.toString(), List.of(
                row("2026-01-01T00:00:00Z", "Travel, daily", 1, 2),
                row("2026-01-02T00:00:00Z", "Say \"hello\"", 2, 2),
                row("2026-01-03T00:00:00Z", "Line one\nLine two", 0, 1),
                row("2026-01-04T00:00:00Z", "Carriage\rreturn", 1, 1),
                row("2026-01-05T00:00:00Z", "plain", 1, 3)));

        assertEquals(HEADER
                + "2026-01-01T00:00:00Z,\"Travel, daily\",1,2\r\n"
                + "2026-01-02T00:00:00Z,\"Say \"\"hello\"\"\",2,2\r\n"
                + "2026-01-03T00:00:00Z,\"Line one\nLine two\",0,1\r\n"
                + "2026-01-04T00:00:00Z,\"Carriage\rreturn\",1,1\r\n"
                + "2026-01-05T00:00:00Z,plain,1,3\r\n", readUtf8(target));
    }

    @Test void emptyResultsStillWriteHeader() throws IOException {
        Path target = dir.resolve("empty.csv");

        exporter.export(target.toString(), Collections.emptyList());

        assertEquals(HEADER, readUtf8(target));
        assertEquals(Set.of("empty.csv"), filesInDir());
    }

    @Test void replacesExistingFileCompletely() throws IOException {
        Path target = dir.resolve("results.csv");
        Files.writeString(target, "old content that is much longer than the new export\nsecond line\n");

        exporter.export(target.toString(), List.of(row("2026-01-01T00:00:00Z", "New", 1, 1)));

        assertEquals(HEADER + "2026-01-01T00:00:00Z,New,1,1\r\n", readUtf8(target));
        assertEquals(Set.of("results.csv"), filesInDir());
    }

    @Test void doesNotModifyTheInputList() throws IOException {
        ExportResultsRow first = row("2026-02-01T00:00:00Z", "B", 1, 2);
        ExportResultsRow second = row("2026-01-01T00:00:00Z", "A", 2, 2);
        List<ExportResultsRow> rows = new ArrayList<>(List.of(first, second));

        exporter.export(dir.resolve("results.csv").toString(), rows);

        assertEquals(2, rows.size());
        assertSame(first, rows.get(0));
        assertSame(second, rows.get(1));
        assertEquals("B", rows.get(0).getTopic());
        assertEquals(1, rows.get(0).getCorrect());
        assertEquals(2, rows.get(0).getTotal());
        assertEquals("2026-02-01T00:00:00Z", rows.get(0).getCompletedAtUtc());
        // Read-only input must also work, proving the exporter never writes to the list.
        exporter.export(dir.resolve("again.csv").toString(), Collections.unmodifiableList(rows));
        assertTrue(readUtf8(dir.resolve("again.csv")).endsWith("2026-01-01T00:00:00Z,A,2,2\r\n"));
    }

    @Test void failedReplaceKeepsOriginalAndRemovesTempFile() throws IOException {
        Path target = dir.resolve("results.csv");
        Files.writeString(target, "original results\n");
        List<Path> movedFrom = new ArrayList<>();
        CsvQuizResultsExporter failing = new CsvQuizResultsExporter((source, destination) -> {
            movedFrom.add(source);
            assertTrue(Files.exists(source), "temp file must be fully written before the replace step");
            assertEquals(dir, source.getParent(), "temp file must be in the destination directory");
            throw new IOException("Simulated replace failure");
        });

        AppException error = assertThrows(AppException.class, () ->
                failing.export(target.toString(), List.of(row("2026-01-01T00:00:00Z", "New", 1, 1))));

        assertEquals("Could not save the results file. Any existing file was left unchanged.", error.getMessage());
        assertInstanceOf(IOException.class, error.getCause());
        assertEquals("original results\n", readUtf8(target));
        assertEquals(1, movedFrom.size());
        assertFalse(Files.exists(movedFrom.get(0)), "temp file must be cleaned up");
        assertEquals(Set.of("results.csv"), filesInDir());
    }

    @Test void atomicReplaceNotSupportedFailsInsteadOfOverwriting() throws IOException {
        Path target = dir.resolve("results.csv");
        Files.writeString(target, "original results\n");
        CsvQuizResultsExporter noAtomicMove = new CsvQuizResultsExporter((source, destination) -> {
            throw new AtomicMoveNotSupportedException(source.toString(), destination.toString(), "not supported");
        });

        AppException error = assertThrows(AppException.class, () ->
                noAtomicMove.export(target.toString(), List.of(row("2026-01-01T00:00:00Z", "New", 1, 1))));

        assertTrue(error.getMessage().startsWith("This location cannot replace files safely"));
        assertInstanceOf(AtomicMoveNotSupportedException.class, error.getCause());
        assertEquals("original results\n", readUtf8(target));
        assertEquals(Set.of("results.csv"), filesInDir());
    }

    @Test void missingDirectoryFailsWithoutCreatingAnything() throws IOException {
        Path target = dir.resolve("no-such-folder").resolve("results.csv");

        AppException error = assertThrows(AppException.class, () ->
                exporter.export(target.toString(), List.of(row("2026-01-01T00:00:00Z", "New", 1, 1))));

        assertEquals("Could not save the results file. Any existing file was left unchanged.", error.getMessage());
        assertTrue(filesInDir().isEmpty());
    }

    @Test void rejectsBlankDestinationAndMissingRows() throws IOException {
        assertEquals("Please choose where to save the exported results.",
                assertThrows(AppException.class, () -> exporter.export("  ", List.of())).getMessage());
        assertEquals("Please choose where to save the exported results.",
                assertThrows(AppException.class, () -> exporter.export(null, List.of())).getMessage());
        assertEquals("There are no results to export.",
                assertThrows(AppException.class, () ->
                        exporter.export(dir.resolve("x.csv").toString(), null)).getMessage());
        assertTrue(filesInDir().isEmpty());
    }

    @Test void rejectsInvalidPathCharacters() {
        AppException error = assertThrows(AppException.class, () ->
                exporter.export("bad\0name.csv", List.of()));
        assertEquals("The chosen location is not a valid file path.", error.getMessage());
    }
}
