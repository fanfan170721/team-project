package com.wordtrail.view;
import com.wordtrail.adapter.PronunciationController;
import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

final class PronunciationControls extends JPanel {
    private final PronunciationController controller;
    private final JButton play = Ui.button("audio.play", "Listen");
    private final JButton stop = Ui.button("audio.stop", "Stop");
    private final JComboBox<String> accent = new JComboBox<>(new String[]{"en-US", "en-GB"});
    private final JTextArea status = Ui.display("Recorded voice · click Listen to play.");
    private String word = "";
    PronunciationControls(PronunciationController controller) {
        super(new BorderLayout(6, 2));
        this.controller = controller;
        setOpaque(false);
        accent.setName("audio.accent");
        accent.getAccessibleContext().setAccessibleName("Preferred recording accent");
        status.setName("audio.status");
        status.setFont(status.getFont().deriveFont(11f));
        status.setRows(1);
        javax.swing.JScrollPane scroll = Ui.scroll(status);
        scroll.setPreferredSize(new java.awt.Dimension(160, 24));
        play.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        stop.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        buttons.setOpaque(false); buttons.add(accent); buttons.add(play); buttons.add(stop);
        add(buttons, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);
        play.setEnabled(false);
        stop.setEnabled(controller != null);
        play.addActionListener(event -> controller.speak(word, (String) accent.getSelectedItem(), message -> {
            status.setText(message); status.setCaretPosition(0);
        }));
        stop.addActionListener(event -> { controller.stop(); status.setText("Audio stopped."); });
    }
    void word(String next) {
        next = next == null ? "" : next;
        if (!Objects.equals(word, next)) {
            if (controller != null) { controller.stop(); }
            status.setText("Recorded voice · click Listen to play.");
        }
        word = next;
        play.setEnabled(controller != null && !word.isBlank());
    }
    @Override public void removeNotify() {
        if (controller != null) { controller.stop(); }
        super.removeNotify();
    }
}
