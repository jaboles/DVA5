package jb.plasma.renderers;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class CityrailV5Landscape extends CityrailV4and5Landscape
{
    protected static final Color TextColor = new Color(0, 0, 0);
    protected static final Color TextBoxColor = new Color(54, 57, 56);
    protected static final double AirportIconWidth = 0.035;
    protected BufferedImage airportIcon;

    public void dimensionsChanged()
    {
        super.dimensionsChanged();

        airportIcon = loadSvg("/jb/plasma/renderers/resources/airport.svg", new Dimension(round(AirportIconWidth * width), round(AirportIconWidth * width)));
    }

    protected double drawMiniTextBox(Graphics g, double x, double y, String s)
    {
        int widthPx = g.getFontMetrics(TextBoxFont).stringWidth(s);
        double width = (double)widthPx / this.width + 0.02;
        drawMiniTextBox(x, y, width, s);
        return width;
    }

    protected void drawMiniTextBox(double x, double y, double w, String s)
    {
        double h = 0.06;
        fillRect(x, y, x + w, y + h, TextBoxColor);
        drawString(s, x + 0.01, y + h - 0.016, Color.white, TextBoxFont);
    }

    protected void drawCarRangeTextBox(Graphics g, double x, int yAbs, String s)
    {
        double w = 0.1;
        double h = 0.06;
        int textYRelative = round(0.045 * height);
        int arcSize = round(0.012 * height);
        g.setColor(TextBoxColor);
        g.fillRoundRect(round(x * width), yAbs, round(w * width), round(h * height), arcSize, arcSize);
        drawStringC(s, x + (w / 2), yAbs + textYRelative, Color.white, TextBoxFont);
    }
}
