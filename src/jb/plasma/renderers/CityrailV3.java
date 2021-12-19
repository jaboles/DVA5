package jb.plasma.renderers;
import jb.dvacommon.ui.Resources;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;

public abstract class CityrailV3 extends Cityrail
{
    protected static final Color MiddleColor = Color.white;
    protected static final Color BackgroundColor = new Color(0, 20, 90);
    protected static final Color TextBlue = new Color(0, 40, 160);
    protected static final Color TextWhite = Color.white;

    protected Font DestinationFont;
    protected Font MainFont;
    protected Font NextTrainFont;
    protected Font MicroFont;
    protected Font MicroFontBold;
    protected Font CarsFont;
    protected Font Cars2Font;
    protected Font HeaderFont;
    protected Font TypeFont;
    protected Font TypeNextFont;

    protected Polygon nextTrainBackground;

    public void dimensionsChanged()
    {
        MainFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.12));
        DestinationFont = MainFont;
        NextTrainFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.065));
        MicroFont = Resources.ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.04));
        MicroFontBold = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.04));
        CarsFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.1));
        Cars2Font = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.049));
        HeaderFont = Resources.ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TypeFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.056));
        TypeNextFont = Resources.ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.078));
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
        fillRect(0, 0, 1, 1, BackgroundColor);
    }
}