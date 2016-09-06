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
    protected static Color TextColor = Color.black;
    protected static Color HeaderTextColor = Color.white;
    protected static Color OrangeTextColor = new Color(255, 128, 0);

    protected Font HeaderFont;
    protected Font HeaderTimeNowFont;
    protected Font DestinationFont;

    public void dimensionsChanged()
    {
        HeaderFont = new Font("Arial", Font.PLAIN, (int)(height * 0.08));
        HeaderTimeNowFont = new Font("Arial", Font.PLAIN, (int)(height * 0.05));
        DestinationFont = new Font("Arial", Font.BOLD, (int)(height * 0.10));
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
}
