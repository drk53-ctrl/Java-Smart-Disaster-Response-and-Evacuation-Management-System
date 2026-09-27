package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sdrs.algorithm.RouteResult;
import sdrs.model.Location;
import sdrs.model.RoadSegment;
import sdrs.model.Shelter;
import sdrs.service.AppContext;
import sdrs.service.RouteService;
import sdrs.ui.MainFrame;
import sdrs.ui.MapCanvas;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class EmergencyMapPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final MainFrame mainFrame;
    private final RouteService routes = AppContext.get().routes();

    private MapCanvas canvas;
    private JComboBox<String> sourceCombo;
    private JComboBox<String> destinationCombo;
    private JTextArea routeResult;
    private JTextArea detailsArea;
    private JLabel routeModeLabel;
    private final java.util.Map<String, String> labelToId = new java.util.HashMap<String, String>();

    public EmergencyMapPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildGuideBar(), BorderLayout.NORTH);

        canvas = new MapCanvas(routes);
        canvas.setSelectionListener(new MapCanvas.SelectionListener() {
            public void locationSelected(Location location) {
                showLocationDetails(location);
            }
        });

        JPanel mapHolder = new JPanel(new BorderLayout());
        mapHolder.setOpaque(false);
        mapHolder.add(buildZoomBar(), BorderLayout.NORTH);
        javax.swing.JScrollPane mapScroll = new javax.swing.JScrollPane(canvas,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mapScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        mapScroll.getVerticalScrollBar().setUnitIncrement(24);
        mapScroll.getHorizontalScrollBar().setUnitIncrement(24);
        mapHolder.add(mapScroll, BorderLayout.CENTER);

        JPanel mapCard = UiUtil.card(mapHolder,
                "Operations Map  \u00B7  circles = locations  \u00B7  lines = roads");

        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.setOpaque(false);
        east.add(buildStep1Card());
        east.add(stepGap());
        east.add(buildStep2Card());
        east.add(stepGap());
        east.add(buildDetailsCard());
        east.add(stepGap());
        east.add(buildRouteResultCard());
        east.add(stepGap());
        east.add(buildLegendHintCard());

        JScrollPane eastScroll = new JScrollPane(east);
        eastScroll.setBorder(BorderFactory.createEmptyBorder());
        eastScroll.getVerticalScrollBar().setUnitIncrement(16);
        eastScroll.setPreferredSize(new Dimension(360, 0));
        eastScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setBackground(Theme.BACKGROUND);
        center.add(mapCard, BorderLayout.CENTER);
        center.add(eastScroll, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        showLocationDetails(null);
        refresh();
        AppContext.get().dataManager().addListener(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    private Component stepGap() {
        return javax.swing.Box.createVerticalStrut(10);
    }

    private JPanel buildZoomBar() {
        javax.swing.JButton zoomIn = Theme.ghostButton("Zoom +");
        javax.swing.JButton zoomOut = Theme.ghostButton("Zoom \u2212");
        javax.swing.JButton zoomReset = Theme.ghostButton("Fit Map");
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                canvas.zoomIn();
            }
        });
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                canvas.zoomOut();
            }
        });
        zoomReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                canvas.zoomReset();
            }
        });
        return UiUtil.row(zoomIn, zoomOut, zoomReset);
    }

    private JPanel buildGuideBar() {
        JLabel guide = new JLabel("How to use:  1) pick Source and Destination   2) click FIND ROUTE   "
                + "3) read the highlighted path, distance and time on the map");
        guide.setFont(Theme.NORMAL);
        guide.setForeground(Theme.TEXT);
        return UiUtil.toolbar(guide);
    }

    private JPanel buildStep1Card() {
        sourceCombo = new JComboBox<String>();
        destinationCombo = new JComboBox<String>();

        JButton find = Theme.primaryButton("FIND ROUTE");
        JButton swap = Theme.ghostButton("Swap");
        JButton clear = Theme.ghostButton("Clear");
        JButton nearest = Theme.successButton("Nearest Shelter");

        find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                findRoute();
            }
        });
        swap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                Object source = sourceCombo.getSelectedItem();
                sourceCombo.setSelectedItem(destinationCombo.getSelectedItem());
                destinationCombo.setSelectedItem(source);
            }
        });
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                canvas.clearRoute();
                routeResult.setText("");
            }
        });
        nearest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                findNearestShelter();
            }
        });

        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.add(sourceRow());
        form.add(javax.swing.Box.createVerticalStrut(6));
        form.add(destinationRow());

        JPanel buttons = UiUtil.row(find, swap, clear, nearest);

        JPanel card = UiUtil.card(wrap(form, buttons), "Step 1 \u00B7 Choose Source and Destination");
        return card;
    }

    private javax.swing.JComponent wrap(javax.swing.JComponent top, javax.swing.JComponent bottom) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel sourceRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel label = new JLabel("Source:");
        label.setPreferredSize(new Dimension(110, 24));
        row.add(label, BorderLayout.WEST);
        row.add(sourceCombo, BorderLayout.CENTER);
        return row;
    }

    private JPanel destinationRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel label = new JLabel("Destination:");
        label.setPreferredSize(new Dimension(110, 24));
        row.add(label, BorderLayout.WEST);
        row.add(destinationCombo, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildStep2Card() {
        routeModeLabel = new JLabel("Route mode: fastest time (switch in Route Planning for distance mode)");
        routeModeLabel.setFont(Theme.SMALL);
        routeModeLabel.setForeground(Theme.TEXT_LIGHT);
        JButton openPlanner = Theme.ghostButton("Open Route Planning (edit roads, block roads, distance mode)");
        openPlanner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                mainFrame.showPanel("Route Planning");
            }
        });
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        box.add(routeModeLabel, BorderLayout.NORTH);
        box.add(openPlanner, BorderLayout.SOUTH);
        return UiUtil.card(box, "Step 2 \u00B7 Advanced Network Tools");
    }

    private JPanel buildDetailsCard() {
        detailsArea = new JTextArea(8, 30);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(Theme.NORMAL);
        detailsArea.setBackground(Theme.INPUT);
        detailsArea.setForeground(Theme.TEXT);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return UiUtil.card(new JScrollPane(detailsArea), "Selected Location (click a circle on the map)");
    }

    private JPanel buildRouteResultCard() {
        routeResult = new JTextArea(9, 30);
        routeResult.setEditable(false);
        routeResult.setLineWrap(true);
        routeResult.setWrapStyleWord(true);
        routeResult.setFont(Theme.NORMAL);
        routeResult.setBackground(Theme.INPUT);
        routeResult.setForeground(Theme.TEXT);
        routeResult.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return UiUtil.card(new JScrollPane(routeResult), "Route Result");
    }

    private JPanel buildLegendHintCard() {
        JLabel hint = new JLabel("Legend is drawn on the map (top-right). Yellow path = current route, "
                + "dashed red road = blocked, boxed labels on roads = km and minutes.");
        hint.setFont(Theme.SMALL);
        hint.setForeground(Theme.TEXT_LIGHT);
        return UiUtil.card(hint, "Map Symbols");
    }

    private void showLocationDetails(Location location) {
        StringBuilder sb = new StringBuilder();
        if (location == null) {
            sb.append("No location selected.\n\nClick any circle on the map to see its details here.");
        } else {
            sb.append("Name: ").append(location.getName()).append("\n");
            sb.append("ID: ").append(location.getId()).append("\n");
            sb.append("Type: ").append(location.getType()).append("\n");
            List<Shelter> shelters = routes.sheltersAt(location.getId());
            for (int i = 0; i < shelters.size(); i++) {
                Shelter shelter = shelters.get(i);
                sb.append("\nShelter: ").append(shelter.getName()).append("\n");
                sb.append("Capacity: ").append(shelter.getCapacity()).append("\n");
                sb.append("Occupied: ").append(shelter.getCurrentOccupancy()).append("\n");
                sb.append("Available: ").append(shelter.getAvailableCapacity()).append("\n");
                sb.append("Status: ").append(shelter.getStatus()).append("\n");
            }
            List<String> disasterIds = routes.activeDisasterIdsAt(location.getId());
            for (int i = 0; i < disasterIds.size(); i++) {
                sdrs.model.Disaster disaster = (sdrs.model.Disaster) AppContext.get().dataManager()
                        .disasterById(disasterIds.get(i));
                if (disaster != null) {
                    sb.append("\nDisaster: ").append(disaster.getType()).append("\n");
                    sb.append("Severity: ").append(disaster.getSeverity()).append("\n");
                    sb.append("Affected Citizens: ").append(disaster.getAffectedPeople()).append("\n");
                    sb.append("Status: ").append(disaster.getStatus()).append("\n");
                    sb.append("Priority Score: ").append(disaster.getPriorityScore()).append("\n");
                }
            }
            List<RoadSegment> roads = routes.roadsTouching(location.getId());
            sb.append("\nRoads from here (").append(roads.size()).append("):\n");
            for (int i = 0; i < roads.size(); i++) {
                RoadSegment road = roads.get(i);
                sb.append("  ").append(road.getName()).append(" to ")
                        .append(routes.locationName(road.otherEnd(location.getId())))
                        .append(String.format(" - %.1f km, %.0f min%s\n",
                                Double.valueOf(road.getDistanceKm()),
                                Double.valueOf(road.getTravelMinutes()),
                                road.isBlocked() ? " [BLOCKED]" : ""));
            }
            List<sdrs.model.EmergencyTeam> teams = routes.teamsAt(location.getId());
            for (int i = 0; i < teams.size(); i++) {
                sb.append("Team here: ").append(teams.get(i).getName()).append("\n");
            }
            List<sdrs.model.Vehicle> vehicles = routes.vehiclesAt(location.getId());
            for (int i = 0; i < vehicles.size(); i++) {
                sb.append("Vehicle here: ").append(vehicles.get(i).getType()).append(" ")
                        .append(vehicles.get(i).getPlateNumber()).append("\n");
            }
        }
        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    private void findRoute() {
        String fromId = selectedId(sourceCombo);
        String toId = selectedId(destinationCombo);
        if (fromId == null || toId == null) {
            UiUtil.error(mainFrame, "Choose both a source and a destination first.");
            return;
        }
        if (fromId.equals(toId)) {
            UiUtil.info(mainFrame, "Source and destination are the same location.");
            return;
        }
        RouteResult result = routes.findRoute(fromId, toId, true);
        canvas.setHighlightedRoute(result.isReachable() ? result : null, true);
        if (!result.isReachable()) {
            routeResult.setText("NO ROUTE FOUND\n\nEvery path between " + routes.locationName(fromId)
                    + " and " + routes.locationName(toId) + " is blocked.\n"
                    + "Open roads again in Route Planning, then retry.");
            return;
        }
        routeResult.setText(formatRoute(result));
    }

    private String formatRoute(RouteResult result) {
        StringBuilder sb = new StringBuilder();
        List<String> path = result.getPathIds();
        sb.append("Route:\n");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append("  \u2192  ");
            }
            sb.append(routes.locationName(path.get(i)));
        }
        sb.append("\n\nTotal Distance:\n  ").append(String.format("%.1f km",
                Double.valueOf(result.getTotalDistanceKm())));
        sb.append("\n\nEstimated Time:\n  ").append(String.format("%.0f minutes",
                Double.valueOf(result.getTotalMinutes())));
        sb.append("\n\nAlgorithm: Dijkstra (optimized for time).\n"
                + "The yellow highlighted path on the map is this route.");
        return sb.toString();
    }

    private void findNearestShelter() {
        String fromId = selectedId(sourceCombo);
        if (fromId == null) {
            UiUtil.error(mainFrame, "Choose a source location first (the disaster zone).");
            return;
        }
        List<Shelter> shelters = routes.nearestShelters(fromId, 1, true, 1);
        if (shelters.isEmpty()) {
            routeResult.setText("NO SHELTER AVAILABLE\n\nNo reachable shelter has free capacity "
                    + "right now. Check the Shelters module.");
            canvas.clearRoute();
            return;
        }
        Shelter shelter = shelters.get(0);
        RouteResult result = routes.findRoute(fromId, shelter.getLocationId(), true);
        canvas.setHighlightedRoute(result.isReachable() ? result : null, true);
        StringBuilder sb = new StringBuilder();
        sb.append("NEAREST SHELTER (greedy search over Dijkstra times)\n\n");
        sb.append("Best choice: ").append(shelter.getName()).append("\n");
        sb.append("Available places: ").append(shelter.getAvailableCapacity()).append("\n");
        sb.append("Status: ").append(shelter.getStatus()).append("\n\n");
        sb.append(formatRoute(result));
        routeResult.setText(sb.toString());
    }

    private String selectedId(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return labelToId.get(selected.toString());
    }

    public void refresh() {
        Object source = sourceCombo == null ? null : sourceCombo.getSelectedItem();
        Object destination = destinationCombo == null ? null : destinationCombo.getSelectedItem();
        sourceCombo.removeAllItems();
        destinationCombo.removeAllItems();
        labelToId.clear();
        List<Location> locations = routes.allLocations();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            String label = location.getName() + "  [" + location.getType() + "]";
            labelToId.put(label, location.getId());
            sourceCombo.addItem(label);
            destinationCombo.addItem(label);
        }
        restore(sourceCombo, source);
        restore(destinationCombo, destination);
        canvas.reload();
        showLocationDetails(canvas.getSelected());
    }

    private void restore(JComboBox<String> combo, Object previous) {
        if (previous == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (previous.equals(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
