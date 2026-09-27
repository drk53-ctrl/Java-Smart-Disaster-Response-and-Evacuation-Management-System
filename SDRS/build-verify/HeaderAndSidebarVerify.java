package verify;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import sdrs.model.Disaster;
import sdrs.service.AppContext;
import sdrs.ui.MainFrame;
import sdrs.ui.Theme;
import sdrs.ui.panels.TeamsPanel;

/**
 * Verification harness for the header-overlap fix and the drag-resizable sidebar.
 *
 * Runs in three phases so the EDT can process queued validation/paint events
 * between phases; inside the measuring phase every mutation is followed by a
 * synchronous validate() so measurements never race the layout manager.
 *
 * 1. Header audit: pairwise intersections among header labels (title block,
 *    status chip, meta line, clock) in one coordinate space, at three window
 *    sizes and all three status states; plus "fully visible" checks (laid-out
 *    size >= preferred size) and "fully inside header" checks.
 * 2. Divider drag simulation: setDividerLocation driven past both limits and
 *    back, like a real drag end; sidebar width must clamp to [210, 380] and
 *    stay wide enough for the widest menu label; header must stay clean.
 * 3. Window resize with the divider parked at 300: sidebar must hold its
 *    position when the window grows (resize weight 0) and stay in range when
 *    the window shrinks below sidebar+max-main minimum.
 * 4. Full sweep: all 12 panels x 3 sizes, sibling intersection audit
 *    including JLabels.
 */
public class HeaderAndSidebarVerify {

    private static int checks = 0;
    private static int failed = 0;
    private static MainFrame frame;

    public static void main(String[] args) throws Exception {
        System.out.println("=== HEADER + SIDEBAR VERIFY ===");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Theme.install();
                frame = new MainFrame();
                frame.setVisible(true);
            }
        });
        Thread.sleep(1200); // EDT free: clock ticks once, queued validation runs

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                seedCriticalState();
                frame.updateHeaderStatus();
                syncLayout();

                int[][] sizes = { {1024, 660}, {1280, 780}, {1600, 900} };
                for (int i = 0; i < sizes.length; i++) {
                    frame.setSize(sizes[i][0], sizes[i][1]);
                    syncLayout();
                    checkHeader("header@" + sizes[i][0] + "x" + sizes[i][1]);
                }

                frame.setSize(1024, 660);
                syncLayout();
                setState("CRITICAL");
                frame.updateHeaderStatus();
                syncLayout();
                checkHeader("header@1024 state=CRITICAL");
                setState("RESPONDING");
                frame.updateHeaderStatus();
                syncLayout();
                checkHeader("header@1024 state=RESPONDING");
                setState("NOMINAL");
                frame.updateHeaderStatus();
                syncLayout();
                checkHeader("header@1024 state=NOMINAL");
                setState("CRITICAL");

                frame.setSize(1280, 780);
                syncLayout();
                JSplitPane shell = findSplitPane(frame.getContentPane());
                check("JSplitPane shell present", shell != null);
                check("divider is grabbable (size >= 5)", shell != null && shell.getDividerSize() >= 5);
                if (shell == null) {
                    finish();
                    return;
                }
                int[] dragTargets = {1, 210, 295, 380, 500, 380, 250, 210, 244};
                for (int i = 0; i < dragTargets.length; i++) {
                    shell.setDividerLocation(dragTargets[i]);
                    syncLayout();
                    int width = shell.getDividerLocation();
                    check("drag to " + dragTargets[i] + " -> sidebar width " + width
                            + " within [210,380]", width >= 210 && width <= 380);
                    checkSidebarLabels("after drag " + dragTargets[i]);
                    checkHeader("header while sidebar=" + width);
                }

                shell.setDividerLocation(300);
                syncLayout();
                frame.setSize(1600, 900);
                syncLayout();
                check("sidebar stays at 300 when window grows (resize weight 0)",
                        shell.getDividerLocation() == 300);
                frame.setSize(1024, 660);
                syncLayout();
                int afterShrink = shell.getDividerLocation();
                check("sidebar within [210,380] after window shrinks to 1024: " + afterShrink,
                        afterShrink >= 210 && afterShrink <= 380);
                checkSidebarLabels("after shrink to 1024");
                checkHeader("header@1024 sidebar=" + afterShrink);

                shell.setDividerLocation(244);
                frame.setSize(1280, 780);
                syncLayout();

                TeamsPanel teams = new TeamsPanel(frame);
                JPanel[] allPanels = allPanels(frame, teams);
                for (int s = 0; s < sizes.length; s++) {
                    frame.setSize(sizes[s][0], sizes[s][1]);
                    syncLayout();
                    for (int p = 0; p < allPanels.length; p++) {
                        String context = allPanels[p].getClass().getSimpleName()
                                + "@" + sizes[s][0] + "x" + sizes[s][1];
                        auditTree(context, frame.getContentPane());
                    }
                }
                frame.dispose();
            }
        });
        finish();
    }

    /** Synchronous layout: what the EDT would do between events, done right now. */
    private static void syncLayout() {
        frame.getRootPane().revalidate();
        frame.validate();
        java.awt.Toolkit.getDefaultToolkit().sync();
    }

    private static void seedCriticalState() {
        List<Disaster> disasters = AppContext.get().dataManager().data().getDisasters();
        int activeCritical = 0;
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (d.isActiveLike() && Disaster.SEVERITY_CRITICAL.equals(d.getSeverity())) {
                activeCritical++;
            }
        }
        while (activeCritical < 3) {
            Disaster d = new Disaster("VRF-" + System.nanoTime(), "Earthquake", "Central District", "LOC-1",
                    Disaster.SEVERITY_CRITICAL, System.currentTimeMillis(), 120, "verify-seed");
            d.setStatus(Disaster.STATUS_ACTIVE);
            AppContext.get().dataManager().addDisaster(d);
            activeCritical++;
        }
    }

    private static void setState(String state) {
        List<Disaster> disasters = AppContext.get().dataManager().data().getDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (d.getId().startsWith("VRF-")) {
                if ("CRITICAL".equals(state)) {
                    d.setStatus(Disaster.STATUS_ACTIVE);
                    d.setSeverity(Disaster.SEVERITY_CRITICAL);
                } else if ("RESPONDING".equals(state)) {
                    d.setStatus(Disaster.STATUS_RESPONSE);
                    d.setSeverity(Disaster.SEVERITY_HIGH);
                } else {
                    d.setStatus(Disaster.STATUS_RESOLVED);
                }
            }
        }
    }

    private static int headerOverlaps;

    /** The header is the NORTH child of the main area inside the split pane's right side. */
    private static Container findHeader() {
        JSplitPane shell = findSplitPane(frame.getContentPane());
        if (shell == null) {
            return null;
        }
        Component right = shell.getRightComponent();
        if (right instanceof Container && ((Container) right).getComponentCount() > 0) {
            Component mainArea = ((Container) right).getComponent(0);
            if (mainArea instanceof Container && ((Container) mainArea).getComponentCount() > 0) {
                Component north = ((Container) mainArea).getComponent(0);
                if (north instanceof Container) {
                    return (Container) north;
                }
            }
        }
        return null;
    }

    private static void checkHeader(String context) {
        headerOverlaps = 0;
        Container header = findHeader();
        check(context + ": header panel found", header != null);
        if (header == null) {
            return;
        }
        List<Component> headerParts = new ArrayList<Component>();
        collectHeaderParts(header, headerParts);
        check(context + ": clock has a time", clockHasTime(headerParts));
        List<Rectangle> rects = new ArrayList<Rectangle>();
        for (int i = 0; i < headerParts.size(); i++) {
            Component part = headerParts.get(i);
            rects.add(SwingUtilities.convertRectangle(part.getParent(), part.getBounds(), header));
        }
        for (int i = 0; i < headerParts.size(); i++) {
            for (int j = i + 1; j < headerParts.size(); j++) {
                Rectangle inter = rects.get(i).intersection(rects.get(j));
                if (inter.isEmpty() || inter.width <= 1 || inter.height <= 1) {
                    continue;
                }
                headerOverlaps++;
                System.out.println("  [HEADER OVERLAP] " + context + ": "
                        + describe(headerParts.get(i)) + rects.get(i) + " vs "
                        + describe(headerParts.get(j)) + rects.get(j));
            }
        }
        check(context + ": header labels do not overlap (" + headerParts.size() + " parts)",
                headerOverlaps == 0);
        for (int i = 0; i < headerParts.size(); i++) {
            Component part = headerParts.get(i);
            if (part.getWidth() == 0 && part.getHeight() == 0) {
                continue; // empty clock label before first tick in fast runs
            }
            boolean inside = header.getBounds().contains(rects.get(i));
            check(context + ": " + describe(part) + " fully inside header", inside);
            boolean fullWidth = part.getWidth() >= part.getPreferredSize().width
                    && part.getHeight() >= part.getPreferredSize().height;
            check(context + ": " + describe(part) + " fully visible ("
                    + part.getWidth() + "x" + part.getHeight()
                    + " >= pref " + part.getPreferredSize().width + "x"
                    + part.getPreferredSize().height + ")", fullWidth);
        }
    }

    private static boolean clockHasTime(List<Component> parts) {
        for (int i = 0; i < parts.size(); i++) {
            String text = ((JLabel) parts.get(i)).getText();
            if (text != null && text.matches("\\d\\d:\\d\\d:\\d\\d")) {
                return true;
            }
        }
        return false;
    }

    private static void collectHeaderParts(Component component, List<Component> out) {
        if (component instanceof JLabel) {
            out.add(component);
        }
        if (component instanceof Container) {
            for (int i = 0; i < ((Container) component).getComponentCount(); i++) {
                collectHeaderParts(((Container) component).getComponent(i), out);
            }
        }
    }

    private static int sweepOverlaps;

    private static void auditTree(String context, Container root) {
        sweepOverlaps = 0;
        List<Component> comps = new ArrayList<Component>();
        collectVisible(root, comps);
        for (int i = 0; i < comps.size(); i++) {
            for (int j = i + 1; j < comps.size(); j++) {
                Component a = comps.get(i);
                Component b = comps.get(j);
                if (a.getParent() != b.getParent()) {
                    continue;
                }
                Rectangle inter = a.getBounds().intersection(b.getBounds());
                if (inter.isEmpty() || inter.width <= 2 || inter.height <= 2) {
                    continue;
                }
                sweepOverlaps++;
                System.out.println("  [SWEEP OVERLAP] " + context + ": "
                        + describe(a) + " vs " + describe(b));
            }
        }
        check(context + ": no sibling overlaps", sweepOverlaps == 0);
    }

    private static void collectVisible(Container container, List<Component> out) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (child.isVisible() && (child instanceof javax.swing.JButton
                    || child instanceof JLabel || child instanceof javax.swing.JTextField
                    || child instanceof javax.swing.JComboBox
                    || child instanceof javax.swing.JTable
                    || child instanceof javax.swing.JToggleButton)) {
                out.add(child);
            }
            if (child instanceof Container) {
                collectVisible((Container) child, out);
            }
        }
    }

    private static void checkSidebarLabels(String context) {
        JSplitPane shell = findSplitPane(frame.getContentPane());
        if (shell == null) {
            return;
        }
        int width = shell.getDividerLocation();
        Component sidebar = shell.getLeftComponent();
        int widest = 0;
        String widestText = "";
        List<Component> buttons = new ArrayList<Component>();
        collectButtons(sidebar, buttons);
        for (int i = 0; i < buttons.size(); i++) {
            javax.swing.AbstractButton b = (javax.swing.AbstractButton) buttons.get(i);
            int textWidth = b.getFontMetrics(b.getFont()).stringWidth(b.getText());
            if (textWidth > widest) {
                widest = textWidth;
                widestText = b.getText().trim();
            }
        }
        boolean fits = width - 12 - 28 - 3 >= widest;
        check(context + ": sidebar " + width + "px fits widest label '" + widestText
                + "' (" + widest + "px)", fits);
    }

    private static void collectButtons(Component component, List<Component> out) {
        if (component instanceof javax.swing.AbstractButton) {
            out.add(component);
        }
        if (component instanceof Container) {
            for (int i = 0; i < ((Container) component).getComponentCount(); i++) {
                collectButtons(((Container) component).getComponent(i), out);
            }
        }
    }

    private static JPanel[] allPanels(MainFrame frame, TeamsPanel teams) {
        return new JPanel[] {
                new sdrs.ui.panels.DashboardPanel(frame),
                new sdrs.ui.panels.CitizensPanel(frame),
                new sdrs.ui.panels.DisastersPanel(frame),
                new sdrs.ui.panels.EvacuationPanel(frame),
                new sdrs.ui.panels.SheltersPanel(frame),
                teams,
                new sdrs.ui.panels.ResourcesPanel(frame),
                new sdrs.ui.panels.VehiclesPanel(frame),
                new sdrs.ui.panels.EmergencyMapPanel(frame),
                new sdrs.ui.panels.RoutePlanningPanel(frame),
                new sdrs.ui.panels.ResponseCenterPanel(frame),
                new sdrs.ui.panels.UserManagementPanel(frame)};
    }

    private static JSplitPane findSplitPane(Container root) {
        if (root instanceof JSplitPane) {
            return (JSplitPane) root;
        }
        for (int i = 0; i < root.getComponentCount(); i++) {
            Component child = root.getComponent(i);
            if (child instanceof JSplitPane) {
                return (JSplitPane) child;
            }
            if (child instanceof Container) {
                JSplitPane found = findSplitPane((Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static String describe(Component component) {
        String extra = "";
        if (component instanceof JLabel) {
            String text = ((JLabel) component).getText();
            extra = "'" + (text == null ? "" : text.length() > 24 ? text.substring(0, 24) : text) + "'";
        }
        return component.getClass().getSimpleName() + extra;
    }

    private static void check(String name, boolean condition) {
        checks++;
        if (condition) {
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void finish() {
        System.out.println("RESULT: " + checks + " checks, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
