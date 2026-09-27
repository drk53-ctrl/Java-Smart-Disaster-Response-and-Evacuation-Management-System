package sdrs;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import sdrs.ui.MainFrame;
import sdrs.ui.Theme;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
        }
        Theme.install();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
