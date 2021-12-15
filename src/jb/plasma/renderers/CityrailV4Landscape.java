package jb.plasma.renderers;

import jb.dvacommon.ui.Resources;

import java.awt.*;

public abstract class CityrailV4Landscape extends CityrailV4
{
    public void dimensionsChanged()
    {
        HeaderFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.075));
        HeaderTimeNowFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TimeFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.07));
        DestinationFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.145));
        Destination2Font = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        PlatformDepartsLabelFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.04));
        PlatformDepartsFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.115));
        PlatformDepartsFontSmall = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.1));
        MainFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.085));
        TextBoxFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.04));
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        fillRect(0, 0, 1, 0.1, HeaderBackgroundColor);
    }

    protected void drawMiniTextBox(double x, double y, String s)
    {
        double h = 0.06;
        double w = 0.20;
        fillRect(x, y, x + w, y + h, TextColor);
        drawString(s, x + 0.01, y + h - 0.016, Color.white, TextBoxFont);
    }
}