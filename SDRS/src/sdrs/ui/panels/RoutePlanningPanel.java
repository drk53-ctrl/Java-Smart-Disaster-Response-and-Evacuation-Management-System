package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import sdrs.algorithm.RouteResult;
import sdrs.model.Location;
import sdrs.model.RoadSegment;
import sdrs.service.AppContext;
import sdrs.service.RouteService;
import sdrs.ui.MainFrame;
import sdrs.ui.MapCanvas;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class RoutePlanningPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private static final String TAB_ROADS = "Road Network";
    private static final String TAB_MAP = "Map View";

    private final MainFrame mainFrame;
    private final RouteService routeService = AppContext.get().routes();
    private final sdrs.service.NetworkService network = AppContext.get().network();

    private DefaultTableModel roadsModel;
    private JTable roadsTable;
    private JComboBox<String> fromCombo;
    private JComboBox<String> toCombo;
    private JRadioButton distanceOption;
    private JRadioButton timeOption;
    private JTextArea routeOutput;
    private MapCanvas mapCanvas;
    private final java.util.Map<String, String> labelToId = new java.util.HashMap<String, String>();

    public RoutePlanningPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildRouteCard(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.H3);
        tabs.addTab(TAB_ROADS, buildNetworkCard());
        tabs.addTab(TAB_MAP, buildMapCard());
        add(tabs, BorderLayout.CENTER);

        refresh();
        AppContext.get().dataManager().addListener(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    private JPanel buildNetworkCard() {
        roadsModel = new DefaultTableModel(new Object[] {"Road", "From", "To", "Distance (km)",
                "Time (min)", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roadsTable = UiUtil.makeTable(roadsModel);
        UiUtil.addToolTipRenderer(roadsTable);

        JPanel buttons = UiUtil.toolbar(
                Theme.successButton("+ Add Location"),
                Theme.primaryButton("+ Add Road"),
                Theme.warningButton("Block / Reopen Road"),
                Theme.dangerButton("Delete Road"),
                bfsButton());
        hookNetworkButtons(buttons);

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Theme.BACKGROUND);
        card.add(buttons, BorderLayout.NORTH);
        card.add(UiUtil.card(UiUtil.tableScrollPane(roadsTable), "Road Network"), BorderLayout.CENTER);
        return card;
    }

    private JButton bfsButton() {
        JButton impact = Theme.ghostButton("Blocked-Road Impact (BFS)");
        impact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                showImpact();
            }
        });
        return impact;
    }

    private void hookNetworkButtons(JPanel buttons) {
        java.awt.Component[] components = buttons.getComponents();
        ((JButton) components[0]).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                addLocationDialog();
            }
        });
        ((JButton) components[1]).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                addRoadDialog();
            }
        });
        ((JButton) components[2]).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                toggleBlock();
            }
        });
        ((JButton) components[3]).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteRoad();
            }
        });
    }

    private JPanel buildMapCard() {
        mapCanvas = new MapCanvas(routeService);
        mapCanvas.setSelectionListener(new MapCanvas.SelectionListener() {
            public void locationSelected(Location location) {
                if (location != null) {
                    routeOutput.setText(describeLocation(location));
                }
            }
        });
        JScrollPane mapScroll = new JScrollPane(mapCanvas);
        mapScroll.getViewport().setBackground(Theme.BACKGROUND);
        mapScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        mapScroll.getVerticalScrollBar().setUnitIncrement(16);
        mapScroll.getHorizontalScrollBar().setUnitIncrement(16);

        javax.swing.JButton zoomIn = Theme.ghostButton("Zoom +");
        javax.swing.JButton zoomOut = Theme.ghostButton("Zoom \u2212");
        javax.swing.JButton zoomReset = Theme.ghostButton("Fit Map");
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                mapCanvas.zoomIn();
            }
        });
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                mapCanvas.zoomOut();
            }
        });
        zoomReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                mapCanvas.zoomReset();
            }
        });

        JPanel infoPanel = new JPanel(new BorderLayout(0, 8));
        infoPanel.setBackground(Theme.BACKGROUND);
        infoPanel.add(buildOutputCard(), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScroll, infoPanel);
        split.setResizeWeight(0.68);
        split.setDividerLocation(0.68);
        split.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BACKGROUND);
        card.add(UiUtil.row(zoomIn, zoomOut, zoomReset), BorderLayout.NORTH);
        card.add(split, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOutputCard() {
        routeOutput = new JTextArea();
        routeOutput.setEditable(false);
        routeOutput.setLineWrap(true);
        routeOutput.setWrapStyleWord(true);
        routeOutput.setFont(Theme.NORMAL);
        routeOutput.setBackground(Theme.INPUT);
        routeOutput.setForeground(Theme.TEXT);
        routeOutput.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        routeOutput.setText("Click a location on the map, or use the Route Finder above, "
                + "to see results here.");
        return UiUtil.card(new JScrollPane(routeOutput), "Map Inspector / Route Result");
    }

    private String describeLocation(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(location.getName()).append(" ===\n");
        sb.append("Type: ").append(location.getType()).append("\n");
        sb.append("Map position: ").append(String.format("%.0f, %.0f",
                Double.valueOf(location.getX()), Double.valueOf(location.getY()))).append("\n");
        List<RoadSegment> roads = routeService.roadsTouching(location.getId());
        sb.append("Connected roads: ").append(roads.size()).append("\n");
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            sb.append("  ").append(road.getName()).append(" -> ")
                    .append(routeService.locationName(road.otherEnd(location.getId())))
                    .append(String.format(" (%.1f km, %.0f min)%s",
                            Double.valueOf(road.getDistanceKm()),
                            Double.valueOf(road.getTravelMinutes()),
                            road.isBlocked() ? " [BLOCKED]" : "")).append("\n");
        }
        List<String> disasterIds = routeService.activeDisasterIdsAt(location.getId());
        for (int i = 0; i < disasterIds.size(); i++) {
            sdrs.model.Disaster disaster = (sdrs.model.Disaster) AppContext.get().dataManager()
                    .disasterById(disasterIds.get(i));
            if (disaster != null) {
                sb.append("ACTIVE DISASTER: ").append(disaster.getId()).append(" ")
                        .append(disaster.getType()).append(" [").append(disaster.getSeverity())
                        .append("], ").append(disaster.getAffectedPeople()).append(" affected\n");
            }
        }
        List<sdrs.model.Shelter> shelters = routeService.sheltersAt(location.getId());
        for (int i = 0; i < shelters.size(); i++) {
            sdrs.model.Shelter shelter = shelters.get(i);
            sb.append("SHELTER: ").append(shelter.getName()).append(" - ")
                    .append(shelter.getCurrentOccupancy()).append("/").append(shelter.getCapacity())
                    .append(" (").append(shelter.getAvailableCapacity()).append(" free)\n");
        }
        return sb.toString();
    }

    private JPanel buildRouteCard() {
        fromCombo = new JComboBox<String>();
        toCombo = new JComboBox<String>();
        distanceOption = new JRadioButton("Shortest distance", true);
        timeOption = new JRadioButton("Fastest time");
        distanceOption.setOpaque(false);
        timeOption.setOpaque(false);
        distanceOption.setFont(Theme.NORMAL);
        timeOption.setFont(Theme.NORMAL);
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        group.add(distanceOption);
        group.add(timeOption);

        JButton find = Theme.primaryButton("Find Best Route");
        JButton alternatives = Theme.ghostButton("Alternative Routes (DFS)");
        JButton nearest = Theme.successButton("Nearest Shelters (from 'From')");

        find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                findRoute();
            }
        });
        alternatives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                findAlternatives();
            }
        });
        nearest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                findNearestShelters();
            }
        });

        JPanel controls = UiUtil.toolbar(new JLabel("From:"), fromCombo, new JLabel("To:"), toCombo,
                distanceOption, timeOption, find, alternatives, nearest);

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BACKGROUND);
        card.add(UiUtil.card(controls, "Emergency Route Finder (Dijkstra)"), BorderLayout.NORTH);
        return card;
    }

    private void addLocationDialog() {
        javax.swing.JTextField nameField = new javax.swing.JTextField();
        javax.swing.JComboBox<String> typeCombo = new javax.swing.JComboBox<String>(new String[] {
                Location.TYPE_DISASTER_ZONE, Location.TYPE_SHELTER, Location.TYPE_HOSPITAL,
                Location.TYPE_FIRE_STATION, Location.TYPE_POLICE_STATION,
                Location.TYPE_EMERGENCY_CENTER, Location.TYPE_JUNCTION});
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Location name:"));
        panel.add(nameField);
        panel.add(new JLabel("Location type:"));
        panel.add(typeCombo);
        int result = javax.swing.JOptionPane.showConfirmDialog(mainFrame, panel, "Add Location",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        try {
            network.addLocation(nameField.getText(), UiUtil.comboValue(typeCombo));
            UiUtil.success(mainFrame, "Location added. New locations get a map position automatically.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void addRoadDialog() {
        List<Location> locations = routeService.allLocations();
        if (locations.size() < 2) {
            UiUtil.info(mainFrame, "Add at least two locations first.");
            return;
        }
        javax.swing.JComboBox<String> from = new javax.swing.JComboBox<String>();
        javax.swing.JComboBox<String> to = new javax.swing.JComboBox<String>();
        for (int i = 0; i < locations.size(); i++) {
            from.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
            to.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }
        javax.swing.JTextField distance = new javax.swing.JTextField();
        javax.swing.JTextField time = new javax.swing.JTextField();
        javax.swing.JTextField name = new javax.swing.JTextField();
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("From:"));
        panel.add(from);
        panel.add(new JLabel("To:"));
        panel.add(to);
        panel.add(new JLabel("Distance (km):"));
        panel.add(distance);
        panel.add(new JLabel("Travel time (minutes):"));
        panel.add(time);
        panel.add(new JLabel("Road name:"));
        panel.add(name);
        int result = javax.swing.JOptionPane.showConfirmDialog(mainFrame, panel, "Add Road",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        try {
            network.addRoad(idOf(from), idOf(to), distance.getText(), time.getText(), name.getText());
            UiUtil.success(mainFrame, "Road added to the network.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private String idOf(javax.swing.JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return labelToId.get(selected.toString());
    }

    private RoadSegment selectedRoad() {
        int row = roadsTable.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a road in the table first.");
            return null;
        }
        int modelRow = roadsTable.convertRowIndexToModel(row);
        String roadName = String.valueOf(roadsModel.getValueAt(modelRow, 0));
        String fromName = String.valueOf(roadsModel.getValueAt(modelRow, 1));
        List<RoadSegment> roads = routeService.allRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            if (road.getName().equals(roadName)
                    && routeService.locationName(road.getFromId()).equals(fromName)) {
                return road;
            }
        }
        return null;
    }

    private void toggleBlock() {
        RoadSegment road = selectedRoad();
        if (road == null) {
            return;
        }
        boolean blocking = !road.isBlocked();
        if (blocking) {
            List<RoadSegment> critical = network.criticalRoadsFor(road.getFromId(), road);
            if (!critical.isEmpty()) {
                StringBuilder warning = new StringBuilder();
                warning.append("Warning: blocking ").append(road.getName())
                        .append(" may disconnect parts of the network from ")
                        .append(routeService.locationName(road.getFromId())).append(".\n");
                if (!UiUtil.confirm(mainFrame, warning.toString() + "Block this road anyway?")) {
                    return;
                }
            }
        }
        network.setRoadStatus(road, blocking ? RoadSegment.STATUS_BLOCKED : RoadSegment.STATUS_OPEN);
        refresh();
    }

    private void deleteRoad() {
        RoadSegment road = selectedRoad();
        if (road == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete road " + road.getName() + "?")) {
            return;
        }
        network.deleteRoad(road);
        refresh();
    }

    private void showImpact() {
        String startId = idOf(fromCombo);
        if (startId == null) {
            UiUtil.info(mainFrame, "Select a 'From' location first.");
            return;
        }
        List<String> reachable = routeService.blockedRoadImpact(startId);
        List<String> unreachable = new ArrayList<String>();
        List<Location> all = routeService.allLocations();
        for (int i = 0; i < all.size(); i++) {
            if (!reachable.contains(all.get(i).getId())) {
                unreachable.add(all.get(i).getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("BLOCKED-ROAD IMPACT (BFS reachability)\n\n");
        sb.append("From ").append(routeService.locationName(startId))
                .append(" you can reach ").append(reachable.size())
                .append(" of ").append(all.size()).append(" locations using open roads.\n\n");
        if (unreachable.isEmpty()) {
            sb.append("Every location is reachable. Network is fully connected.");
        } else {
            sb.append("UNREACHABLE locations:\n");
            for (int i = 0; i < unreachable.size(); i++) {
                sb.append(" - ").append(unreachable.get(i)).append("\n");
            }
        }
        routeOutput.setText(sb.toString());
    }

    private void findRoute() {
        String fromId = idOf(fromCombo);
        String toId = idOf(toCombo);
        if (fromId == null || toId == null) {
            UiUtil.error(mainFrame, "Select both a start and a destination location.");
            return;
        }
        if (fromId.equals(toId)) {
            UiUtil.info(mainFrame, "Start and destination are the same location.");
            return;
        }
        boolean timeMode = timeOption.isSelected();
        RouteResult result = routeService.findRoute(fromId, toId, timeMode);
        if (!result.isReachable()) {
            if (mapCanvas != null) {
                mapCanvas.setHighlightedRoute(null, timeMode);
            }
            routeOutput.setText("NO ROUTE FOUND\n\nAll paths between " + routeService.locationName(fromId)
                    + " and " + routeService.locationName(toId)
                    + " are blocked. Check the road network or use the BFS impact analysis.");
            return;
        }
        if (mapCanvas != null) {
            mapCanvas.setHighlightedRoute(result, timeMode);
        }
        routeOutput.setText(describeRoute(result, timeMode, "BEST ROUTE (Dijkstra)"));
    }

    private void findAlternatives() {
        String fromId = idOf(fromCombo);
        String toId = idOf(toCombo);
        if (fromId == null || toId == null) {
            UiUtil.error(mainFrame, "Select both a start and a destination location.");
            return;
        }
        boolean timeMode = timeOption.isSelected();
        List<RouteResult> results = routeService.alternativeRoutes(fromId, toId, timeMode, 3);
        if (results.isEmpty()) {
            routeOutput.setText("No open route exists between these locations.");
            return;
        }
        if (mapCanvas != null) {
            mapCanvas.setHighlightedRoute(results.get(0), timeMode);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            String title = i == 0 ? "OPTION 1 - BEST ROUTE (Dijkstra)" : "OPTION " + (i + 1) + " - ALTERNATIVE (DFS)";
            sb.append(describeRoute(results.get(i), timeMode, title));
            if (i < results.size() - 1) {
                sb.append("\n");
            }
        }
        routeOutput.setText(sb.toString());
    }

    private void findNearestShelters() {
        String fromId = idOf(fromCombo);
        if (fromId == null) {
            UiUtil.error(mainFrame, "Select a 'From' location first.");
            return;
        }
        boolean timeMode = timeOption.isSelected();
        List<sdrs.model.Shelter> shelters = routeService.nearestShelters(fromId, 1, timeMode, 3);
        StringBuilder sb = new StringBuilder();
        sb.append("NEAREST SHELTERS WITH FREE CAPACITY from ")
                .append(routeService.locationName(fromId)).append(" (greedy by ")
                .append(timeMode ? "time" : "distance").append(")\n\n");
        if (shelters.isEmpty()) {
            sb.append("No reachable shelter currently has free capacity.");
        }
        for (int i = 0; i < shelters.size(); i++) {
            sdrs.model.Shelter shelter = shelters.get(i);
            RouteResult route = routeService.findRoute(fromId, shelter.getLocationId(), timeMode);
            sb.append(i + 1).append(". ").append(shelter.getName()).append(" [")
                    .append(shelter.getStatus()).append(", ").append(shelter.getAvailableCapacity())
                    .append(" free] - ").append(route.summary(timeMode)).append("\n");
        }
        routeOutput.setText(sb.toString());
    }

    private String describeRoute(RouteResult result, boolean byTime, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" ===\n");
        List<String> pathIds = result.getPathIds();
        sb.append(routeService.locationName(pathIds.get(0)));
        for (int i = 1; i < pathIds.size(); i++) {
            sdrs.algorithm.RouteStep step = result.getSteps().get(i - 1);
            sb.append("\n   |  ").append(String.format("%.1f km", step.getDistanceKm()))
                    .append(", ").append(String.format("%.0f min", step.getTravelMinutes()))
                    .append("\n> ").append(routeService.locationName(pathIds.get(i)));
        }
        sb.append("\n\nTOTAL: ").append(String.format("%.1f km", result.getTotalDistanceKm()))
                .append(", ").append(String.format("%.0f min", result.getTotalMinutes()))
                .append(byTime ? " (optimized for time)" : " (optimized for distance)").append("\n\n");
        return sb.toString();
    }

    public void refresh() {
        List<Location> locations = routeService.allLocations();
        Object fromSelected = fromCombo == null ? null : fromCombo.getSelectedItem();
        Object toSelected = toCombo == null ? null : toCombo.getSelectedItem();
        fromCombo.removeAllItems();
        toCombo.removeAllItems();
        labelToId.clear();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            String label = location.getId() + " - " + location.getName();
            labelToId.put(label, location.getId());
            fromCombo.addItem(label);
            toCombo.addItem(label);
        }
        restoreSelection(fromCombo, fromSelected);
        restoreSelection(toCombo, toSelected);

        UiUtil.clearRows(roadsModel);
        List<RoadSegment> roads = routeService.allRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            UiUtil.addRow(roadsModel, new Object[] {
                    road.getName(), routeService.locationName(road.getFromId()),
                    routeService.locationName(road.getToId()),
                    String.format("%.1f", Double.valueOf(road.getDistanceKm())),
                    String.format("%.0f", Double.valueOf(road.getTravelMinutes())),
                    road.getStatus()});
        }
        UiUtil.packColumns(roadsTable);
        if (mapCanvas != null) {
            mapCanvas.reload();
        }
    }

    private void restoreSelection(javax.swing.JComboBox<String> combo, Object previous) {
        if (previous != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (previous.equals(combo.getItemAt(i))) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }
}
