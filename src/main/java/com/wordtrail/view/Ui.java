package com.wordtrail.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Small shared Swing styling and layout helpers. */
final class Ui {
    static final Color INK = new Color(30, 46, 63);
    static final Color ACCENT = new Color(20, 112, 110);
    static final Color MUTED = new Color(92, 106, 119);

    private Ui() { }

    static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.putClientProperty("html.disable", true);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }

    static JButton button(String name, String text) {
        JButton button = new JButton(text);
        button.setName(name);
        return button;
    }

    static JTextField field(String name, int columns) {
        JTextField field = new JTextField(columns);
        field.setName(name);
        return field;
    }

    static JTextArea textArea(String name, int rows) {
        JTextArea area = new JTextArea(rows, 35);
        area.setName(name);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        return area;
    }

    static JTextArea display(String text) {
        JTextArea area = textArea("", 4);
        area.setText(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(INK);
        return area;
    }

    static JPanel row(Component... children) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        for (Component child : children) { row.add(child); }
        return row;
    }

    static JPanel page(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 5));
        heading.add(label(title, 25));
        JLabel hint = label(subtitle, 13);
        hint.setForeground(MUTED);
        heading.add(hint);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    static void formRow(JPanel form, int row, String label, Component input) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(7, 5, 7, 12);
        form.add(Ui.label(label, 14), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, c);
    }

    static String topic(JComboBox<String> combo) {
        return combo.getSelectedIndex() <= 0 ? null : (String) combo.getSelectedItem();
    }

    static void topics(JComboBox<String> combo, java.util.List<String> topics) {
        String previous = (String) combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem("All topics");
        for (String topic : topics) { combo.addItem(topic); }
        combo.setSelectedItem(previous);
        if (combo.getSelectedIndex() < 0) { combo.setSelectedIndex(0); }
    }
}
