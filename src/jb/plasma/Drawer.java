package jb.plasma;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import jb.common.ExceptionReporter;
import jb.plasma.renderers.CityrailV4Primary;
import jb.plasma.ui.PlasmaPanel;

import static java.awt.RenderingHints.*;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

// Abstract renderer class
public abstract class Drawer implements Cloneable
{
    protected static final SVGUniverse SvgUniverse = new SVGUniverse();
    protected int width = 0;
    protected int height = 0;
    private Graphics g;
    protected double realFPSAdjustment;
    private long lastFrame = 0;

    protected List<DepartureData> DepartureData;

    // Aspect ratios
    protected static final Dimension LANDSCAPE_43 = new Dimension(4, 3);
    protected static final Dimension LANDSCAPE_1610 = new Dimension(16, 10);
    protected static final Dimension PORTRAIT_1610 = new Dimension(10, 16);

    public final static Map<Object, Object> RENDERING_HINTS = Map.of(
            KEY_ANTIALIASING,
            VALUE_ANTIALIAS_ON,
            KEY_ALPHA_INTERPOLATION,
            VALUE_ALPHA_INTERPOLATION_QUALITY,
            KEY_COLOR_RENDERING,
            VALUE_COLOR_RENDER_QUALITY,
            KEY_DITHERING,
            VALUE_DITHER_DISABLE,
            KEY_FRACTIONALMETRICS,
            VALUE_FRACTIONALMETRICS_ON,
            KEY_INTERPOLATION,
            VALUE_INTERPOLATION_BICUBIC,
            KEY_RENDERING,
            VALUE_RENDER_QUALITY,
            KEY_STROKE_CONTROL,
            VALUE_STROKE_PURE,
            KEY_TEXT_ANTIALIASING,
            VALUE_TEXT_ANTIALIAS_ON
    );

    protected Drawer()
    {
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
        List<DepartureData> filteredNewData = newData.stream()
                .filter(d -> d.Destination != null && !d.Destination.equals(""))
                .collect(Collectors.toList());
        newData.clear();
        newData.addAll(filteredNewData);
        DepartureData = newData;
    }

    // Set up some basic variables before the main rendering task carried out by each renderer.
    public void paint(Graphics g)
    {
        this.g = g;

        realFPSAdjustment = getRealFPSAdjustment();
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

    // Draw a string using X coordinate between (0,1) and absolute Y coordinate.
    public void drawStringC(Object obj, double x, int y, Color c, Font f)
    {
        g.setFont(f);
        g.setColor(c);
        String s = obj.toString();
        g.drawString(s, round(x * width) - (g.getFontMetrics(f).stringWidth(s) / 2), y);
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

    public void drawImage(Image image, double x, double y)
    {
        g.drawImage(image, round(x * width), round(y * height), null);
    }

    public void drawImage(Image image, double x, int y)
    {
        g.drawImage(image, round(x * width), y, null);
    }

    protected int round(double d)
    {
        return (int)Math.round(d);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private double getRealFPSAdjustment()
    {
        double adj;
        if (lastFrame == 0)
        {
            adj = 1.0;
        }
        else
        {
            double realFPS = 1000.0 / (System.currentTimeMillis() - lastFrame);
            adj = PlasmaPanel.FPS / realFPS;
        }
        return adj;
    }

    protected BufferedImage loadSvg(String resourcePath, Dimension d)
    {
        URL url = CityrailV4Primary.class.getResource(resourcePath);
        if (url == null) return null;

        BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHints(RENDERING_HINTS);
        SVGDiagram svg = SvgUniverse.getDiagram(SvgUniverse.loadSVG(url));
        svg.setIgnoringClipHeuristic(true);
        final AffineTransform transform = g.getTransform();
        transform.setToScale(d.width / svg.getWidth(), d.height / svg.getHeight());
        g.setTransform( transform );
        try {
            svg.render(g);
            return bi;
        } catch (SVGException e) {
            ExceptionReporter.reportException(e);
            return null;
        }
    }
}