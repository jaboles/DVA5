package jb.plasma.renderers;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;

public abstract class CityrailV3 extends Cityrail
{
    protected static Color MiddleColor = Color.white;
    protected static Color BackgroundColor = new Color(0, 20, 90);
    protected static Color TextBlue = new Color(0, 40, 160);
    protected static Color TextWhite = Color.white;

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
        MainFont = new Font("Arial", Font.BOLD, (int)(height * 0.12));
        DestinationFont = MainFont;
        NextTrainFont = new Font("Arial", Font.BOLD, (int)(height * 0.065));
        MicroFont = new Font("Arial", Font.PLAIN, (int)(height * 0.04));
        MicroFontBold = new Font("Arial", Font.BOLD, (int)(height * 0.04));
        CarsFont = new Font("Arial", Font.BOLD, (int)(height * 0.1));
        Cars2Font = new Font("Arial", Font.BOLD, (int)(height * 0.049));
        HeaderFont = new Font("Arial", Font.PLAIN, (int)(height * 0.05));
        TypeFont = new Font("Arial", Font.BOLD, (int)(height * 0.056));
        TypeNextFont = new Font("Arial", Font.PLAIN, (int)(height * 0.078));
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