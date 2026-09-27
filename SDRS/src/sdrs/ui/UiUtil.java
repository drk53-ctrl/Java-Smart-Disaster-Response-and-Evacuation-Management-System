package sdrs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import sdrs.util.AppException;

public final class UiUtil {

    public static final String FILTER_ALL = "All";
    public static final int FORM_LABEL_WIDTH = 195;

    private UiUtil() {
    }

    public static JPanel card(JComponent content, String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10)) {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 18, 18);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        if (title != null) {
            panel.add(headerLabel(title), BorderLayout.NORTH);
        }
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public static JLabel headerLabel(String text) {
        JLabel titleLabel = new JLabel(text);
        titleLabel.setFont(Theme.H2);
        titleLabel.setForeground(Theme.TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return titleLabel;
    }

    public static JPanel toolbar(Component... components) {
        JPanel panel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 6));
        panel.setBackground(Theme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        for (int i = 0; i < components.length; i++) {
            panel.add(components[i]);
        }
        return panel;
    }

    public static JPanel row(Component... components) {
        JPanel panel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 4));
        panel.setOpaque(false);
        for (int i = 0; i < components.length; i++) {
            panel.add(components[i]);
        }
        return panel;
    }

    public static JScrollPane tableScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scroll;
    }

    public static JTable makeTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        return table;
    }

    public static void packColumns(JTable table) {
        packColumns(table, null, null, 420);
    }

    public static void packColumns(JTable table, java.util.Map<Integer, Integer> minSizes,
                                   java.util.Map<Integer, Integer> maxSizes, int stretchColumn) {
        javax.swing.table.TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount(); i++) {
            TableColumn column = columns.getColumn(i);
            Object headerValue = column.getHeaderValue();
            int width = headerValue == null ? 70
                    : table.getFontMetrics(Theme.H3).stringWidth(headerValue.toString()) + 34;
            for (int row = 0; row < Math.min(table.getRowCount(), 60); row++) {
                Object value = table.getModel().getValueAt(row, i);
                if (value != null) {
                    width = Math.max(width, table.getFontMetrics(Theme.NORMAL)
                            .stringWidth(value.toString()) + 26);
                }
            }
            if (minSizes != null && minSizes.containsKey(Integer.valueOf(i))) {
                width = Math.max(width, ((Integer) minSizes.get(Integer.valueOf(i))).intValue());
            }
            if (maxSizes != null && maxSizes.containsKey(Integer.valueOf(i))) {
                width = Math.min(width, ((Integer) maxSizes.get(Integer.valueOf(i))).intValue());
            }
            column.setPreferredWidth(Math.max(width, 48));
        }
        int columnCount = columns.getColumnCount();
        int total = 0;
        for (int i = 0; i < columnCount; i++) {
            total += columns.getColumn(i).getPreferredWidth();
        }
        if (stretchColumn >= 0 && stretchColumn < columnCount) {
            TableColumn stretch = columns.getColumn(stretchColumn);
            int index = stretchColumn;
            int best = stretch.getPreferredWidth();
            if (total - stretch.getPreferredWidth() < 520) {
                int desired = Math.max(best, 560 - (total - stretch.getPreferredWidth()));
                stretch.setPreferredWidth(Math.max(best, desired));
            }
            total = 0;
            for (int i = 0; i < columnCount; i++) {
                total += columns.getColumn(i).getPreferredWidth();
            }
            stretch.setPreferredWidth(stretch.getPreferredWidth() + Math.max(0, 860 - total));
        }
    }

    public static void addToolTipRenderer(final JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(t, value,
                        selected, focused, row, column);
                if (value != null) {
                    ((JComponent) component).setToolTipText(value.toString());
                }
                return component;
            }
        });
    }

    public static void clearRows(DefaultTableModel model) {
        model.setRowCount(0);
    }

    public static void addRow(DefaultTableModel model, Object[] row) {
        model.addRow(row);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, AppException ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Cannot complete action",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cannot complete action", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String comboValue(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    public static boolean isAll(String value) {
        return value == null || FILTER_ALL.equals(value);
    }

    public static void showDialog(JFrame owner, JDialog dialog) {
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.NORMAL);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JPanel formRow(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        JLabel label = fieldLabel(labelText);
        label.setPreferredSize(new java.awt.Dimension(FORM_LABEL_WIDTH, 24));
        label.setMinimumSize(new java.awt.Dimension(FORM_LABEL_WIDTH, 24));
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    public static JLabel bold(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.H3.deriveFont(Font.BOLD, 14f));
        return label;
    }

    public static JLabel statusBadge(String text, Color color) {
        return Theme.statusLabel(text, color);
    }

    public static JPanel sidePanel(String title, String subtitle, List<String> lines) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.H2);
        heading.setForeground(Theme.TEXT);
        panel.add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setOpaque(false);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(Theme.H3);
            sub.setForeground(Theme.TEXT);
            sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            body.add(sub);
        }
        for (int i = 0; i < lines.size(); i++) {
            JLabel line = new JLabel(lines.get(i));
            line.setFont(Theme.NORMAL);
            line.setForeground(Theme.TEXT);
            line.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            body.add(line);
        }
        body.add(javax.swing.Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}
