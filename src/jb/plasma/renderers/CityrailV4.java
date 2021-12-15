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