package verify;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import sdrs.Main;

/**
 * Live visual capture: boots the real application, walks it through the exact
 * steps from the user's checklist (open Emergency Teams, narrow window, drag
 * divider left/right, grow window) and saves a PNG after each step.
 */
public class LiveScreenshot {

    public static void main(String[] args) throws Exception {
        final String dir = args.length > 0 ? args[0] : "build-verify/shots";
        new File(dir).mkdirs();
        Main.main(new String[0]);
        Thread.sleep(6000); // frame shown, first clock tick, layout settled

        final Window[] frameHolder = new Window[1];
        final JSplitPane[] shellHolder = new JSplitPane[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                for (Window window : Window.getWindows()) {
                    if (window instanceof sdrs.ui.MainFrame && window.isVisible()) {
                        frameHolder[0] = window;
                        shellHolder[0] = findSplit((java.awt.Container) window);
                    }
                }
            }
        });
        final Window frame = frameHolder[0];
        final JSplitPane shell = shellHolder[0];
        if (frame == null || shell == null) {
            System.out.println("[FAIL] MainFrame or split pane not found");
            System.exit(1);
        }
        final Robot robot = new Robot();

        shot(robot, frame, dir + "/01-dashboard-default.png");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ((sdrs.ui.MainFrame) frame).showPanel("Emergency Teams");
            }
        });
        Thread.sleep(1000);
        shot(robot, frame, dir + "/02-teams-default.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.setSize(1024, 660);
            }
        });
        Thread.sleep(1000);
        shot(robot, frame, dir + "/03-teams-narrow-1024.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                shell.setDividerLocation(340); // drag right: wider sidebar
            }
        });
        Thread.sleep(900);
        shot(robot, frame, dir + "/04-teams-sidebar-340.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                shell.setDividerLocation(210); // drag left: narrowest allowed
            }
        });
        Thread.sleep(900);
        shot(robot, frame, dir + "/05-teams-sidebar-210.png");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.setSize(1600, 900);
                frame.setLocationRelativeTo(null);
            }
        });
        Thread.sleep(1000);
        shot(robot, frame, dir + "/06-teams-wide-1600.png");

        System.out.println("[DONE] screenshots written to " + dir);
        System.exit(0);
    }

    private static void shot(Robot robot, Window frame, String path) throws Exception {
        frame.toFront();
        Thread.sleep(500);
        Rectangle screen = new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle target = frame.getBounds().intersection(screen);
        java.awt.image.BufferedImage image = robot.createScreenCapture(target);
        ImageIO.write(image, "png", new File(path));
        System.out.println("[SHOT] " + path + " " + image.getWidth() + "x" + image.getHeight());
    }

    private static JSplitPane findSplit(java.awt.Container root) {
        if (root instanceof JSplitPane) {
            return (JSplitPane) root;
        }
        for (int i = 0; i < root.getComponentCount(); i++) {
            java.awt.Component child = root.getComponent(i);
            if (child instanceof JSplitPane) {
                return (JSplitPane) child;
            }
            if (child instanceof java.awt.Container) {
                JSplitPane found = findSplit((java.awt.Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
