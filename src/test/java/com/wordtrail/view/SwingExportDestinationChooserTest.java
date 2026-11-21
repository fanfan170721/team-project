package com.wordtrail.view;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwingExportDestinationChooserTest {
    @TempDir Path folder;

    /** Scripted dialog answers; records every question so tests can see exactly what was asked. */
    private static final class ScriptedDialogs implements SwingExportDestinationChooser.Dialogs {
        File selection;
        int replaceAnswer = JOptionPane.CLOSED_OPTION;
        final List<Path> replaceQuestions = new ArrayList<>();
        final List<Path> folderMessages = new ArrayList<>();

        @Override public Optional<File> showSaveDialog(Component parent) { return Optional.ofNullable(selection); }

        @Override public int confirmReplace(Component parent, Path target) {
            replaceQuestions.add(target);
            return replaceAnswer;
        }

        @Override public void showFolderChosen(Component parent, Path target) { folderMessages.add(target); }
    }

    private final ScriptedDialogs dialogs = new ScriptedDialogs();
    private final SwingExportDestinationChooser chooser = new SwingExportDestinationChooser(dialogs);

    private Path existing(String name) throws IOException {
        return Files.write(folder.resolve(name), "keep me\n".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void cancellingTheSaveDialogChoosesNothing() {
        dialogs.selection = null;

        assertEquals(Optional.empty(), chooser.choose(null));
        assertTrue(dialogs.replaceQuestions.isEmpty());
    }

    @Test
    void aNewFileIsReturnedWithoutAReplaceQuestion() {
        Path target = folder.resolve("results.csv");
        dialogs.selection = target.toFile();

        assertEquals(Optional.of(target.toAbsolutePath()), chooser.choose(null));
        assertTrue(dialogs.replaceQuestions.isEmpty());
    }

    @Test
    void confirmingReplacementReturnsTheSameFileThatWasConfirmed() throws IOException {
        Path target = existing("results.csv");
        dialogs.selection = target.toFile();
        dialogs.replaceAnswer = JOptionPane.YES_OPTION;

        Optional<Path> chosen = chooser.choose(null);

        assertEquals(List.of(target.toAbsolutePath()), dialogs.replaceQuestions);
        assertEquals(Optional.of(target.toAbsolutePath()), chosen);
    }

    @ParameterizedTest
    @ValueSource(ints = {JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION})
    void decliningOrClosingTheReplaceQuestionChoosesNothingAndKeepsTheFile(int answer) throws IOException {
        Path target = existing("results.csv");
        byte[] before = Files.readAllBytes(target);
        dialogs.selection = target.toFile();
        dialogs.replaceAnswer = answer;

        assertEquals(Optional.empty(), chooser.choose(null));
        assertEquals(List.of(target.toAbsolutePath()), dialogs.replaceQuestions);
        assertArrayEquals(before, Files.readAllBytes(target));
    }

    @Test
    void addsCsvBeforeCheckingWhetherTheFinalFileExists() throws IOException {
        Path finalFile = existing("report.csv");
        dialogs.selection = folder.resolve("report").toFile();
        dialogs.replaceAnswer = JOptionPane.NO_OPTION;

        assertEquals(Optional.empty(), chooser.choose(null));
        assertEquals(List.of(finalFile.toAbsolutePath()), dialogs.replaceQuestions,
                "the question names the .csv file that would actually be replaced");

        dialogs.replaceAnswer = JOptionPane.YES_OPTION;
        assertEquals(Optional.of(finalFile.toAbsolutePath()), chooser.choose(null));
    }

    @Test
    void anExistingFileWithoutTheExtensionIsNotTheTarget() throws IOException {
        existing("notes");
        dialogs.selection = folder.resolve("notes").toFile();

        assertEquals(Optional.of(folder.resolve("notes.csv").toAbsolutePath()), chooser.choose(null));
        assertTrue(dialogs.replaceQuestions.isEmpty(), "notes.csv does not exist yet, so nothing is replaced");
    }

    @Test
    void keepsAnExistingCsvExtensionInAnyCase() {
        dialogs.selection = folder.resolve("REPORT.CSV").toFile();

        assertEquals(Optional.of(folder.resolve("REPORT.CSV").toAbsolutePath()), chooser.choose(null));
    }

    @Test
    void aFolderIsNeverChosenAsTheExportFile() throws IOException {
        Path directory = Files.createDirectory(folder.resolve("archive.csv"));
        dialogs.selection = directory.toFile();
        dialogs.replaceAnswer = JOptionPane.YES_OPTION;

        assertEquals(Optional.empty(), chooser.choose(null));
        assertEquals(List.of(directory.toAbsolutePath()), dialogs.folderMessages);
        assertTrue(dialogs.replaceQuestions.isEmpty());
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    void finalPathIsAbsolute() {
        Path path = SwingExportDestinationChooser.finalPath(new File("relative-name"));

        assertTrue(path.isAbsolute());
        assertEquals("relative-name.csv", path.getFileName().toString());
    }
}
