package jb.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

// A panel that maintains a given aspect ratios
public class ProportionalPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    public ProportionalPanel(Dimension aspectRatio, JPanel panel, Color background) {
        float proportion = (float)aspectRatio.getWidth() / (float)aspectRatio.getHeight();
        setLayout(new AspectRatioLayout(proportion));
        if (background != null) {
            setBackground(background);
        } else {
            setOpaque(false);
        }

        this.add(panel);
    }
}