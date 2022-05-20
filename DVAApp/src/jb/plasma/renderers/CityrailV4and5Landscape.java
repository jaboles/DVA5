package jb.plasma.renderers;

import jb.dvacommon.ui.Resources;

import java.awt.*;

public abstract class CityrailV4and5Landscape extends CityrailV4and5
{
    public CityrailV4and5Landscape(Color headerBackgroundColor) {
        super(headerBackgroundColor);
    }

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

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
        fillRect(0, 0, 1, 0.1, HeaderBackgroundColor);
    }
}