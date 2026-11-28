package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import java.awt.BorderLayout;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Validate first, then explicitly confirm a per-account restore. No background import on selection. */
final class BackupPanel extends JPanel implements Refreshable {
    interface Picker { String choose(boolean export); }
    private final AppController controller;
    private final UiTasks tasks;
    private final JTextArea details = Ui.display("Back up your words, saved meanings, results and guided-study checkpoints.\n"
            + "Backups contain learning data only; keep them private. Accounts and passwords are not exported.\n"
            + "Restore replaces this account's learning data and creates a recovery copy first.");
    private final JCheckBox confirm = new JCheckBox("Replace my learning records with this validated backup");
    private final JButton restore = Ui.primaryButton("backup.restore", "Restore and sign out");
    private String source;
    private String digest;

    BackupPanel(AppController controller, UiTasks tasks, Runnable afterRestore) {
        this(controller, tasks, afterRestore, export -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("WordTrail JSON backup", "json"));
            if (export) { chooser.setSelectedFile(new java.io.File("wordtrail-learning-backup.json")); }
            int result = export ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);
            return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().getAbsolutePath() : null;
        });
    }
    BackupPanel(AppController controller, UiTasks tasks, Runnable afterRestore, Picker picker) {
        super(new BorderLayout());
        this.controller = controller; this.tasks = tasks;
        JPanel page = Ui.page("Backup & restore", "Carry your learning to another computer.");
        details.setName("backup.details");
        page.add(Ui.scroll(details), BorderLayout.CENTER);
        JButton export = Ui.button("backup.export", "Export learning backup");
        JButton inspect = Ui.button("backup.inspect", "Choose and validate backup");
        confirm.setName("backup.confirm");
        confirm.setOpaque(false);
        confirm.setEnabled(false); restore.setEnabled(false);
        JPanel actions = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        actions.setOpaque(false);
        actions.add(Ui.row(export, inspect)); actions.add(confirm); actions.add(restore);
        page.add(actions, BorderLayout.SOUTH); add(page);
        export.addActionListener(event -> {
            String destination = picker.choose(true);
            if (destination == null) { tasks.message("Backup export cancelled."); return; }
            tasks.run("Exporting learning backup", () -> {
                controller.backup().exportTo(destination); return destination;
            }, saved -> details.setText("Backup saved to:\n" + saved + "\nExisting files are never overwritten."));
        });
        inspect.addActionListener(event -> {
            source = picker.choose(false);
            digest = null; confirm.setSelected(false); confirm.setEnabled(false); restore.setEnabled(false);
            if (source == null) { tasks.message("Backup selection cancelled."); return; }
            String selected = source;
            tasks.run("Validating learning backup", () -> controller.backup().inspect(selected), result -> {
                digest = result.substring(0, result.indexOf('\n'));
                details.setText(result.substring(result.indexOf('\n') + 1)
                        + "\nFile: " + selected + "\nA recovery copy will be saved before replacement. You will be signed out.");
                confirm.setEnabled(true);
            });
        });
        confirm.addActionListener(event -> restore.setEnabled(digest != null && confirm.isSelected()));
        restore.addActionListener(event -> {
            String selected = source; String checked = digest;
            boolean accepted = confirm.isSelected();
            tasks.run("Restoring learning backup", () -> controller.backup().restore(selected, checked, accepted), recovery -> {
                controller.logout(); afterRestore.run();
                tasks.message("Learning restored. Sign in to continue. Recovery copy: " + recovery);
            });
        });
    }
    @Override public void refresh() { }
}
