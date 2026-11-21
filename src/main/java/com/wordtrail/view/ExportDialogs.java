package com.wordtrail.view;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/** The two UC-09 questions asked of the learner; tests replace the native Swing dialogs. */
interface ExportDialogs {
    /** Returns the chosen file path, or null when the learner cancels. */
    String chooseDestination(Component parent);

    /** Returns true only when the learner agrees to replace the existing file at path. */
    boolean confirmOverwrite(Component parent, String path);

    /** Native Swing dialogs used by the running application. */
    final class Swing implements ExportDialogs {
        static final String SUGGESTED_NAME = "wordtrail-quiz-results.csv";

        @Override
        public String chooseDestination(Component parent) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export completed quiz results");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
            chooser.setSelectedFile(new File(SUGGESTED_NAME));
            if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return null;
            }
            return chooser.getSelectedFile().getAbsolutePath();
        }

        @Override
        public boolean confirmOverwrite(Component parent, String path) {
            return JOptionPane.showConfirmDialog(parent,
                    new File(path).getName() + " already exists.\nReplace it with the exported results?",
                    "Replace existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                    == JOptionPane.YES_OPTION;
        }
    }
}
