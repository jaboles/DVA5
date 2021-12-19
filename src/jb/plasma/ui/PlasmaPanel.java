package jb.plasma.ui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.Buffer;
import java.time.LocalDateTime;
import javax.swing.JPanel;
import javax.swing.Timer;
import jb.plasma.Drawer;

public class PlasmaPanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    private final Drawer drawer; // The current renderer for this panel
    public final static int FPS = 30;  // Default frames per second
    /*
    private long lastRenderTimeMillis;
    private final static boolean mouseDebug = false;
    private Point mousePosition = new Point(0, 0);
    */
    private final static RenderingHints renderingHints = new RenderingHints(null, null);
    private int lastHeight = 0;
    private int lastWidth = 0;
    private int lastSecond = 0;
    private static final boolean BufferInfrequentDraws = true;
    private Image buf = null;
    private Graphics bg = null;

    public PlasmaPanel(Drawer drawer) {
        this.drawer = drawer;
        setDoubleBuffered(true);
        ActionListener repaintAction = evt -> PlasmaPanel.this.repaint();
        new Timer(1000 / FPS, repaintAction).start();
        /*
        if (mouseDebug)
        {
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    mousePosition = e.getPoint();
                }
            });
        }
        */

        renderingHints.clear();
        renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    // Main paint routine. Notifies the active renderer of the panel dimensions, then calls the renderer's
    // paint routine. Also draws the FPS if set to do so.
    public void paint(Graphics g)
    {
        int width = getWidth();
        int height = getHeight();
        int second = LocalDateTime.now().getSecond();
        boolean paintInfrequent = false;

        if (height != lastHeight || width != lastWidth) {
            lastHeight = height;
            lastWidth = width;
            drawer.setDimensions(width, height);
            drawer.dimensionsChanged();
            buf = createImage(width, height);
            bg = buf.getGraphics();
            ((Graphics2D)bg).setRenderingHints(renderingHints);
            if (BufferInfrequentDraws)
                drawer.paintInfrequent(bg);
        } else if (second != lastSecond && BufferInfrequentDraws) {
            lastSecond = second;
             drawer.paintInfrequent(bg);
        }

        ((Graphics2D)g).setRenderingHints(renderingHints);
        if (BufferInfrequentDraws)
            g.drawImage(buf, 0, 0, null);
        else
            drawer.paintInfrequent(g);
        drawer.paint(g);

        /*
        if (lastRenderTimeMillis != 0)
        {
            long currentTimeMillis = System.currentTimeMillis();
            g.setColor(Color.orange);
            g.setFont(FpsFont);
            g.drawString("FPS: " + Integer.toString((int)(1000.0 / (currentTimeMillis - lastRenderTimeMillis))), 0, height);
            lastRenderTimeMillis = currentTimeMillis;
        }

        if (mousePosition != null)
        {
            g.setColor(Color.orange);
            g.setFont(FpsFont);
            g.drawString("X: " + ((double)mousePosition.x / (double)width), 0, height - 30);
            g.drawString("Y: " + ((double)mousePosition.y / (double)height), 0, height - 20);
        }
        */
    }
}