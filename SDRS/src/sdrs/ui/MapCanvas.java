package sdrs.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import sdrs.algorithm.RouteResult;
import sdrs.model.Location;
import sdrs.model.RoadSegment;

public class MapCanvas extends JPanel implements javax.swing.Scrollable {

    private static final long serialVersionUID = 1L;

    public interface SelectionListener {
        void locationSelected(Location location);
    }

    private static final int NODE_RADIUS = 16;
    private static final int LABEL_HEIGHT = 17;
    private static final int PAD = 34;
    private static final double ZOOM_MIN = 0.5;
    private static final double ZOOM_MAX = 3.0;
    private static final Dimension BASE_SIZE = new Dimension(760, 520);

    private final sdrs.service.RouteService routes;
    private final Map<String, double[]> virtual = new HashMap<String, double[]>();
    private double minX = 0;
    private double maxX = 100;
    private double minY = 0;
    private double maxY = 100;
    private double zoom = 1.0;
    private Location selected;
    private Location hover;
    private String sourceId;
    private String destinationId;
    private RouteResult highlightedRoute;
    private SelectionListener listener;

    public MapCanvas(sdrs.service.RouteService routes) {
        this.routes = routes;
        setBackground(new Color(10, 10, 10));
        setPreferredSize(BASE_SIZE);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                Location hit = hitTest(event.getX(), event.getY());
                selected = hit;
                if (listener != null) {
                    listener.locationSelected(hit);
                }
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event) {
                Location hit = hitTest(event.getX(), event.getY());
                if (hit != hover) {
                    hover = hit;
                    setCursor(hit == null ? Cursor.getDefaultCursor()
                            : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        });
        addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent event) {
                if (event.getWheelRotation() < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }

    public void setSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }

    public void reload() {
        virtual.clear();
        List<Location> locations = routes.allLocations();
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;
        int placed = 0;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            double[] position;
            if (location.hasMapPosition()) {
                position = new double[] {location.getX(), location.getY()};
            } else {
                double angle = (Math.PI * 2 * i) / Math.max(1, locations.size());
                position = new double[] {50 + 34 * Math.cos(angle), 50 + 34 * Math.sin(angle)};
            }
            virtual.put(location.getId(), position);
            minX = Math.min(minX, position[0]);
            maxX = Math.max(maxX, position[0]);
            minY = Math.min(minY, position[1]);
            maxY = Math.max(maxY, position[1]);
            placed++;
        }
        if (placed == 0) {
            minX = 0;
            maxX = 100;
            minY = 0;
            maxY = 100;
        }
        if (maxX - minX < 1e-6) {
            minX -= 10;
            maxX += 10;
        }
        if (maxY - minY < 1e-6) {
            minY -= 10;
            maxY += 10;
        }
        boolean allSamePoint = (maxX - minX) < 12 && (maxY - minY) < 12;
        if (allSamePoint) {
            minX = Math.min(0, minX - 10);
            minY = Math.min(0, minY - 10);
            maxX = Math.max(100, maxX + 10);
            maxY = Math.max(100, maxY + 10);
            for (int i = 0; i < locations.size(); i++) {
                Location location = locations.get(i);
                double angle = (Math.PI * 2 * i) / Math.max(1, locations.size());
                virtual.put(location.getId(), new double[] {
                        50 + 34 * Math.cos(angle), 50 + 34 * Math.sin(angle)});
            }
        }
        dropMissingReferences();
        repaint();
    }

    private void dropMissingReferences() {
        if (selected != null && findLocation(selected.getId()) == null) {
            selected = null;
        }
        if (sourceId != null && findLocation(sourceId) == null) {
            sourceId = null;
        }
        if (destinationId != null && findLocation(destinationId) == null) {
            destinationId = null;
        }
        if (highlightedRoute != null) {
            List<String> path = highlightedRoute.getPathIds();
            for (int i = 0; i < path.size(); i++) {
                if (findLocation(path.get(i)) == null) {
                    highlightedRoute = null;
                    break;
                }
            }
        }
    }

    private Location findLocation(String id) {
        List<Location> locations = routes.allLocations();
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getId().equals(id)) {
                return locations.get(i);
            }
        }
        return null;
    }

    public void setHighlightedRoute(RouteResult route, boolean timeMode) {
        this.highlightedRoute = route;
        if (route != null && route.isReachable() && route.getPathIds().size() >= 2) {
            sourceId = route.getPathIds().get(0);
            destinationId = route.getPathIds().get(route.getPathIds().size() - 1);
        }
        repaint();
    }

    public void clearRoute() {
        highlightedRoute = null;
        sourceId = null;
        destinationId = null;
        repaint();
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public Location getSelected() {
        return selected;
    }

    public void clearSelection() {
        selected = null;
        repaint();
    }

    public double getZoom() {
        return zoom;
    }

    public java.awt.Point screenPositionOf(String locationId) {
        int[] point = toScreen(locationId);
        return point == null ? null : new java.awt.Point(point[0], point[1]);
    }

    public void zoomIn() {
        setZoom(zoom * 1.25);
    }

    public void zoomOut() {
        setZoom(zoom / 1.25);
    }

    public void zoomReset() {
        setZoom(1.0);
    }

    public void setZoom(double value) {
        double clamped = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, value));
        if (Math.abs(clamped - zoom) < 1e-9) {
            return;
        }
        zoom = clamped;
        revalidate();
        repaint();
    }

    private int[] toScreen(String id) {
        double[] pos = virtual.get(id);
        if (pos == null) {
            return null;
        }
        int w = Math.max(getWidth(), 1);
        int h = Math.max(getHeight(), 1);
        double drawW = Math.max(1, w - 2 * PAD);
        double drawH = Math.max(1, h - 2 * PAD);
        double spanX = Math.max(1e-6, maxX - minX);
        double spanY = Math.max(1e-6, maxY - minY);
        double nx = (pos[0] - minX) / spanX;
        double ny = (pos[1] - minY) / spanY;
        double sx = PAD + nx * drawW;
        double sy = PAD + ny * drawH;
        double cx = w / 2.0;
        double cy = h / 2.0;
        return new int[] {
                (int) Math.round(cx + (sx - cx) * zoom),
                (int) Math.round(cy + (sy - cy) * zoom)};
    }

    private Location hitTest(int px, int py) {
        List<Location> locations = routes.allLocations();
        Location bestLocation = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            int[] point = toScreen(location.getId());
            if (point == null) {
                continue;
            }
            double distance = Math.hypot(point[0] - px, point[1] - py);
            if (distance <= NODE_RADIUS + 10 && distance < bestDistance) {
                bestDistance = distance;
                bestLocation = location;
            }
        }
        return bestLocation;
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintGrid(g);
        paintRoads(g);
        paintRoute(g);
        paintMarkers(g);
        paintLegend(g);
        paintCaption(g);
    }

    private void paintGrid(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 9));
        int w = getWidth();
        int h = getHeight();
        for (int x = 0; x < w; x += 48) {
            g.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 48) {
            g.drawLine(0, y, w, y);
        }
        g.setColor(new Color(255, 255, 255, 22));
        g.drawRect(1, 1, w - 3, h - 3);
    }

    private void paintRoads(Graphics2D g) {
        List<RoadSegment> roads = routes.allRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            int[] from = toScreen(road.getFromId());
            int[] to = toScreen(road.getToId());
            if (from == null || to == null) {
                continue;
            }
            boolean onRoute = onHighlightedRoad(road);
            if (road.isBlocked()) {
                g.setColor(new Color(225, 70, 70));
                float[] dash = {10f, 8f};
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10f, dash, 0f));
            } else if (onRoute) {
                g.setColor(Theme.ACCENT);
                g.setStroke(new BasicStroke(4.5f));
            } else {
                g.setColor(new Color(122, 122, 122));
                g.setStroke(new BasicStroke(2.2f));
            }
            g.drawLine(from[0], from[1], to[0], to[1]);
            if (!onRoute) {
                drawRoadLabel(g, road, from, to);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawRoadLabel(Graphics2D g, RoadSegment road, int[] from, int[] to) {
        if (Math.hypot(from[0] - to[0], from[1] - to[1]) < 90) {
            return;
        }
        g.setFont(Theme.SMALL);
        FontMetrics metrics = g.getFontMetrics();
        String distance = String.format("%.1f km", Double.valueOf(road.getDistanceKm()));
        String time = String.format("%.0f min", Double.valueOf(road.getTravelMinutes()));
        int midX = (from[0] + to[0]) / 2;
        int midY = (from[1] + to[1]) / 2;
        int boxWidth = Math.max(metrics.stringWidth(distance), metrics.stringWidth(time)) + 14;
        int boxHeight = metrics.getHeight() * 2 + 6;
        int boxX = midX - boxWidth / 2;
        int boxY = midY - boxHeight / 2;
        g.setColor(road.isBlocked() ? new Color(60, 20, 24) : new Color(10, 10, 10, 235));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g.setColor(road.isBlocked() ? new Color(225, 70, 70) : new Color(64, 64, 64));
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g.setColor(new Color(203, 213, 225));
        g.drawString(distance, midX - metrics.stringWidth(distance) / 2, boxY + metrics.getAscent());
        g.setColor(road.isBlocked() ? new Color(252, 165, 165) : new Color(148, 163, 184));
        g.drawString(road.isBlocked() ? "BLOCKED" : time,
                midX - metrics.stringWidth(road.isBlocked() ? "BLOCKED" : time) / 2,
                boxY + metrics.getHeight() + metrics.getAscent() + 1);
    }

    private boolean onHighlightedRoad(RoadSegment road) {
        if (highlightedRoute == null || !highlightedRoute.isReachable()) {
            return false;
        }
        List<String> path = highlightedRoute.getPathIds();
        for (int i = 1; i < path.size(); i++) {
            String a = path.get(i - 1);
            String b = path.get(i);
            if ((road.getFromId().equals(a) && road.getToId().equals(b))
                    || (road.getFromId().equals(b) && road.getToId().equals(a))) {
                return true;
            }
        }
        return false;
    }

    private void paintRoute(Graphics2D g) {
        if (highlightedRoute == null || !highlightedRoute.isReachable()) {
            return;
        }
        List<String> path = highlightedRoute.getPathIds();
        for (int i = 1; i < path.size(); i++) {
            int[] from = toScreen(path.get(i - 1));
            int[] to = toScreen(path.get(i));
            if (from == null || to == null) {
                continue;
            }
            g.setColor(new Color(255, 69, 0, 70));
            g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(from[0], from[1], to[0], to[1]);
            g.setColor(new Color(255, 110, 40));
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(from[0], from[1], to[0], to[1]);
            int midX = (from[0] + to[0]) / 2;
            int midY = (from[1] + to[1]) / 2;
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics metrics = g.getFontMetrics();
            String legText = String.format("%.1f km, %.0f min",
                    Double.valueOf(highlightedRoute.getSteps().get(i - 1).getDistanceKm()),
                    Double.valueOf(highlightedRoute.getSteps().get(i - 1).getTravelMinutes()));
            int textWidth = metrics.stringWidth(legText);
            g.setColor(new Color(255, 110, 40));
            g.fillRoundRect(midX - textWidth / 2 - 6, midY - 16, textWidth + 12, 19, 9, 9);
            g.setColor(new Color(20, 8, 2));
            g.drawString(legText, midX - textWidth / 2, midY - 2);
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void paintMarkers(Graphics2D g) {
        List<Location> locations = routes.allLocations();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            int[] point = toScreen(location.getId());
            if (point == null) {
                continue;
            }
            boolean isSelected = selected != null && selected.getId().equals(location.getId());
            boolean isHover = hover != null && hover.getId().equals(location.getId());
            boolean isSource = location.getId().equals(sourceId);
            boolean isDestination = location.getId().equals(destinationId);
            Color fill = nodeColor(location.getType());
            String glyph = nodeGlyph(location.getType());

            drawBadges(g, location, point);

            if (isSelected) {
                g.setColor(new Color(255, 69, 0, 130));
                g.fillOval(point[0] - NODE_RADIUS - 7, point[1] - NODE_RADIUS - 7,
                        (NODE_RADIUS + 7) * 2, (NODE_RADIUS + 7) * 2);
            } else if (isHover) {
                g.setColor(new Color(255, 255, 255, 42));
                g.fillOval(point[0] - NODE_RADIUS - 7, point[1] - NODE_RADIUS - 7,
                        (NODE_RADIUS + 7) * 2, (NODE_RADIUS + 7) * 2);
            }
            g.setColor(fill);
            g.fillOval(point[0] - NODE_RADIUS, point[1] - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g.setColor(isSelected ? Color.WHITE : new Color(8, 13, 25));
            g.setStroke(new BasicStroke(isSelected ? 2.8f : 1.6f));
            g.drawOval(point[0] - NODE_RADIUS, point[1] - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
            FontMetrics glyphMetrics = g.getFontMetrics();
            g.setColor(Color.WHITE);
            g.drawString(glyph, point[0] - glyphMetrics.stringWidth(glyph) / 2,
                    point[1] + glyphMetrics.getAscent() / 2 - 1);

            drawNameBelow(g, location, point);

            if (isSource) {
                drawTag(g, "START", point[0], point[1] + NODE_RADIUS + LABEL_HEIGHT + 20,
                        new Color(22, 163, 74));
            } else if (isDestination) {
                drawTag(g, "END", point[0], point[1] + NODE_RADIUS + LABEL_HEIGHT + 20, Theme.DANGER);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawBadges(Graphics2D g, Location location, int[] point) {
        boolean hasDisaster = !routes.activeDisasterIdsAt(location.getId()).isEmpty();
        int freeShelterPlaces = 0;
        List<sdrs.model.Shelter> shelters = routes.sheltersAt(location.getId());
        for (int i = 0; i < shelters.size(); i++) {
            freeShelterPlaces += Math.max(0, shelters.get(i).getAvailableCapacity());
        }
        int badgeY = point[1] - NODE_RADIUS - 22;
        if (badgeY < 4) {
            badgeY = 4;
        }
        int offset = 0;
        if (hasDisaster) {
            drawBadge(g, "ACTIVE DISASTER", point[0] - 4, badgeY, new Color(220, 38, 38));
            offset = -2;
        }
        if (freeShelterPlaces > 0) {
            drawBadge(g, "+" + freeShelterPlaces + " free", point[0] + 4 + offset, badgeY,
                    new Color(29, 78, 216));
        }
    }

    private void drawBadge(Graphics2D g, String text, int centerX, int topY, Color color) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text) + 10;
        int height = metrics.getHeight() + 2;
        g.setColor(color);
        g.fillRoundRect(centerX - width / 2, topY, width, height, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(text, centerX - width / 2 + 5, topY + metrics.getAscent());
    }

    private void drawNameBelow(Graphics2D g, Location location, int[] point) {
        String name = location.getName();
        g.setFont(Theme.NORMAL);
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(name);
        boolean isSelected = selected != null && selected.getId().equals(location.getId());
        boolean isSource = location.getId().equals(sourceId);
        boolean isDestination = location.getId().equals(destinationId);
        int pillY = point[1] + NODE_RADIUS + 4;
        g.setColor(isSelected ? new Color(255, 69, 0) : new Color(10, 10, 10, 235));
        g.fillRoundRect(point[0] - textWidth / 2 - 7, pillY, textWidth + 14, LABEL_HEIGHT, 8, 8);
        g.setColor(isSelected ? new Color(255, 110, 40)
                : (isSource ? new Color(74, 208, 106) : (isDestination ? Theme.DANGER
                        : new Color(64, 64, 64))));
        g.drawRoundRect(point[0] - textWidth / 2 - 7, pillY, textWidth + 14, LABEL_HEIGHT, 8, 8);
        g.setColor(isSelected ? Color.WHITE : new Color(226, 232, 240));
        g.drawString(name, point[0] - textWidth / 2, pillY + metrics.getAscent());
    }

    private void drawTag(Graphics2D g, String text, int centerX, int topY, Color color) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text) + 12;
        g.setColor(color);
        g.fillRoundRect(centerX - width / 2, topY, width, 15, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(text, centerX - width / 2 + 6, topY + 11);
    }

    private Color nodeColor(String type) {
        if (Location.TYPE_EMERGENCY_CENTER.equals(type)) {
            return new Color(74, 208, 106);
        }
        if (Location.TYPE_HOSPITAL.equals(type)) {
            return new Color(255, 59, 48);
        }
        if (Location.TYPE_SHELTER.equals(type)) {
            return new Color(74, 208, 106);
        }
        if (Location.TYPE_FIRE_STATION.equals(type)) {
            return new Color(255, 69, 0);
        }
        if (Location.TYPE_POLICE_STATION.equals(type)) {
            return new Color(154, 154, 154);
        }
        if (Location.TYPE_DISASTER_ZONE.equals(type)) {
            return new Color(255, 159, 28);
        }
        return new Color(92, 92, 92);
    }

    private String nodeGlyph(String type) {
        if (Location.TYPE_EMERGENCY_CENTER.equals(type)) {
            return "\u271A";
        }
        if (Location.TYPE_HOSPITAL.equals(type)) {
            return "H";
        }
        if (Location.TYPE_SHELTER.equals(type)) {
            return "\u26E9";
        }
        if (Location.TYPE_FIRE_STATION.equals(type)) {
            return "\u2666";
        }
        if (Location.TYPE_POLICE_STATION.equals(type)) {
            return "P";
        }
        if (Location.TYPE_DISASTER_ZONE.equals(type)) {
            return "\u26A0";
        }
        return "\u25CF";
    }

    private void paintLegend(Graphics2D g) {
        String[][] entries = {
                {"\u26A0", "Disaster Zone"},
                {"\u26E9", "Shelter"},
                {"H", "Hospital"},
                {"\u2666", "Fire Station"},
                {"P", "Police Station"},
                {"\u271A", "Emergency Center"},
                {"\u25CF", "Junction"},
                {"\u2500\u2500", "Road (km, min)"},
                {"- -", "Blocked road"},
                {"\u25CF\u2192\u25CF", "Selected route"}};
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics metrics = g.getFontMetrics();
        int rowHeight = 16;
        int boxWidth = 150;
        int boxHeight = 22 + entries.length * rowHeight;
        int boxX = Math.max(6, getWidth() - boxWidth - 10);
        int boxY = 10;
        g.setColor(new Color(10, 10, 10, 228));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        g.setColor(Theme.BORDER);
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.setColor(new Color(148, 163, 184));
        g.drawString("LEGEND", boxX + 10, boxY + 15);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i < entries.length; i++) {
            int row = boxY + 34 + i * rowHeight;
            g.setColor(nodeColorForLegend(entries[i][0]));
            g.drawString(entries[i][0], boxX + 10, row);
            g.setColor(new Color(203, 213, 225));
            g.drawString(entries[i][1], boxX + 44, row);
        }
    }

    private Color nodeColorForLegend(String glyph) {
        if ("\u271A".equals(glyph)) {
            return new Color(34, 197, 94);
        }
        if ("H".equals(glyph)) {
            return new Color(239, 68, 68);
        }
        if ("\u26E9".equals(glyph)) {
            return new Color(59, 130, 246);
        }
        if ("\u2666".equals(glyph)) {
            return new Color(249, 115, 22);
        }
        if ("P".equals(glyph)) {
            return new Color(99, 102, 241);
        }
        if ("\u26A0".equals(glyph)) {
            return new Color(250, 204, 21);
        }
        if ("- -".equals(glyph)) {
            return new Color(225, 70, 70);
        }
        if ("\u25CF\u2192\u25CF".equals(glyph)) {
            return new Color(253, 224, 71);
        }
        return new Color(148, 163, 184);
    }

    private void paintCaption(Graphics2D g) {
        g.setFont(Theme.SMALL);
        g.setColor(new Color(148, 163, 184));
        g.drawString("Click a circle to inspect its location \u00B7 roads show distance and time"
                + " \u00B7 scroll wheel or buttons to zoom", 12, getHeight() - 10);
    }

    public Dimension getPreferredScrollableViewportSize() {
        return BASE_SIZE;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 32;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 96;
    }

    public boolean getScrollableTracksViewportWidth() {
        return zoom <= 1.0;
    }

    public boolean getScrollableTracksViewportHeight() {
        return zoom <= 1.0;
    }

    public Dimension getPreferredSize() {
        if (zoom > 1.0 && getWidth() > 0 && getHeight() > 0) {
            return new Dimension((int) Math.round(getWidth() * zoom),
                    (int) Math.round(getHeight() * zoom));
        }
        return new Dimension(BASE_SIZE);
    }
}
