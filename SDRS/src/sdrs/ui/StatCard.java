package sdrs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JPanel indicator;
    private final Color accent;

    public StatCard(String title, String value, Color accent) {
        this.accent = accent;
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        indicator = new JPanel() {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, 8, 8);
            }
        };
        indicator.setOpaque(false);
        indicator.setPreferredSize(new Dimension(8, 8));
        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.STAT_TITLE);
        titleLabel.setForeground(Theme.TEXT_LIGHT);
        titleRow.add(indicator, BorderLayout.WEST);
        titleRow.add(titleLabel, BorderLayout.CENTER);

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.STAT_NUMBER);
        valueLabel.setForeground(Theme.TEXT);

        add(titleRow, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Theme.CARD);
        g2.fillRoundRect(0, 0, w, h, 18, 18);
        g2.setColor(new Color(255, 255, 255, 6));
        g2.fillRoundRect(0, 0, w, h / 2, 18, 18);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 130));
        g2.drawLine(14, h - 8, 44, h - 8);
        g2.dispose();
        super.paintComponent(g);
    }

    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension((int) size.getWidth(), 100);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTitleText(String title) {
        titleLabel.setText(title.toUpperCase());
    }
}
