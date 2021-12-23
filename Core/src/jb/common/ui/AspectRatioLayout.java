package jb.common.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

// A layout manager that supports a locked aspect ratio for its only component.
public class AspectRatioLayout implements LayoutManager {

    private final float ratio;
    private int compCount = 0;

    public AspectRatioLayout(float ratio) {
        this.ratio = ratio;
    }

    public void addLayoutComponent(String name, Component comp) {
        compCount++;
        assert(compCount == 1) : "AspectRatioLayout can only contain one component";
    }

    public void removeLayoutComponent(Component comp) {
        compCount--;
    }

    public void layoutContainer(Container parent) {
        if(parent.getComponentCount() == 1) {
            Insets insets = parent.getInsets();
            int maxWidth = parent.getWidth()
                    - (insets.left + insets.right);
            int maxHeight = parent.getHeight()
                    - (insets.top + insets.bottom);

            float w = Math.min(maxHeight * ratio, maxWidth);
            float h = w / ratio;

            Component c = parent.getComponent(0);

            Rectangle bounds = new Rectangle();
            bounds.width = (int) w;
            bounds.height = (int) h;
            bounds.x = (maxWidth - bounds.width)/2;
            bounds.y = (maxHeight - bounds.height)/2;

            c.setBounds(bounds);
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        Component c = parent.getComponent(0);

        Insets i = parent.getInsets();

        Dimension min = new Dimension(c.getMinimumSize());
        min.width += i.left + i.right;
        min.height += i.top + i.bottom;

        return min;
    }

    public Dimension preferredLayoutSize(Container parent) {
        Component c = parent.getComponent(0);

        Insets i = parent.getInsets();

        Dimension min = new Dimension(c.getPreferredSize());
        min.width += i.left + i.right;
        min.height += i.top + i.bottom;

        return min;
    }
}