package jb.common.ui;

import java.awt.*;

public class UIUtilities {
    public static void setContainerEnabled(Container container, boolean enabled) {
        container.setEnabled(enabled);
        Component[] components = container.getComponents();
        for (Component comp : components) {
            comp.setEnabled(enabled);
            if (comp instanceof Container) {
                setContainerEnabled((Container) comp, enabled);
            }
        }
    }
}
