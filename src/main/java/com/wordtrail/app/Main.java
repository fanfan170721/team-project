package com.wordtrail.app;

import com.wordtrail.view.AppPanel;
import com.wordtrail.data.DictionaryApiClient;
import com.wordtrail.usecase.port.DictionaryGateway;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Launch with java -jar target/wordtrail.jar [--data-dir PATH]. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Path directory;
        if (args.length == 1 && "--help".equals(args[0])) {
            System.out.println("WordTrail: java -jar wordtrail.jar [--data-dir PATH]\n"
                    + "API diagnostic: java -jar wordtrail.jar --check-api WORD\n"
                    + "Default data folder: .wordtrail in the working directory.");
            return;
        }
        if (args.length == 2 && "--check-api".equals(args[0])) {
            try {
                DictionaryGateway.Entry entry = new DictionaryApiClient().lookup(args[1]);
                System.out.println(entry.term + "\n" + entry.definition + "\nSource: " + entry.source);
            }
            catch (RuntimeException ex) { System.err.println(ex.getMessage()); System.exit(1); }
            return;
        }
        if (args.length == 0) { directory = Path.of(".wordtrail"); }
        else if (args.length == 2 && "--data-dir".equals(args[0])) { directory = Path.of(args[1]); }
        else { System.err.println("Usage: java -jar wordtrail.jar [--data-dir PATH]"); System.exit(2); return; }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("WordTrail requires a graphical desktop to display its Swing window.");
            System.exit(2); return;
        }
        final Application application;
        try { application = new Application(directory); }
        catch (RuntimeException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Startup failed", ex);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, ex.getMessage(), "WordTrail could not start", JOptionPane.ERROR_MESSAGE));
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { application.close(); }
            catch (java.io.IOException ex) { Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Could not close data lock", ex); }
        }, "wordtrail-shutdown"));
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { Logger.getLogger(Main.class.getName()).log(Level.FINE, "Using default Swing look and feel", ex); }
            JFrame frame = new JFrame("WordTrail — Vocabulary Learning");
            AppPanel panel = new AppPanel(application.controller());
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent event) {
                    if (panel.isBusy()) { return; }
                    frame.dispose();
                    try { application.close(); }
                    catch (java.io.IOException ex) { Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Could not close data lock", ex); }
                }
            });
            frame.pack(); frame.setMinimumSize(new java.awt.Dimension(950, 700));
            frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
