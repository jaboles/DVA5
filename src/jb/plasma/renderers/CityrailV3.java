package jb.plasma.renderers;
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
        MainFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.12));
        DestinationFont = MainFont;
        NextTrainFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.065));
        MicroFont = ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.04));
        MicroFontBold = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.04));
        CarsFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.1));
        Cars2Font = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.049));
        HeaderFont = ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TypeFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.056));
        TypeNextFont = ArialRegular.deriveFont(Font.PLAIN, (int)(height * 0.078));
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        //if (paintInfrequent)
        {
            fillRect(0, 0, 1, 1, BackgroundColor);
        }
    }
}