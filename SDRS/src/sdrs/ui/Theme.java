package sdrs.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import sdrs.model.Disaster;
import sdrs.model.Shelter;

public final class Theme {

    public static final Color BG = new Color(0x05, 0x05, 0x05);
    public static final Color BACKGROUND = BG;
    public static final Color CARD = new Color(0x11, 0x11, 0x11);
    public static final Color ACCENT = new Color(0xFF, 0x45, 0x00);
    public static final Color ACCENT_SOFT = new Color(0xFF, 0x45, 0x00, 40);
    public static final Color ACCENT_DIM = new Color(0x3A, 0x1E, 0x12);
    public static final Color GLASS = new Color(255, 255, 255, 8);
    public static final Color GLASS_HOVER = new Color(255, 255, 255, 14);
    public static final Color INPUT = new Color(0x0A, 0x0A, 0x0A);
    public static final Color BORDER = new Color(255, 255, 255, 18);
    public static final Color BORDER_HOVER = new Color(255, 255, 255, 42);
    public static final Color SIDEBAR = new Color(0x08, 0x08, 0x08);
    public static final Color SIDEBAR_TEXT = new Color(0x8A, 0x8A, 0x8A);
    public static final Color SIDEBAR_ACTIVE = new Color(255, 255, 255, 16);
    public static final Color HEADER_BG = new Color(0x0D, 0x0D, 0x0D);
    public static final Color TABLE_SELECTION = new Color(60, 24, 10);
    public static final Color TABLE_ALT = new Color(0x0D, 0x0D, 0x0D);
    public static final Color CHAIN_BG = new Color(26, 18, 12);

    public static final Color PRIMARY = ACCENT;
    public static final Color PRIMARY_DARK = new Color(0xCC, 0x37, 0x00);
    public static final Color SUCCESS = new Color(0x4A, 0xD0, 0x6A);
    public static final Color WARNING = new Color(0xFF, 0x9F, 0x1C);
    public static final Color DANGER = new Color(0xFF, 0x3B, 0x30);
    public static final Color INFO = new Color(0x9A, 0x9A, 0x9A);
    public static final Color TEXT = new Color(0xF2, 0xF2, 0xF2);
    public static final Color TEXT_LIGHT = new Color(0x9A, 0x9A, 0x9A);
    public static final Color TEXT_FADED = new Color(0x5C, 0x5C, 0x5C);

    public static final Font DISPLAY = new Font("Georgia", Font.PLAIN, 30);
    public static final Font DISPLAY_SMALL = new Font("Georgia", Font.PLAIN, 22);
    public static final Font TITLE = new Font("Segoe UI", Font.PLAIN, 21);
    public static final Font H2 = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font H3 = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font H3_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font MICRO = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font STAT_NUMBER = new Font("Georgia", Font.PLAIN, 30);
    public static final Font STAT_TITLE = new Font("Segoe UI", Font.PLAIN, 11);

    private Theme() {
    }

    public static JLabel microLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(MICRO);
        label.setForeground(TEXT_LIGHT);
        return label;
    }

    public static JLabel displayHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DISPLAY);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel serifSubheading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DISPLAY_SMALL);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel statusChip(String text, Color color) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setOpaque(true);
        label.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 36));
        label.setForeground(brighten(color));
        label.setFont(MICRO.deriveFont(Font.BOLD));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120)),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        return label;
    }

    public static Color brighten(Color color) {
        return new Color(Math.min(255, color.getRed() + 70),
                Math.min(255, color.getGreen() + 70),
                Math.min(255, color.getBlue() + 70));
    }

    public static Color chipBackground(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 36);
    }

    public static Color chipBorder(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);
    }

    public static JPanel atmosphere() {
        return new JPanel(null) {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = Math.max(getWidth(), 1);
                int h = Math.max(getHeight(), 1);
                g2.setColor(BG);
                g2.fillRect(0, 0, w, h);

                java.awt.geom.Point2D center = new java.awt.geom.Point2D.Double(w * 0.72, h * 0.18);
                float radius = Math.max(w, h) * 0.9f;
                float[] dist = {0.0f, 0.35f, 1.0f};
                Color[] colors = {new Color(120, 38, 8), new Color(70, 22, 6), new Color(0x05, 0x05, 0x05, 255)};
                java.awt.RadialGradientPaint glow = new java.awt.RadialGradientPaint(center, radius, dist, colors);
                g2.setPaint(glow);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(255, 255, 255, 6));
                for (int x = 0; x < w; x += 44) {
                    g2.drawLine(x, 0, x, h);
                }
                for (int y = 0; y < h; y += 44) {
                    g2.drawLine(0, y, w, y);
                }
                g2.setColor(new Color(255, 69, 0, 22));
                g2.drawLine(0, h - 1, w, h - 1);
            }
        };
    }

    public static void install() {
        javax.swing.border.Border inputBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(5, 8, 5, 8));
        UIManager.put("control", BG);
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("ScrollPane.background", CARD);
        UIManager.put("Viewport.background", CARD);
        UIManager.put("TextField.background", INPUT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextField.selectionBackground", new Color(90, 34, 14));
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.inactiveBackground", INPUT);
        UIManager.put("TextField.inactiveForeground", TEXT_LIGHT);
        UIManager.put("TextField.border", inputBorder);
        UIManager.put("PasswordField.background", INPUT);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("PasswordField.selectionBackground", new Color(90, 34, 14));
        UIManager.put("PasswordField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.border", inputBorder);
        UIManager.put("TextArea.background", INPUT);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("TextArea.selectionBackground", new Color(90, 34, 14));
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.border", inputBorder);
        UIManager.put("ComboBox.background", INPUT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", new Color(90, 34, 14));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground", CARD);
        UIManager.put("ComboBox.buttonHighlight", BORDER);
        UIManager.put("ComboBox.buttonShadow", BORDER);
        UIManager.put("ComboBox.border", inputBorder);
        UIManager.put("List.background", CARD);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", TABLE_SELECTION);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", new Color(255, 255, 255, 10));
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.alternateRowColor", TABLE_ALT);
        UIManager.put("TableHeader.background", HEADER_BG);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("TitledBorder.titleColor", TEXT);
        UIManager.put("Separator.background", BORDER);
        UIManager.put("ProgressBar.foreground", ACCENT);
    }

    public static void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(color == ACCENT ? Color.WHITE : new Color(0x0A, 0x0A, 0x0A));
        button.setFocusPainted(false);
        button.setFont(H3_BOLD);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent event) {
                JButton source = (JButton) event.getSource();
                source.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_HOVER),
                        BorderFactory.createEmptyBorder(7, 17, 7, 17)));
            }

            public void mouseExited(java.awt.event.MouseEvent event) {
                JButton source = (JButton) event.getSource();
                source.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            }
        });
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, ACCENT);
        return button;
    }

    public static JButton successButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, SUCCESS);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, DANGER);
        return button;
    }

    public static JButton warningButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, WARNING);
        return button;
    }

    public static JButton ghostButton(String text) {
        JButton button = new JButton(text) {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x14, 0x14, 0x14));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (getModel().isRollover()) {
                    g2.setColor(GLASS_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setFont(H3);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(NORMAL);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(255, 255, 255, 10));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(t, value,
                        selected, focused, row, column);
                component.setBackground(HEADER_BG);
                component.setForeground(TEXT_FADED);
                component.setFont(MICRO.deriveFont(Font.BOLD));
                return component;
            }
        };
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(headerRenderer);
        header.setReorderingAllowed(false);
    }

    public static JLabel statusLabel(String text, Color color) {
        return statusChip(text, color);
    }

    public static Color severityColor(String severity) {
        if (Disaster.SEVERITY_CRITICAL.equals(severity)) {
            return DANGER;
        }
        if (Disaster.SEVERITY_HIGH.equals(severity)) {
            return WARNING;
        }
        if (Disaster.SEVERITY_MEDIUM.equals(severity)) {
            return ACCENT;
        }
        return SUCCESS;
    }

    public static Color statusColor(String status) {
        if (Disaster.STATUS_RESOLVED.equals(status)) {
            return SUCCESS;
        }
        if (Disaster.STATUS_RESPONSE.equals(status)) {
            return WARNING;
        }
        if (Disaster.STATUS_ACTIVE.equals(status)) {
            return DANGER;
        }
        return TEXT_LIGHT;
    }

    public static Color shelterStatusColor(String status) {
        if (Shelter.STATUS_FULL.equals(status)) {
            return WARNING;
        }
        if (Shelter.STATUS_CLOSED.equals(status)) {
            return DANGER;
        }
        return SUCCESS;
    }
}
