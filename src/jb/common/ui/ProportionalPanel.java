package jb.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

// A panel that maintains a given aspect ratios
public class ProportionalPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    public ProportionalPanel(Dimension aspectRatio, JPanel panel) {
        float proportion = (float)aspectRatio.getWidth() / (float)aspectRatio.getHeight();
        setLayout(new AspectRatioLayout(proportion));
        setBackground(Color.black);

        this.add(panel);
    }
}