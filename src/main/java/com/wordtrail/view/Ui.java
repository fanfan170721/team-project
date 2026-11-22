package com.wordtrail.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/** Shared visual tokens and ordinary Swing controls; no application state lives here. */
final class Ui {
    static final Color INK = new Color(27, 42, 56);
    static final Color ACCENT = new Color(0, 124, 168);
    static final Color MUTED = new Color(100, 116, 133);
    static final Color BACKGROUND = new Color(247, 249, 251);
    static final Color SURFACE = Color.WHITE;
    static final Color BORDER = new Color(225, 232, 238);
    static final Color TINT = new Color(237, 249, 253);

    private Ui() { }

    static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.putClientProperty("html.disable", true);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }

    static JLabel muted(String text, int size) {
        JLabel label = label(text, size);
        label.setForeground(MUTED);
        return label;
    }

    static JLabel heading(String text, int size) {
        JLabel label = label(text, size);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    static JButton button(String name, String text) {
        return new ThemeButton(name, text, false);
    }

    static JButton primaryButton(String name, String text) {
        return new ThemeButton(name, text, true);
    }

    static JTextField field(String name, int columns) {
        JTextField field = new JTextField(columns);
        field.setName(name);
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setForeground(INK);
        field.setBackground(SURFACE);
        field.setCaretColor(ACCENT);
        field.setSelectionColor(TINT);
        field.setSelectedTextColor(INK);
        field.setBorder(new ControlBorder(new Insets(9, 11, 9, 11)));
        return field;
    }

    static JTextArea textArea(String name, int rows) {
        JTextArea area = new JTextArea(rows, 35);
        area.setName(name);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        area.setForeground(INK);
        area.setCaretColor(ACCENT);
        area.setSelectionColor(TINT);
        area.setSelectedTextColor(INK);
        return area;
    }

    static JTextArea display(String text) {
        JTextArea area = textArea("", 4);
        area.setText(text);
        area.setEditable(false);
        area.setOpaque(false);
        return area;
    }

    static JPanel row(Component... children) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row.setOpaque(false);
        for (Component child : children) {
            row.add(child);
        }
        return row;
    }

    static JPanel surface(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = smooth(graphics);
                canvas.setColor(SURFACE);
                canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                canvas.setColor(BORDER);
                canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                canvas.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    static JScrollPane scroll(Component content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    static JPanel page(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(14, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(18, 20, 16, 20));
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);
        heading.add(heading(title, 28), BorderLayout.CENTER);
        heading.add(muted(subtitle, 13), BorderLayout.SOUTH);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    static void formRow(JPanel form, int row, String label, Component input) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(7, 5, 7, 12);
        form.add(Ui.label(label, 14), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, c);
    }

    static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        combo.setForeground(INK);
        combo.setBackground(SURFACE);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new BasicArrowButton(SwingConstants.SOUTH, SURFACE, SURFACE, MUTED, SURFACE);
                arrow.setBorder(new EmptyBorder(0, 5, 0, 5));
                return arrow;
            }
        });
        combo.setBorder(new ControlBorder(new Insets(4, 4, 4, 4)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel cell = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                cell.putClientProperty("html.disable", true);
                cell.setBorder(new EmptyBorder(6, 8, 6, 8));
                cell.setBackground(selected ? TINT : SURFACE);
                cell.setForeground(list.isEnabled() ? INK : MUTED);
                return cell;
            }
        });
    }

    static void styleTable(JTable table) {
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.setForeground(INK);
        table.setBackground(SURFACE);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TINT);
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        DefaultTableCellRenderer cells = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable owner, Object value, boolean selected,
                                                            boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        owner, value, selected, focus, row, column);
                cell.putClientProperty("html.disable", true);
                cell.setHorizontalAlignment(value instanceof Number ? SwingConstants.CENTER : SwingConstants.LEFT);
                cell.setFont(owner.getFont().deriveFont(column == 0 ? Font.BOLD : Font.PLAIN));
                cell.setForeground(owner.isEnabled() ? INK : MUTED);
                cell.setBackground(selected ? TINT : row % 2 == 0 ? SURFACE : BACKGROUND);
                cell.setBorder(focus ? BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT), new EmptyBorder(0, 11, 0, 11))
                        : new EmptyBorder(1, 12, 1, 12));
                return cell;
            }
        };
        table.setDefaultRenderer(Object.class, cells);
        table.setDefaultRenderer(Number.class, cells);
        TableCellRenderer originalHeader = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer((owner, value, selected, focus, row, column) -> {
            Component component = originalHeader.getTableCellRendererComponent(
                    owner, value, selected, focus, row, column);
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.putClientProperty("html.disable", true);
                label.setOpaque(true);
                label.setBackground(BACKGROUND);
                label.setForeground(MUTED);
                label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(new EmptyBorder(8, 12, 8, 12));
            }
            return component;
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(BACKGROUND);
    }

    static JComboBox<TopicOption> topicSelector() {
        JComboBox<TopicOption> combo = new JComboBox<>();
        styleCombo(combo);
        combo.addItem(new TopicOption(null));
        return combo;
    }

    static String topic(JComboBox<TopicOption> combo) {
        TopicOption selected = (TopicOption) combo.getSelectedItem();
        return selected == null ? null : selected.value;
    }

    static void topics(JComboBox<TopicOption> combo, List<String> topics) {
        String previous = topic(combo);
        combo.removeAllItems();
        combo.addItem(new TopicOption(null));
        for (String topic : topics) {
            combo.addItem(new TopicOption(topic));
        }
        selectTopic(combo, previous);
    }

    /** Selects the actual topic value, independently of its display label. */
    static void selectTopic(JComboBox<TopicOption> combo, String topic) {
        for (int index = 0; index < combo.getItemCount(); index++) {
            TopicOption option = combo.getItemAt(index);
            if (Objects.equals(option.value, topic)) {
                combo.setSelectedIndex(index);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private static Graphics2D smooth(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return canvas;
    }

    private static final class ThemeButton extends JButton {
        private final boolean primary;

        private ThemeButton(String name, String text, boolean primary) {
            super(text);
            this.primary = primary;
            setName(name);
            setUI(new BasicButtonUI());
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setForeground(primary ? SURFACE : INK);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(11, 15, 11, 15));
            putClientProperty("html.disable", true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D canvas = smooth(graphics);
            Color fill = primary ? ACCENT : SURFACE;
            if (!isEnabled()) {
                fill = BACKGROUND;
            } else if (getModel().isPressed()) {
                fill = primary ? ACCENT.darker() : TINT.darker();
            } else if (getModel().isRollover()) {
                fill = primary ? new Color(0, 108, 148) : TINT;
            }
            canvas.setColor(fill);
            canvas.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            canvas.setColor(hasFocus() ? ACCENT : primary && isEnabled() ? fill : BORDER);
            canvas.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            if (hasFocus()) {
                canvas.setColor(primary ? SURFACE : ACCENT);
                canvas.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 7, 7);
            }
            canvas.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ControlBorder extends AbstractBorder {
        private final Insets insets;

        private ControlBorder(Insets insets) {
            this.insets = insets;
        }

        @Override
        public Insets getBorderInsets(Component component, Insets destination) {
            destination.set(insets.top, insets.left, insets.bottom, insets.right);
            return destination;
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(insets.top, insets.left, insets.bottom, insets.right);
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D canvas = smooth(graphics);
            canvas.setColor(component.hasFocus() ? ACCENT : BORDER);
            canvas.drawRoundRect(x, y, width - 1, height - 1, 9, 9);
            canvas.dispose();
        }
    }

    /** A null value denotes all topics; any non-null topic name remains selectable. */
    static final class TopicOption {
        private final String value;

        private TopicOption(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value == null ? "All topics" : "Topic: " + value;
        }
    }
}
