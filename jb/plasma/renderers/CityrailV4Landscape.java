package jb.plasma.renderers;

import jb.common.ExceptionReporter;
import jb.plasma.CityrailLine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class CityrailV4Landscape extends CityrailV4
{
    public void dimensionsChanged()
    {
        HeaderFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.075));
        HeaderTimeNowFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TimeFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.07));
        DestinationFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.145));
        Destination2Font = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        PlatformDepartsLabelFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.04));
        PlatformDepartsFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.115));
        PlatformDepartsFontSmall = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.1));
        MainFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.085));
        TextBoxFont = TPFrankMedium.deriveFont(Font.PLAIN, (int)(height * 0.04));
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
