package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

final class HomePanel extends JPanel {
    HomePanel(AppController controller, UiTasks tasks, ViewModels.Account account, Runnable onLogout) {
        this(controller, tasks, account, onLogout, new SwingExportDestinationChooser());
    }

    HomePanel(AppController controller, UiTasks tasks, ViewModels.Account account, Runnable onLogout,
              ExportDestinationChooser exportChooser) {
        super(new BorderLayout());
        setBackground(Ui.BACKGROUND);
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Ui.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        brand.setOpaque(false);
        JLabel name = Ui.heading("WordTrail", 25);
        name.setForeground(Ui.ACCENT);
        brand.add(name);
        JLabel tagline = Ui.muted("A little learning, every day.", 12);
        tagline.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        brand.add(tagline);
        header.add(brand, BorderLayout.WEST);
        JButton logout = Ui.button("account.logout", "Log out");
        JPanel accountActions = Ui.row(Ui.muted(account.username
                + (account.admin ? "  /  Administrator" : "  /  Learner"), 13), logout);
        accountActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        header.add(accountActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("home.tabs");
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabs.setBackground(Ui.SURFACE);
        tabs.setForeground(Ui.INK);
        tabs.setUI(new NavigationTabs());
        tabs.addTab("Vocabulary", new LibraryPanel(controller, tasks, false));
        tabs.addTab("Flashcards", new FlashcardPanel(controller, tasks));
        tabs.addTab("Quiz", new QuizPanel(controller, tasks));
        tabs.addTab("Review later", new LibraryPanel(controller, tasks, true));
        tabs.addTab("Progress", new ProgressPanel(controller, tasks));
        if (account.admin) {
            tabs.addTab("Admin", new AdminPanel(controller, tasks));
        }
        tabs.addTab("Export results", new ExportResultsPanel(controller, tasks, exportChooser));
        tabs.addTab("Review plan", new ReviewPlanPanel(controller, tasks));
        if (controller.study() != null) {
            tabs.addTab("Study", new StudyPanel(controller.study(), tasks, controller.audio()));
        }
        if (controller.backup() != null) {
            tabs.addTab("Backup & restore", new BackupPanel(controller, tasks, onLogout));
        }
        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(event -> {
            if (controller.audio() != null) { controller.audio().stop(); }
            ((Refreshable) tabs.getSelectedComponent()).refresh();
        });
        logout.addActionListener(event -> tasks.run("Logging out", () -> {
            controller.logout();
            return true;
        }, ignored -> onLogout.run()));
    }

    void refreshFirstTab() {
        for (Component component : getComponents()) {
            if (component instanceof JTabbedPane) {
                ((Refreshable) ((JTabbedPane) component).getSelectedComponent()).refresh();
            }
        }
    }

    /** Retains native tab selection and keyboard behavior, with a quieter visual treatment. */
    private static final class NavigationTabs extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(13, 14, 13, 14);
            tabAreaInsets = new Insets(0, 24, 0, 24);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(1, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics graphics, int placement, int index,
                                           int x, int y, int width, int height, boolean selected) {
            graphics.setColor(selected ? Ui.TINT : Ui.SURFACE);
            graphics.fillRect(x, y, width, height);
        }

        @Override
        protected void paintTabBorder(Graphics graphics, int placement, int index,
                                       int x, int y, int width, int height, boolean selected) {
            if (selected) {
                graphics.setColor(Ui.ACCENT);
                graphics.fillRect(x + 9, y + height - 3, width - 18, 3);
            }
        }

        @Override
        protected void paintContentBorder(Graphics graphics, int placement, int selected) {
            graphics.setColor(Ui.BORDER);
            int top = calculateTabAreaHeight(placement, runCount, maxTabHeight);
            graphics.drawLine(0, top, tabPane.getWidth(), top);
        }

        @Override
        protected void paintFocusIndicator(Graphics graphics, int placement, Rectangle[] rectangles,
                                           int index, Rectangle icon, Rectangle text, boolean selected) {
            if (selected && tabPane.hasFocus()) {
                Rectangle tab = rectangles[index];
                graphics.setColor(Ui.ACCENT);
                graphics.drawRect(tab.x + 3, tab.y + 3, tab.width - 7, tab.height - 7);
            }
        }

        @Override
        protected int getTabLabelShiftX(int placement, int index, boolean selected) {
            return 0;
        }

        @Override
        protected int getTabLabelShiftY(int placement, int index, boolean selected) {
            return 0;
        }
    }
}
