package jb.plasma.renderers;

import java.awt.*;

public abstract class CityrailV4Landscape extends CityrailV4and5Landscape
{
    protected static final Color TextColor = new Color(0, 0, 50);

    protected void drawMiniTextBox(double x, double y, String s)
    {
        double h = 0.06;
        double w = 0.20;
        fillRect(x, y, x + w, y + h, TextColor);
        drawString(s, x + 0.01, y + h - 0.016, Color.white, TextBoxFont);
    }
}
