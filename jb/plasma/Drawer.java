package jb.plasma;
import java.awt.*;
import java.util.List;

import com.innahema.collections.query.queriables.Queryable;
import jb.common.ExceptionReporter;
import jb.plasma.data.DepartureData;
import jb.plasma.ui.PlasmaPanel;

// Abstract renderer class
public abstract class Drawer implements Cloneable
{
    protected int width = 0;
    protected int height = 0;
    protected boolean paintInfrequent = false;
    private Graphics g;
    protected double realFPSAdjustment;
    private long lastFrame = 0;
    
    protected List<jb.plasma.data.DepartureData> DepartureData;

    // Aspect ratios
    protected static Dimension LANDSCAPE_43 = new Dimension(4, 3);
    protected static Dimension LANDSCAPE_1610 = new Dimension(16, 10);
    protected static Dimension PORTRAIT_1610 = new Dimension(10, 16);

    // Fonts
    protected static Font ArialRegular;
    protected static Font ArialBold;
    protected static Font TPFrankRegular;
    protected static Font TPFrankMedium;
    protected static Font TPFrankBold;

    protected Drawer()
    {
    }

    public static void initializeFonts()
    {
        try
        {
            ArialRegular = Font.createFont(Font.TRUETYPE_FONT, Drawer.class.getResourceAsStream("/arial.ttf"));
            ArialBold = Font.createFont(Font.TRUETYPE_FONT, Drawer.class.getResourceAsStream("/arialbd.ttf"));
            TPFrankRegular = Font.createFont(Font.TRUETYPE_FONT, Drawer.class.getResourceAsStream("/tpfrank-regular-webfont.ttf"));
            TPFrankMedium = Font.createFont(Font.TRUETYPE_FONT, Drawer.class.getResourceAsStream("/tpfrank-medium-webfont.ttf"));
            TPFrankBold = Font.createFont(Font.TRUETYPE_FONT, Drawer.class.getResourceAsStream("/tpfrank-bold-webfont.ttf"));
        }
        catch (Exception ex)
        {
            ExceptionReporter.reportException(ex);
        }
    }

    // Called before each render cycle to notify the renderer of the dimensions it is drawing in
    public void setDimensions(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public abstract void dimensionsChanged();

    // Renderers should return the aspect ratio they will render in
    public abstract Dimension getAspectRatio();

    // Renderers should return their name
    public abstract String toString();

    // Renderers can react to the list of departures changing (e.g. the first one being removed from the front
    // of the list when its due-out time has passed)
    public void dataChanged(List<DepartureData> newData)
    {
        List<DepartureData> filteredNewData = Queryable.from(newData)
                .where(d -> d.Destination != null && !d.Destination.equals(""))
                .toList();
        newData.clear();
        newData.addAll(filteredNewData);
        DepartureData = newData;
    }

    // Set up some basic variables before the main rendering task carried out by each renderer.
    public void paint(Graphics g)
    {
        this.g = g;
        
        realFPSAdjustment = getRealFPSAdjustment(PlasmaPanel.FPS);
        lastFrame = System.currentTimeMillis();

        //paintInfrequent = paintInfrequent();
    }

    // Fill a rectangle using coordinates between (0,0) and (1,1)
    public void fillRect(double x1, double y1, double x2, double y2, Color c)
    {
        int x = round(x1 * width);
        int y = round(y1 * height);
        int w = round(x2 * width) - x;
        int h = round(y2 * height) - y;
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    // Draw a rectangle using coordinates between (0,0) and (1,1)
    public void drawRect(double x1, double y1, double x2, double y2, Color c)
    {
        int x = round(x1 * width);
        int y = round(y1 * height);
        int w = round(x2 * width) - x;
        int h = round(y2 * height) - y;
        g.setColor(c);
        g.drawRect(x, y, w, h);
    }

    // Draw a line using coordinates between (0,0) and (1,1)
    public void drawLine(double x1, double y1, double x2, double y2, Color c)
    {
        int x1i = round(x1 * width);
        int y1i = round(y1 * height);
        int x2i = round(x2 * width);
        int y2i = round(y2 * height);
        g.setColor(c);
        g.drawLine(x1i, y1i, x2i, y2i);
    }

    // Draw a string using coordinates between (0,0) and (1,1)
    public void drawString(Object obj, double x, double y, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        g.drawString(obj.toString(), round(x * width), round(y * height));
    }

    // Draw a string using X coordinate between (0,1) and absolute Y coordinate.
    public void drawString(Object obj, double x, int y, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        g.drawString(obj.toString(), round(x * width), y);
    }

    // Draw a string (right aligned) using coordinates between (0,0) and (1,1)
    public void drawStringR(Object obj, double x, double y, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        String s = obj.toString();
        g.drawString(s, round(x * width) - g.getFontMetrics(f).stringWidth(s), round(y * height));
    }

    // Draw a string (centered) using coordinates between (0,0) and (1,1)
    public void drawStringC(Object obj, double x, double y, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        String s = obj.toString();
        g.drawString(s, round(x * width) - (g.getFontMetrics(f).stringWidth(s) / 2), round(y * height));
    }

    // Draw a list of strings using coordinates between (0,0) and (1,1)
    public void drawString(String[] str, double x, double y, double sep, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        for (int i = 0; i < str.length; i++)
        {
            g.drawString(str[i], round(x * width), round((y + (i * sep)) * height));
        }
    }

    // Draw a list of strings (centered) using coordinates between (0,0) and (1,1)
    public void drawStringC(String[] str, double x, double y, double sep, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        for (int i = 0; i < str.length; i++)
        {
            drawStringC(str[i], x, y + (i * sep), c, f);
        }
    }
    
    public void drawImage(Image image, double x, double y, double w, double h)
    {
        g.drawImage(image, round(x * width), round(y * height), round(w * width), round(h * height), null);
    }

    public void drawImageSquare(Image image, double x, double y, double w)
    {
        int dimension = round(w * height);
        g.drawImage(image, round(x * width), round(y * height), dimension, dimension, null);
    }

    protected int round(double d)
    {
        return (int)Math.round(d);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private double getRealFPSAdjustment(int nominalFPS)
    {
        double adj;
        if (lastFrame == 0)
        {
            adj = 1.0;
        }
        else
        {
            double realFPS = 1000.0 / (System.currentTimeMillis() - lastFrame);
            adj = nominalFPS / realFPS;
        }
        return adj;
    }
    
    /*private boolean paintInfrequent()
    {
        boolean isNewSecond = (System.currentTimeMillis() % 1000) < (lastFrame % 1000);
        return (isNewSecond || lastFrame == 0);
    }*/
}