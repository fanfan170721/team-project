package com.wordtrail.view;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

/** A small native Swing bar chart; each bar counts distinct words studied that day. */
final class ProgressChart extends JPanel {
    private List<String[]> days = Collections.emptyList();

    ProgressChart() {
        setPreferredSize(new Dimension(760, 190));
        setBackground(Color.WHITE);
        setName("progress.chart");
        getAccessibleContext().setAccessibleDescription("Words studied per day during the last fourteen days.");
    }

    void setDays(List<String[]> days) { this.days = days; repaint(); }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (days.isEmpty()) { return; }
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 36, top = 26, bottom = getHeight() - 36;
            int plotWidth = getWidth() - left - 20;
            int max = Math.max(1, days.stream().mapToInt(row -> Integer.parseInt(row[1])).max().orElse(1));
            g.setColor(Ui.MUTED);
            g.drawString("Distinct words studied / day", left, 16);
            g.drawLine(left, bottom, getWidth() - 20, bottom);
            for (int i = 0; i < days.size(); i++) {
                int count = Integer.parseInt(days.get(i)[1]);
                int cellWidth = Math.max(1, plotWidth / days.size());
                int x = left + i * cellWidth;
                int height = count * Math.max(1, bottom - top - 15) / max;
                g.setColor(Ui.ACCENT);
                g.fillRoundRect(x + 6, bottom - height, Math.max(3, cellWidth - 12), height, 5, 5);
                g.setColor(Ui.INK);
                g.drawString(Integer.toString(count), x + cellWidth / 2 - 3, bottom - height - 4);
                g.setFont(g.getFont().deriveFont(10f));
                g.drawString(days.get(i)[0].substring(5), x + 3, bottom + 18);
            }
        }
        finally { g.dispose(); }
    }
}
