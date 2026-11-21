package com.wordtrail.view;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Optional;

/** Asks the learner where to save a UC-09 export. Runs on the Swing event dispatch thread. */
interface ExportDestinationChooser {
    /**
     * Returns the final file the export will write, already confirmed by the learner when it replaces an
     * existing file; empty when the learner cancels or declines to replace it. Nothing is written here.
     */
    Optional<Path> choose(Component parent);
}
