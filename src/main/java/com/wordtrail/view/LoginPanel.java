package com.wordtrail.view;

import com.wordtrail.adapter.AppController;
import com.wordtrail.adapter.ViewModels;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.*;

final class LoginPanel extends JPanel {
    LoginPanel(AppController controller, UiTasks tasks, Consumer<ViewModels.Account> onLogin) {
        super(new GridBagLayout());
        boolean setup = controller.needsSetup();
        JPanel card = Ui.page("WordTrail", setup ? "Welcome! Set up this device's first administrator."
                : "Learn a few new words every day.");
        JPanel form = new JPanel(new GridBagLayout());
        JComboBox<String> mode = new JComboBox<>(new String[]{"Log in", "Register a learner account"});
        mode.setName("auth.mode");
        JTextField username = Ui.field("auth.username", 24);
        JPasswordField password = new JPasswordField(24);
        password.setName("auth.password");
        JPasswordField confirm = new JPasswordField(24);
        confirm.setName("auth.confirm");
        Ui.formRow(form, 0, "Action", mode);
        Ui.formRow(form, 1, "Username", username);
        Ui.formRow(form, 2, "Password", password);
        JLabel confirmLabel = Ui.label("Confirm password", 14);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 3; c.anchor = GridBagConstraints.NORTHWEST; c.insets = new Insets(7, 5, 7, 12);
        form.add(confirmLabel, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(confirm, c);
        JButton submit = Ui.button("auth.submit", setup ? "Create administrator" : "Log in");
        Runnable updateMode = () -> {
            boolean creating = setup || mode.getSelectedIndex() == 1;
            confirm.setVisible(creating); confirmLabel.setVisible(creating);
            submit.setText(setup ? "Create administrator" : creating ? "Create learner account" : "Log in");
            revalidate(); repaint();
        };
        mode.setEnabled(!setup);
        mode.addActionListener(event -> updateMode.run());
        updateMode.run();
        card.add(form, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(Ui.display("Username: 3-30 letters, digits or underscores.\nNew passwords: 8-128 characters. Accounts stay in this device's data folder."), BorderLayout.CENTER);
        footer.add(Ui.row(submit), BorderLayout.SOUTH);
        card.add(footer, BorderLayout.SOUTH);
        submit.addActionListener(event -> {
            String name = username.getText();
            char[] secret = password.getPassword();
            char[] repeated = confirm.getPassword();
            boolean registering = mode.getSelectedIndex() == 1;
            password.setText(""); confirm.setText("");
            if ((setup || registering) && !Arrays.equals(secret, repeated)) {
                Arrays.fill(secret, '\0'); Arrays.fill(repeated, '\0');
                tasks.message("The passwords do not match.");
                return;
            }
            Arrays.fill(repeated, '\0');
            tasks.run("Signing in", () -> setup ? controller.setup(name, secret)
                    : registering ? controller.register(name, secret) : controller.login(name, secret), onLogin);
        });
        password.addActionListener(event -> submit.doClick());
        confirm.addActionListener(event -> submit.doClick());
        add(card);
    }
}
