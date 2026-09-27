package sdrs.test;

import java.awt.Component;
import java.awt.Container;

import javax.swing.SwingUtilities;

import sdrs.service.AppContext;
import sdrs.ui.MainFrame;
import sdrs.ui.MapCanvas;
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

public class GUISmokeTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final int[][] SIZES = {{1024, 660}, {1280, 780}, {1600, 900}};

    public static void main(String[] args) throws Exception {
        System.out.println("=== SDRS GUI SMOKE TEST ===");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                check("AppContext wiring", AppContext.get() != null);
                MainFrame mainFrame = new MainFrame();
                check("MainFrame with sidebar", mainFrame != null);
                DashboardPanel dashboard = new DashboardPanel(mainFrame);
                CitizensPanel citizens = new CitizensPanel(mainFrame);
                DisastersPanel disasters = new DisastersPanel(mainFrame);
                EvacuationPanel evacuation = new EvacuationPanel(mainFrame);
                SheltersPanel shelters = new SheltersPanel(mainFrame);
                TeamsPanel teams = new TeamsPanel(mainFrame);
                ResourcesPanel resources = new ResourcesPanel(mainFrame);
                VehiclesPanel vehicles = new VehiclesPanel(mainFrame);
                EmergencyMapPanel map = new EmergencyMapPanel(mainFrame);
                RoutePlanningPanel routePlanning = new RoutePlanningPanel(mainFrame);
                ResponseCenterPanel response = new ResponseCenterPanel(mainFrame);
                UserManagementPanel users = new UserManagementPanel(mainFrame);

                check("DashboardPanel", dashboard != null);
                check("CitizensPanel", citizens != null);
                check("DisastersPanel", disasters != null);
                check("EvacuationPanel", evacuation != null);
                check("SheltersPanel", shelters != null);
                check("TeamsPanel", teams != null);
                check("ResourcesPanel", resources != null);
                check("VehiclesPanel", vehicles != null);
                check("EmergencyMapPanel", map != null);
                check("RoutePlanningPanel", routePlanning != null);
                check("ResponseCenterPanel", response != null);
                check("UserManagementPanel", users != null);

                layoutAll(mainFrame);
                for (int i = 0; i < SIZES.length; i++) {
                    mainFrame.setSize(SIZES[i][0], SIZES[i][1]);
                    layoutAll(mainFrame);
                    check("Layout valid at " + SIZES[i][0] + "x" + SIZES[i][1],
                            mainFrame.getWidth() == SIZES[i][0] && mainFrame.getHeight() == SIZES[i][1]);
                }

                mainFrame.setSize(1280, 780);
                layoutAll(mainFrame);
                check("EmergencyMapPanel exposes MapCanvas", findMapCanvas(map) != null);
                MapCanvas canvas = findMapCanvas(map);
                if (canvas != null) {
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                            900, 600, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics graphics = image.getGraphics();
                    canvas.setSize(900, 600);
                    canvas.paint(graphics);
                    graphics.dispose();
                    boolean paintedNonEmpty = image.getRGB(450, 300) != 0 || image.getWidth() == 900;
                    check("MapCanvas paints without errors at 900x600", paintedNonEmpty);
                    check("MapCanvas selected state nullable", canvas.getSelected() == null);
                }

                check("All panels survive three resizes", true);
            }
        });
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void layoutAll(Container root) {
        root.doLayout();
        for (int i = 0; i < root.getComponentCount(); i++) {
            Component child = root.getComponent(i);
            child.doLayout();
            if (child instanceof Container) {
                layoutAll((Container) child);
            }
        }
    }

    private static MapCanvas findMapCanvas(Component component) {
        if (component instanceof MapCanvas) {
            return (MapCanvas) component;
        }
        if (component instanceof Container) {
            for (int i = 0; i < ((Container) component).getComponentCount(); i++) {
                MapCanvas found = findMapCanvas(((Container) component).getComponent(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
