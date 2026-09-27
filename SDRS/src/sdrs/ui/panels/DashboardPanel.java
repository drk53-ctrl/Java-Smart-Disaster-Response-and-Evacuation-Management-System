package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.Border;

import sdrs.model.Disaster;
import sdrs.service.AppContext;
import sdrs.service.DataManager;
import sdrs.service.DisasterService;
import sdrs.service.ResponseService;
import sdrs.ui.MainFrame;
import sdrs.ui.MapCanvas;
import sdrs.ui.Refreshable;
import sdrs.ui.StatCard;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;

public class DashboardPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final DataManager dm;

    private StatCard citizensCard;
    private StatCard disastersCard;
    private StatCard activeCard;
    private StatCard criticalCard;
    private StatCard resolvedCard;
    private StatCard sheltersCard;
    private StatCard capacityCard;
    private StatCard teamsCard;
    private StatCard vehiclesCard;
    private StatCard resourcesCard;
    private StatCard pendingEvacCard;
    private StatCard evacuatedCard;

    private DefaultListModel<String> alertsModel;
    private DefaultTableModel recentModel;
    private JLabel alertsEmpty;
    private JTable recentTable;
    private JLabel statusLine;
    private MapCanvas dashboardMap;
    private JLabel mapHint;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dm = AppContext.get().dataManager();
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
        AppContext.get().dataManager().addListener(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        root.add(buildHero(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(buildStatGrid(), BorderLayout.NORTH);
        center.add(buildOperationalRow(), BorderLayout.CENTER);
        center.add(buildRecentCard(), BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        JLabel eyebrow = Theme.microLabel("Live Operations Overview");
        JLabel heading = Theme.displayHeading("Command Center");
        JLabel sub = new JLabel("Disaster response and evacuation management \u00B7 unified view");
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.TEXT_LIGHT);
        titleBox.add(eyebrow);
        titleBox.add(javax.swing.Box.createVerticalStrut(4));
        titleBox.add(heading);
        titleBox.add(javax.swing.Box.createVerticalStrut(3));
        titleBox.add(sub);
        hero.add(titleBox, BorderLayout.WEST);

        JPanel heroRight = new JPanel();
        heroRight.setLayout(new javax.swing.BoxLayout(heroRight, javax.swing.BoxLayout.X_AXIS));
        heroRight.setOpaque(false);
        statusLine = Theme.statusChip("MONITORING", Theme.SUCCESS);
        heroRight.add(statusLine);
        hero.add(heroRight, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildStatGrid() {
        JPanel stats = new JPanel(new GridLayout(3, 4, 12, 12));
        stats.setOpaque(false);
        citizensCard = new StatCard("Registered Citizens", "0", Theme.ACCENT);
        disastersCard = new StatCard("Total Disasters", "0", Theme.TEXT_LIGHT);
        activeCard = new StatCard("Active Disasters", "0", Theme.DANGER);
        criticalCard = new StatCard("Critical Disasters", "0", Theme.DANGER);
        resolvedCard = new StatCard("Resolved Disasters", "0", Theme.SUCCESS);
        sheltersCard = new StatCard("Shelters", "0", Theme.SUCCESS);
        capacityCard = new StatCard("Shelter Capacity Free", "0", Theme.SUCCESS);
        teamsCard = new StatCard("Teams Available", "0", Theme.WARNING);
        vehiclesCard = new StatCard("Vehicles Available", "0", Theme.WARNING);
        resourcesCard = new StatCard("Resource Types in Stock", "0", Theme.ACCENT);
        pendingEvacCard = new StatCard("Pending Evacuations", "0", Theme.DANGER);
        evacuatedCard = new StatCard("Citizens Evacuated", "0", Theme.SUCCESS);
        stats.add(citizensCard);
        stats.add(disastersCard);
        stats.add(activeCard);
        stats.add(criticalCard);
        stats.add(resolvedCard);
        stats.add(sheltersCard);
        stats.add(capacityCard);
        stats.add(teamsCard);
        stats.add(vehiclesCard);
        stats.add(resourcesCard);
        stats.add(pendingEvacCard);
        stats.add(evacuatedCard);
        return stats;
    }

    private JPanel buildOperationalRow() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(buildMapCard(), BorderLayout.CENTER);
        row.add(buildAlertsCard(), BorderLayout.EAST);
        return row;
    }

    private JPanel buildMapCard() {
        dashboardMap = new MapCanvas(AppContext.get().routes());
        dashboardMap.setSelectionListener(new MapCanvas.SelectionListener() {
            public void locationSelected(sdrs.model.Location location) {
                if (mapHint != null) {
                    mapHint.setText(location == null ? "Click a marker for details"
                            : location.getName() + " \u00B7 " + location.getType()
                                    + " \u00B7 open Emergency Navigation for full details");
                }
            }
        });

        JButton zoomIn = Theme.ghostButton("Zoom +");
        JButton zoomOut = Theme.ghostButton("Zoom \u2212");
        JButton zoomReset = Theme.ghostButton("Fit");
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dashboardMap.zoomIn();
            }
        });
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dashboardMap.zoomOut();
            }
        });
        zoomReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dashboardMap.zoomReset();
            }
        });
        JButton openMap = Theme.primaryButton("Open Full Map Console");
        openMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                mainFrame.showPanel("Emergency Map");
            }
        });

        mapHint = new JLabel("Click a marker for details");
        mapHint.setFont(Theme.MICRO);
        mapHint.setForeground(Theme.TEXT_FADED);

        JPanel mapHolder = new JPanel(new BorderLayout());
        mapHolder.setOpaque(false);
        mapHolder.add(dashboardMap, BorderLayout.CENTER);

        JPanel tools = UiUtil.row(zoomIn, zoomOut, zoomReset, openMap, mapHint);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(tools, BorderLayout.NORTH);
        body.add(mapHolder, BorderLayout.CENTER);
        return UiUtil.card(body, "Operations Map \u00B7 Live Road Network");
    }

    private JPanel buildAlertsCard() {
        alertsModel = new DefaultListModel<String>();
        JList<String> list = new JList<String>(alertsModel);
        list.setFont(Theme.NORMAL);
        list.setBackground(new Color(0, 0, 0, 0));
        list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                label.setOpaque(selected);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, severityColorOf(String.valueOf(value))),
                        BorderFactory.createEmptyBorder(8, 10, 8, 8)));
                label.setFont(Theme.NORMAL);
                label.setForeground(severityColorOf(String.valueOf(value)).equals(Theme.DANGER)
                        ? new Color(0xFF, 0xB4, 0xAE) : new Color(0xFF, 0xD9, 0xA8));
                return label;
            }
        });
        alertsEmpty = new JLabel("No critical alerts. All quiet.");
        alertsEmpty.setFont(Theme.SMALL);
        alertsEmpty.setForeground(Theme.TEXT_FADED);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(alertsEmpty, BorderLayout.NORTH);
        body.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel card = UiUtil.card(body, "Critical Alerts \u00B7 Priority Ranked");
        card.setPreferredSize(new Dimension(360, 300));
        return card;
    }

    private Color severityColorOf(String text) {
        if (text.startsWith("CRITICAL")) {
            return Theme.DANGER;
        }
        return Theme.WARNING;
    }

    private JPanel buildRecentCard() {
        recentModel = new DefaultTableModel(
                new Object[] {"ID", "Type", "Location", "Severity", "Status", "Affected", "Reported"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = UiUtil.makeTable(recentModel);
        recentTable = table;
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(UiUtil.tableScrollPane(table), BorderLayout.CENTER);
        return UiUtil.card(body, "Recent Disasters");
    }

    public void refresh() {
        java.util.List<Disaster> disasters = dm.data().getDisasters();
        int active = 0;
        int critical = 0;
        int resolved = 0;
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (d.isActiveLike()) {
                active++;
            }
            if (d.isActiveLike() && Disaster.SEVERITY_CRITICAL.equals(d.getSeverity())) {
                critical++;
            }
            if (Disaster.STATUS_RESOLVED.equals(d.getStatus())) {
                resolved++;
            }
        }
        int shelterTotal = dm.data().getShelters().size();
        int capacity = 0;
        for (int i = 0; i < dm.data().getShelters().size(); i++) {
            capacity += Math.max(0, dm.data().getShelters().get(i).getAvailableCapacity());
        }
        int teamsAvailable = 0;
        for (int i = 0; i < dm.data().getTeams().size(); i++) {
            if (dm.data().getTeams().get(i).isAvailable()) {
                teamsAvailable++;
            }
        }
        int vehiclesAvailable = 0;
        for (int i = 0; i < dm.data().getVehicles().size(); i++) {
            if (dm.data().getVehicles().get(i).isAvailable()) {
                vehiclesAvailable++;
            }
        }
        int resourcesInStock = 0;
        for (int i = 0; i < dm.data().getResources().size(); i++) {
            if (dm.data().getResources().get(i).getQuantity() > 0) {
                resourcesInStock++;
            }
        }
        int pending = 0;
        int evacuated = 0;
        for (int i = 0; i < dm.data().getEvacuations().size(); i++) {
            String status = dm.data().getEvacuations().get(i).getStatus();
            if (sdrs.model.Evacuation.STATUS_PENDING.equals(status)) {
                pending++;
            }
            if (sdrs.model.Evacuation.STATUS_EVACUATED.equals(status)) {
                evacuated++;
            }
        }

        citizensCard.setValue(String.valueOf(dm.data().getCitizens().size()));
        disastersCard.setValue(String.valueOf(disasters.size()));
        activeCard.setValue(String.valueOf(active));
        criticalCard.setValue(String.valueOf(critical));
        resolvedCard.setValue(String.valueOf(resolved));
        sheltersCard.setValue(String.valueOf(shelterTotal));
        capacityCard.setValue(String.valueOf(capacity));
        teamsCard.setValue(String.valueOf(teamsAvailable));
        vehiclesCard.setValue(String.valueOf(vehiclesAvailable));
        resourcesCard.setValue(String.valueOf(resourcesInStock));
        pendingEvacCard.setValue(String.valueOf(pending));
        evacuatedCard.setValue(String.valueOf(evacuated));

        if (critical > 0) {
            statusLine.setText("CRITICAL \u00B7 " + critical + " ACTIVE INCIDENTS");
            statusLine.setForeground(Theme.brighten(Theme.DANGER));
            statusLine.setBackground(Theme.chipBackground(Theme.DANGER));
            statusLine.setBorder(chipBorderFor(Theme.DANGER));
        } else if (active > 0) {
            statusLine.setText("RESPONDING \u00B7 " + active + " ACTIVE");
            statusLine.setForeground(Theme.brighten(Theme.WARNING));
            statusLine.setBackground(Theme.chipBackground(Theme.WARNING));
            statusLine.setBorder(chipBorderFor(Theme.WARNING));
        } else {
            statusLine.setText("ALL CLEAR \u00B7 MONITORING");
            statusLine.setForeground(Theme.brighten(Theme.SUCCESS));
            statusLine.setBackground(Theme.chipBackground(Theme.SUCCESS));
            statusLine.setBorder(chipBorderFor(Theme.SUCCESS));
        }
        statusLine.revalidate();
        statusLine.repaint();

        ResponseService response = AppContext.get().response();
        alertsModel.clear();
        java.util.List<Disaster> alerts = response.triageQueue();
        boolean anyAlert = false;
        for (int i = 0; i < alerts.size() && i < 6; i++) {
            Disaster d = alerts.get(i);
            if (Disaster.SEVERITY_CRITICAL.equals(d.getSeverity())
                    || Disaster.SEVERITY_HIGH.equals(d.getSeverity())) {
                anyAlert = true;
                String tag = Disaster.SEVERITY_CRITICAL.equals(d.getSeverity()) ? "CRITICAL" : "HIGH";
                alertsModel.addElement(tag + "  \u00B7  " + d.getId() + " " + d.getType() + " at "
                        + d.getLocationName() + "  \u00B7  score " + d.getPriorityScore());
            }
        }
        alertsEmpty.setVisible(!anyAlert);

        UiUtil.clearRows(recentModel);
        java.util.List<Disaster> recent = AppContext.get().disasters().recentDisasters(8);
        for (int i = 0; i < recent.size(); i++) {
            Disaster d = recent.get(i);
            UiUtil.addRow(recentModel, new Object[] {
                    d.getId(), d.getType(), d.getLocationName(), d.getSeverity(),
                    d.getStatus(), String.valueOf(d.getAffectedPeople()),
                    DisasterService.formatTime(d.getReportedAt())});
        }

        if (dashboardMap != null) {
            dashboardMap.reload();
        }
        if (recentTable != null) {
            UiUtil.packColumns(recentTable);
        }
    }

    private Border chipBorderFor(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.chipBorder(color)),
                BorderFactory.createEmptyBorder(3, 10, 3, 10));
    }
}
