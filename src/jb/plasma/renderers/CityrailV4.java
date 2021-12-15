package jb.plasma.renderers;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import jb.common.ExceptionReporter;
import jb.plasma.CityrailLine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static java.awt.RenderingHints.*;

public abstract class CityrailV4 extends Cityrail
{
    protected static Color BackgroundColor = Color.white;
    protected static Color HeaderBackgroundColor = new Color(255, 128, 0);
    protected static Color TextColor = new Color(0, 0, 50);
    protected static Color HeaderTextColor = Color.white;
    protected static Color OrangeTextColor = new Color(255, 128, 0);
    protected static SVGUniverse SvgUniverse = new SVGUniverse();
    protected static final double LeftMargin = 0.03;
    protected static final double RightMargin = 0.97;
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

    protected Font HeaderFont;
    protected Font HeaderTimeNowFont;
    protected Font DestinationFont;
    protected Font PlatformDepartsLabelFont;
    protected Font PlatformDepartsFont;
    protected Font PlatformDepartsFontSmall;
    protected Font TextBoxFont;

    public void paint(Graphics g)
    {
        super.paint(g);
        fillRect(0, 0, 1, 1, BackgroundColor);
    }

    protected BufferedImage TryReloadLineLogo(CityrailLine line, Dimension d) {
        if (d.height == 0 || d.width == 0) return null;

        if (line != null && line.LogoImageFilename != null)
        {
            String filename = line.LogoImageFilename;
            URL url = CityrailV4Primary.class.getResource("/jb/plasma/renderers/resources/" + line.LogoImageFilename);
            BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHints(RENDERING_HINTS);
            try {
                if (filename.toLowerCase().endsWith(".svg")) {
                    SVGDiagram svg = SvgUniverse.getDiagram(SvgUniverse.loadSVG(url));
                    svg.setIgnoringClipHeuristic(true);
                    final AffineTransform transform = g.getTransform();
                    transform.setToScale(d.width / svg.getWidth(), d.height / svg.getHeight());
                    g.setTransform( transform );
                    svg.render(g);
                } else {
                    g.drawImage(ImageIO.read(url), 0, 0, d.width, d.height, null);
                }
                return bi;
            } catch (IOException | SVGException e) {
                ExceptionReporter.reportException(e);
                return null;
            } finally {
                g.dispose();
            }
        }
        return null;
    }
}