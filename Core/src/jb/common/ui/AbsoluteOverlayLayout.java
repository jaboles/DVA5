package jb.common.ui;

import javax.swing.*;
import java.awt.*;

public class AbsoluteOverlayLayout extends OverlayLayout
{
    public AbsoluteOverlayLayout(Container target) {
        super(target);
    }

    public void layoutContainer(Container target) {
        int nChildren = target.getComponentCount();
        for (int i = 0; i < nChildren; i++){
            Component c = target.getComponent(i);
            c.setBounds(c.getLocation().x, c.getLocation().y,
                    c.getPreferredSize().width, c.getPreferredSize().height);
        }
    }
}
