package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import javax.swing.*;

/** Root view is a panel so complete user flows can also run in headless Swing tests. */
public final class AppPanel extends JPanel {
    private final AppController controller;
    private final UiTasks tasks;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JTextArea status = Ui.display("Ready");

    public AppPanel(AppController controller) {
        super(new BorderLayout());
        this.controller = controller;
        status.setName("app.status"); status.setRows(2);
        status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        status.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        status.setForeground(Ui.ACCENT);
        tasks = new UiTasks(this, status::setText);
        setPreferredSize(new Dimension(1080, 780));
        add(content, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        showLogin();
    }

    /** Observability for the integration harness; not used by business logic. */
    public boolean isBusy() { return tasks.isBusy(); }

    private void showLogin() {
        content.removeAll();
        content.add(new LoginPanel(controller, tasks, this::showHome), BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private void showHome(ViewModels.Account account) {
        content.removeAll();
        HomePanel home = new HomePanel(controller, tasks, account, this::showLogin);
        content.add(home, BorderLayout.CENTER);
        revalidate(); repaint();
        home.refreshFirstTab();
    }
}
