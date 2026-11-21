package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.BorderLayout;
import javax.swing.*;

final class HomePanel extends JPanel {
    HomePanel(AppController controller, UiTasks tasks, ViewModels.Account account, Runnable onLogout) {
        this(controller, tasks, account, onLogout, new SwingExportDestinationChooser());
    }

    HomePanel(AppController controller, UiTasks tasks, ViewModels.Account account, Runnable onLogout,
              ExportDestinationChooser exportChooser) {
        super(new BorderLayout(8, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 4, 18));
        header.add(Ui.label("WordTrail  /  " + account.username + (account.admin ? "  (administrator)" : ""), 20), BorderLayout.WEST);
        JButton logout = Ui.button("account.logout", "Log out");
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("home.tabs");
        tabs.addTab("Vocabulary", new LibraryPanel(controller, tasks, false));
        tabs.addTab("Flashcards", new FlashcardPanel(controller, tasks));
        tabs.addTab("Quiz", new QuizPanel(controller, tasks));
        tabs.addTab("Review later", new LibraryPanel(controller, tasks, true));
        tabs.addTab("Progress", new ProgressPanel(controller, tasks));
        if (account.admin) { tabs.addTab("Admin", new AdminPanel(controller, tasks)); }
        tabs.addTab("Export results", new ExportResultsPanel(controller, tasks, exportChooser));
        tabs.addTab("Review plan", new ReviewPlanPanel(controller, tasks));
        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(event -> ((Refreshable) tabs.getSelectedComponent()).refresh());
        logout.addActionListener(event -> tasks.run("Logging out", () -> { controller.logout(); return true; }, ignored -> onLogout.run()));
    }

    void refreshFirstTab() {
        for (java.awt.Component component : getComponents()) {
            if (component instanceof JTabbedPane) {
                ((Refreshable) ((JTabbedPane) component).getSelectedComponent()).refresh();
            }
        }
    }
}
