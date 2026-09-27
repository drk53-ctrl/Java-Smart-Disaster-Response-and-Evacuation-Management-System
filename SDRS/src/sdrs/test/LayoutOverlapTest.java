package sdrs.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sdrs.service.AppContext;
import sdrs.ui.MainFrame;
import sdrs.ui.Theme;
import sdrs.ui.panels.CitizensPanel;
import sdrs.ui.panels.DashboardPanel;
import sdrs.ui.panels.DisastersPanel;
import sdrs.ui.panels.EmergencyMapPanel;
import sdrs.ui.panels.EvacuationPanel;
import sdrs.ui.panels.ResourcesPanel;
import sdrs.ui.panels.ResponseCenterPanel;
import sdrs.ui.panels.RoutePlanningPanel;
import sdrs.ui.panels.SheltersPanel;
import sdrs.ui.panels.TeamsPanel;
import sdrs.ui.panels.UserManagementPanel;
import sdrs.ui.panels.VehiclesPanel;

public class LayoutOverlapTest {

    private static int overlapCount = 0;
    private static int checkedPanels = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== SDRS LAYOUT OVERLAP AUDIT ===");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Theme.install();
                MainFrame mainFrame = new MainFrame();

                JPanel[] panels = new JPanel[] {
                        new DashboardPanel(mainFrame),
                        new CitizensPanel(mainFrame),
                        new DisastersPanel(mainFrame),
                        new EvacuationPanel(mainFrame),
                        new SheltersPanel(mainFrame),
                        new TeamsPanel(mainFrame),
                        new ResourcesPanel(mainFrame),
                        new VehiclesPanel(mainFrame),
                        new EmergencyMapPanel(mainFrame),
                        new RoutePlanningPanel(mainFrame),
                        new ResponseCenterPanel(mainFrame),
                        new UserManagementPanel(mainFrame)};

                int[][] sizes = {{1024, 660}, {1280, 780}, {1600, 900}};
                for (int s = 0; s < sizes.length; s++) {
                    mainFrame.setSize(sizes[s][0], sizes[s][1]);
                    for (int p = 0; p < panels.length; p++) {
                        panels[p].setBounds(0, 0, sizes[s][0] - 260, sizes[s][1] - 60);
                        layoutDeep(panels[p]);
                        audit(panels[p].getClass().getSimpleName() + "@" + sizes[s][0] + "x" + sizes[s][1],
                                panels[p]);
                    }
                    checkedPanels += panels.length;
                }

                int sidebarWidth = 0;
                java.awt.Component sidebar = null;
                for (java.awt.Component component : mainFrame.getContentPane().getComponents()) {
                    int preferredWidth = component.getPreferredSize().width;
                    if (preferredWidth > 0 && preferredWidth < 400) {
                        sidebar = component;
                        sidebarWidth = preferredWidth;
                    }
                }
                java.util.List<JToggleButtonLike> buttons = new java.util.ArrayList<JToggleButtonLike>();
                if (sidebar instanceof Container) {
                    collectToggleButtons((Container) sidebar, buttons);
                }
                int widestItem = 0;
                for (int i = 0; i < buttons.size(); i++) {
                    widestItem = Math.max(widestItem, buttons.get(i).getWidth());
                }
                System.out.println("Sidebar preferred width: " + sidebarWidth
                        + ", widest menu item preferred width: " + widestItem
                        + " (" + buttons.size() + " items)");
                System.out.println(sidebarWidth >= widestItem
                        ? "[PASS] Sidebar wide enough for every label"
                        : "[FAIL] Sidebar too narrow for its labels");
                if (sidebarWidth < widestItem) {
                    overlapCount++;
                }
            }
        });
        System.out.println("AUDIT RESULT: " + checkedPanels + " panel-size combinations checked, "
                + overlapCount + " overlap problems found");
        if (overlapCount > 0) {
            System.exit(1);
        }
    }

    private interface JToggleButtonLike {
        int getWidth();
    }

    private static void collectToggleButtons(Container container, final List<JToggleButtonLike> out) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (child instanceof javax.swing.JToggleButton) {
                final javax.swing.JToggleButton button = (javax.swing.JToggleButton) child;
                button.doLayout();
                out.add(new JToggleButtonLike() {
                    public int getWidth() {
                        return button.getPreferredSize().width;
                    }
                });
            }
            if (child instanceof Container) {
                collectToggleButtons((Container) child, out);
            }
        }
    }

    private static void layoutDeep(Container container) {
        container.doLayout();
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (child instanceof Container) {
                layoutDeep((Container) child);
            }
        }
    }

    private static void audit(String context, Container root) {
        List<Component> interactive = new ArrayList<Component>();
        collect(root, interactive);
        for (int i = 0; i < interactive.size(); i++) {
            for (int j = i + 1; j < interactive.size(); j++) {
                Component a = interactive.get(i);
                Component b = interactive.get(j);
                if (!a.isVisible() || !b.isVisible()) {
                    continue;
                }
                if (a.getParent() != b.getParent()) {
                    continue;
                }
                Rectangle ra = a.getBounds();
                Rectangle rb = b.getBounds();
                Rectangle intersection = ra.intersection(rb);
                if (intersection.isEmpty() || intersection.width <= 2 || intersection.height <= 2) {
                    continue;
                }
                if (a instanceof JLabel && b instanceof JLabel) {
                    continue;
                }
                overlapCount++;
                System.out.println("[OVERLAP] " + context + ": "
                        + name(a) + " " + ra + " overlaps " + name(b) + " " + rb
                        + " by " + intersection.width + "x" + intersection.height);
            }
        }
    }

    private static void collect(Container container, List<Component> out) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (child instanceof JButton || child instanceof JLabel || child instanceof javax.swing.JTextField
                    || child instanceof javax.swing.JComboBox || child instanceof javax.swing.JTextArea
                    || child instanceof javax.swing.JTable || child instanceof javax.swing.JRadioButton
                    || child instanceof javax.swing.JToggleButton || child instanceof JPanel) {
                out.add(child);
            }
            if (child instanceof Container) {
                collect((Container) child, out);
            }
        }
    }

    private static String name(Component component) {
        return component.getClass().getSimpleName() + "('" + shortText(component) + "')";
    }

    private static String shortText(Component component) {
        if (component instanceof JButton) {
            return ((JButton) component).getText();
        }
        if (component instanceof JLabel) {
            String text = ((JLabel) component).getText();
            return text == null ? "" : (text.length() > 22 ? text.substring(0, 22) : text);
        }
        return "";
    }
}
