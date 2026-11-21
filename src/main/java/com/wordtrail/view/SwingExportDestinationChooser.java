package com.wordtrail.view;

import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Save dialog for UC-09. The final {@code .csv} path is decided first, and that exact file is checked for an
 * existing copy, so the file the learner confirms is the file that is written.
 */
final class SwingExportDestinationChooser implements ExportDestinationChooser {
    static final String SUGGESTED_NAME = "wordtrail-quiz-results.csv";

    /** The native dialog calls; tests replace them so no window is opened. */
    interface Dialogs {
        /** Returns the file selected in the save dialog, or empty when the dialog is cancelled or closed. */
        Optional<File> showSaveDialog(Component parent);

        /** Asks to replace an existing file; returns the {@link JOptionPane} option the learner chose. */
        int confirmReplace(Component parent, Path target);

        /** Explains that the chosen name belongs to a folder, which can never be replaced by an export. */
        void showFolderChosen(Component parent, Path target);
    }

    private final Dialogs dialogs;

    SwingExportDestinationChooser() {
        this(new NativeDialogs());
    }

    SwingExportDestinationChooser(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public Optional<Path> choose(Component parent) {
        Optional<File> selected = dialogs.showSaveDialog(parent);
        if (selected.isEmpty()) {
            return Optional.empty();
        }
        Path target = finalPath(selected.get());
        if (Files.isDirectory(target)) {
            dialogs.showFolderChosen(parent, target);
            return Optional.empty();
        }
        if (Files.exists(target) && dialogs.confirmReplace(parent, target) != JOptionPane.YES_OPTION) {
            return Optional.empty();
        }
        return Optional.of(target);
    }

    /** The absolute path that will be written, with {@code .csv} added when the name has no such extension. */
    static Path finalPath(File selected) {
        Path path = selected.toPath().toAbsolutePath().normalize();
        String name = path.getFileName().toString();
        return name.toLowerCase(Locale.ROOT).endsWith(".csv") ? path : path.resolveSibling(name + ".csv");
    }

    /** The real Swing dialogs used by the running application. */
    static final class NativeDialogs implements Dialogs {
        @Override
        public Optional<File> showSaveDialog(Component parent) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export completed quiz results");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
            chooser.setSelectedFile(new File(SUGGESTED_NAME));
            if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return Optional.empty();
            }
            return Optional.ofNullable(chooser.getSelectedFile());
        }

        @Override
        public int confirmReplace(Component parent, Path target) {
            return JOptionPane.showConfirmDialog(parent,
                    target + "\nalready exists. Replace it with the exported results?",
                    "Replace existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }

        @Override
        public void showFolderChosen(Component parent, Path target) {
            JOptionPane.showMessageDialog(parent, target + "\nis a folder. Choose a file name instead.",
                    "Cannot export here", JOptionPane.WARNING_MESSAGE);
        }
    }
}
