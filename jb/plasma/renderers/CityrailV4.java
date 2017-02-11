package jb.plasma.renderers;

import jb.common.ExceptionReporter;
import jb.plasma.CityrailLine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class CityrailV4 extends Cityrail
{
    protected static Color BackgroundColor = Color.white;
    protected static Color HeaderBackgroundColor = new Color(255, 128, 0);
    protected static Color TextColor = new Color(0, 0, 50);
    protected static Color HeaderTextColor = Color.white;
    protected static Color OrangeTextColor = new Color(255, 128, 0);

    protected static final double LeftMargin = 0.03;
    protected static final double RightMargin = 0.97;

    protected Font HeaderFont;
    protected Font HeaderTimeNowFont;
    protected Font DestinationFont;
    protected Font PlatformDepartsLabelFont;
    protected Font PlatformDepartsFont;
    protected Font PlatformDepartsFontSmall;
    protected Font TextBoxFont;

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

    public Dimension getAspectRatio()
    {
        return LANDSCAPE_1610;
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        fillRect(0, 0, 1, 1, BackgroundColor);
        fillRect(0, 0, 1, 0.1, HeaderBackgroundColor);
    }

    protected BufferedImage TryLoadLineLogo(CityrailLine line) {
        if (line.LogoImageFilename != null)
        {
            InputStream imageStream = CityrailV4Primary.class.getResourceAsStream("/jb/plasma/renderers/resources/" + line.LogoImageFilename);
            try {
                return ImageIO.read(imageStream);
            } catch (IOException e) {
                ExceptionReporter.reportException(e);
                return null;
            }
        }
        return null;
    }

    protected void drawMiniTextBox(double x, double y, String s)
    {
        double h = 0.06;
        double w = 0.20;
        fillRect(x, y, x + w, y + h, TextColor);
        drawString(s, x + 0.01, y + h - 0.016, Color.white, TextBoxFont);
    }
}
