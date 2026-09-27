package sdrs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import sdrs.service.AppContext;
import sdrs.ui.panels.CitizensPanel;
import sdrs.ui.panels.DashboardPanel;
import sdrs.ui.panels.DisastersPanel;
import sdrs.ui.panels.EmergencyMapPanel;
import sdrs.ui.panels.EvacuationPanel;
import sdrs.ui.panels.ResponseCenterPanel;
import sdrs.ui.panels.RoutePlanningPanel;
import sdrs.ui.panels.SheltersPanel;
import sdrs.ui.panels.TeamsPanel;
import sdrs.ui.panels.ResourcesPanel;
import sdrs.ui.panels.UserManagementPanel;
import sdrs.ui.panels.VehiclesPanel;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final java.util.Map<String, JPanel> panels = new java.util.LinkedHashMap<String, JPanel>();
    private final java.util.Map<String, JToggleButton> buttons = new java.util.LinkedHashMap<String, JToggleButton>();
    private JPanel contentPanel;
    private JLabel headerTitle;
    private String activeKey;
    private JLabel statusChip;
    private JLabel statusMeta;
    private JLabel clockLabel;
    private Timer clockTimer;
    private JSplitPane shellSplit;

    private static final int SIDEBAR_MIN = 210;
    private static final int SIDEBAR_DEFAULT = 244;
    private static final int SIDEBAR_MAX = 380;

    public MainFrame() {
        super("Smart Disaster Response System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 660));
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | java.awt.Frame.MAXIMIZED_HORIZ);
        setLayout(new BorderLayout());

        contentPanel = new JPanel(new java.awt.CardLayout());
        contentPanel.setBackground(Theme.BG);

        createPanels();
        for (java.util.Map.Entry<String, JPanel> entry : panels.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey());
        }

        JPanel sidebar = buildSidebar();
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(Theme.BG);
        mainArea.add(buildHeader(), BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        add(buildShell(sidebar, mainArea), BorderLayout.CENTER);

        showPanel("Dashboard");
        startClock();
    }

    private void createPanels() {
        panels.put("Dashboard", new DashboardPanel(this));
        panels.put("Citizens", new CitizensPanel(this));
        panels.put("Disasters", new DisastersPanel(this));
        panels.put("Evacuation", new EvacuationPanel(this));
        panels.put("Shelters", new SheltersPanel(this));
        panels.put("Emergency Teams", new TeamsPanel(this));
        panels.put("Resources", new ResourcesPanel(this));
        panels.put("Vehicles", new VehiclesPanel(this));
        panels.put("Emergency Map", new EmergencyMapPanel(this));
        panels.put("Route Planning", new RoutePlanningPanel(this));
        panels.put("Response Center", new ResponseCenterPanel(this));
        panels.put("User Management", new UserManagementPanel(this));
    }

    private javax.swing.JComponent buildShell(JPanel sidebar, JPanel mainArea) {
        // Dynamic minimum width for the main area: keeps the divider from being dragged
        // past SIDEBAR_MAX (and lets the sidebar give way first if the window shrinks).
        JPanel mainHolder = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            public Dimension getMinimumSize() {
                int width = super.getMinimumSize().width;
                if (shellSplit != null && shellSplit.getWidth() > 0) {
                    width = Math.max(width,
                            shellSplit.getWidth() - SIDEBAR_MAX - shellSplit.getDividerSize());
                }
                return new Dimension(width, super.getMinimumSize().height);
            }
        };
        mainHolder.setBackground(Theme.BG);
        mainHolder.add(mainArea, BorderLayout.CENTER);

        shellSplit = new JSplitPane() {
            private static final long serialVersionUID = 1L;

            /** Reported divider position is clamped so the sidebar stays within [SIDEBAR_MIN, SIDEBAR_MAX]. */
            public int getDividerLocation() {
                return clampSidebar(super.getDividerLocation());
            }
        };
        shellSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        shellSplit.setLeftComponent(sidebar);
        shellSplit.setRightComponent(mainHolder);
        shellSplit.setContinuousLayout(true);
        shellSplit.setBorder(null);
        shellSplit.setDividerSize(5);
        shellSplit.setResizeWeight(0.0);
        shellSplit.setOneTouchExpandable(false);
        shellSplit.setBackground(Theme.SIDEBAR);
        shellSplit.setDividerLocation(SIDEBAR_DEFAULT);
        return shellSplit;
    }

    private int clampSidebar(int value) {
        return Math.max(SIDEBAR_MIN, Math.min(SIDEBAR_MAX, value));
    }

    public int sidebarWidth() {
        return shellSplit == null ? SIDEBAR_DEFAULT : shellSplit.getDividerLocation();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_MIN, 0));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_DEFAULT, 100));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(Theme.SIDEBAR);
        brand.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new javax.swing.BoxLayout(brandBox, javax.swing.BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        JLabel brandIcon = new JLabel("SDRS.");
        brandIcon.setFont(Theme.DISPLAY_SMALL.deriveFont(Font.BOLD, 24f));
        brandIcon.setForeground(Theme.TEXT);
        brandIcon.setAlignmentX(0.5f);
        JLabel brandSub = new JLabel("DISASTER RESPONSE COMMAND");
        brandSub.setFont(Theme.MICRO);
        brandSub.setForeground(Theme.TEXT_FADED);
        brandSub.setAlignmentX(0.5f);
        brandBox.add(brandIcon);
        brandBox.add(Box.createVerticalStrut(3));
        brandBox.add(brandSub);
        brand.add(brandBox, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new javax.swing.BoxLayout(menu, javax.swing.BoxLayout.Y_AXIS));
        menu.setBackground(Theme.SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        ButtonGroup group = new ButtonGroup();
        String[][] items = {
                {"Dashboard", "Dashboard"},
                {"Citizens", "Citizen Management"},
                {"Disasters", "Disaster Management"},
                {"Evacuation", "Evacuation Management"},
                {"Shelters", "Shelter Management"},
                {"Emergency Teams", "Emergency Teams"},
                {"Resources", "Resource Management"},
                {"Vehicles", "Vehicle Management"},
                {"Emergency Map", "Emergency Navigation"},
                {"Route Planning", "Route Planning"},
                {"Response Center", "Response Center"},
                {"User Management", "User Management"}};
        for (int i = 0; i < items.length; i++) {
            final String key = items[i][0];
            String display = items[i][1];
            JToggleButton button = new JToggleButton("   " + display) {
                private static final long serialVersionUID = 1L;

                public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    return new Dimension((int) size.getWidth(), 40);
                }

                public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, 40);
                }
            };
            button.setFont(Theme.SMALL);
            button.setForeground(Theme.SIDEBAR_TEXT);
            button.setBackground(Theme.SIDEBAR);
            button.setFocusPainted(false);
            button.setHorizontalAlignment(JLabel.LEFT);
            button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    showPanel(key);
                }
            });
            menu.add(button);
            menu.add(Box.createVerticalStrut(3));
            group.add(button);
            buttons.put(key, button);
        }

        menu.add(Box.createVerticalGlue());
        JButton exit = new JButton("\u23FB  Exit");
        exit.setFont(Theme.SMALL);
        exit.setForeground(Theme.SIDEBAR_TEXT);
        exit.setBackground(Theme.SIDEBAR);
        exit.setFocusPainted(false);
        exit.setHorizontalAlignment(JLabel.LEFT);
        exit.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        exit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                exitApplication();
            }
        });
        menu.add(exit);
        JScrollPane menuScroll = new JScrollPane(menu);
        menuScroll.setBorder(null);
        menuScroll.setBackground(Theme.SIDEBAR);
        menuScroll.getViewport().setBackground(Theme.SIDEBAR);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        menuScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebar.add(menuScroll, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = Math.max(getWidth(), 1);
                int h = Math.max(getHeight(), 1);
                g2.setColor(new Color(13, 13, 13, 235));
                g2.fillRect(0, 0, w, h);
                g2.setColor(Theme.BORDER);
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        JLabel eyebrow = Theme.microLabel("SDRS \u00B7 Command Center");
        headerTitle = new JLabel("Dashboard");
        headerTitle.setFont(Theme.DISPLAY_SMALL);
        headerTitle.setForeground(Theme.TEXT);
        titleBox.add(eyebrow);
        titleBox.add(headerTitle);
        header.add(titleBox, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setLayout(new javax.swing.BoxLayout(right, javax.swing.BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        statusChip = Theme.statusChip("SYSTEM NOMINAL", Theme.SUCCESS);
        statusChip.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusMeta = new JLabel("");
        statusMeta.setFont(Theme.MICRO);
        statusMeta.setForeground(Theme.TEXT_FADED);
        statusMeta.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clockLabel = new JLabel("");
        clockLabel.setFont(Theme.MICRO);
        clockLabel.setForeground(Theme.TEXT_FADED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(statusChip);
        right.add(Box.createVerticalStrut(4));
        right.add(statusMeta);
        right.add(Box.createVerticalStrut(2));
        right.add(clockLabel);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void startClock() {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        clockTimer = new Timer(1000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                clockLabel.setText(format.format(new Date()));
                updateHeaderStatus();
            }
        });
        clockTimer.start();
    }

    public void updateHeaderStatus() {
        java.util.List<sdrs.model.Disaster> disasters = AppContext.get().dataManager().data().getDisasters();
        int active = 0;
        int critical = 0;
        for (int i = 0; i < disasters.size(); i++) {
            sdrs.model.Disaster disaster = disasters.get(i);
            if (disaster.isActiveLike()) {
                active++;
                if (sdrs.model.Disaster.SEVERITY_CRITICAL.equals(disaster.getSeverity())) {
                    critical++;
                }
            }
        }
        if (statusChip == null) {
            return;
        }
        if (critical > 0) {
            statusChip.setText("CRITICAL \u00B7 " + critical + " ACTIVE");
            statusChip.setForeground(Theme.brighten(Theme.DANGER));
            statusChip.setBackground(Theme.chipBackground(Theme.DANGER));
            statusChip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.chipBorder(Theme.DANGER)),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        } else if (active > 0) {
            statusChip.setText("RESPONDING \u00B7 " + active + " ACTIVE");
            statusChip.setForeground(Theme.brighten(Theme.WARNING));
            statusChip.setBackground(Theme.chipBackground(Theme.WARNING));
            statusChip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.chipBorder(Theme.WARNING)),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        } else {
            statusChip.setText("SYSTEM NOMINAL");
            statusChip.setForeground(Theme.brighten(Theme.SUCCESS));
            statusChip.setBackground(Theme.chipBackground(Theme.SUCCESS));
            statusChip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.chipBorder(Theme.SUCCESS)),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        }
        int shelters = AppContext.get().dataManager().data().getShelters().size();
        int pending = 0;
        java.util.List<sdrs.model.Evacuation> evacuations =
                AppContext.get().dataManager().data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (sdrs.model.Evacuation.STATUS_PENDING.equals(evacuations.get(i).getStatus())) {
                pending++;
            }
        }
        statusMeta.setText(shelters + " shelters \u00B7 " + pending + " pending evacuations");
        statusChip.revalidate();
        statusChip.repaint();
    }

    public void showPanel(String key) {
        activeKey = key;
        java.awt.CardLayout layout = (java.awt.CardLayout) contentPanel.getLayout();
        layout.show(contentPanel, key);
        headerTitle.setText(key);
        for (java.util.Map.Entry<String, JToggleButton> entry : buttons.entrySet()) {
            JToggleButton button = entry.getValue();
            boolean selected = entry.getKey().equals(key);
            button.setSelected(selected);
            if (selected) {
                button.setBackground(Theme.SIDEBAR_ACTIVE);
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.ACCENT),
                        BorderFactory.createEmptyBorder(8, 11, 8, 14)));
            } else {
                button.setBackground(Theme.SIDEBAR);
                button.setForeground(Theme.SIDEBAR_TEXT);
                button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            }
        }
        JPanel panel = panels.get(key);
        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refresh();
        }
    }

    public String activePanel() {
        return activeKey;
    }

    public void refreshAll() {
        for (java.util.Map.Entry<String, JPanel> entry : panels.entrySet()) {
            if (entry.getValue() instanceof Refreshable) {
                ((Refreshable) entry.getValue()).refresh();
            }
        }
    }

    public void exitApplication() {
        AppContext.get().dataManager().save();
        if (clockTimer != null) {
            clockTimer.stop();
        }
        System.exit(0);
    }
}
