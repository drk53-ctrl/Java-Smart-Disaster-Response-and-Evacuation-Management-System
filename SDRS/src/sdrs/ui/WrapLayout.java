package sdrs.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class WrapLayout extends FlowLayout {

    private static final long serialVersionUID = 1L;

    public WrapLayout() {
        super();
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= getHgap() + getVgap();
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            Container container = target;
            while (container.getSize().width == 0 && container.getParent() != null) {
                container = container.getParent();
            }
            targetWidth = container.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            Insets insets = target.getInsets();
            int horizontalGap = getHgap();
            int verticalGap = getVgap();
            int maxWidth = targetWidth - (insets.left + insets.right + horizontalGap * 2);
            int maxWidthNoGap = maxWidth - horizontalGap;
            Dimension result = new Dimension(0, 0);
            Dimension lineSize = new Dimension(0, 0);
            int members = target.getComponentCount();
            for (int i = 0; i < members; i++) {
                Component member = target.getComponent(i);
                if (!member.isVisible()) {
                    continue;
                }
                Dimension size = preferred ? member.getPreferredSize() : member.getMinimumSize();
                if (size.width > maxWidth) {
                    size = new Dimension(maxWidth, size.height);
                }
                if (i > 0 && lineSize.width + horizontalGap + size.width > maxWidth) {
                    result.width = Math.max(result.width, lineSize.width);
                    result.height += verticalGap + lineSize.height;
                    lineSize = new Dimension(0, 0);
                }
                if (lineSize.width > 0) {
                    lineSize.width += horizontalGap;
                }
                lineSize.width += size.width;
                lineSize.height = Math.max(lineSize.height, size.height);
            }
            result.width = Math.max(result.width, lineSize.width);
            result.height += verticalGap + lineSize.height;
            if (result.width > maxWidthNoGap && members > 1) {
                result.width = maxWidthNoGap;
            }
            result.width += insets.left + insets.right + horizontalGap * 2;
            result.height += insets.top + insets.bottom + verticalGap * 2;
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null) {
                result.width = Math.max(result.width, scrollPane.getViewport().getWidth() - 8);
            }
            return result;
        }
    }
}
