package jb.dvacommon.ui;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import jb.dva.ui.DVAUI;
import jb.plasma.Drawer;

public class Resources
{
    private Resources() { }

    static
    {
        URL url;
        url = DVAUI.class.getResource("/indicatorIconGreen4.png");
        if (url != null) greenIcon = new ImageIcon(url);
        url = DVAUI.class.getResource("/indicatorIconRed4.png");
        if (url != null) redIcon = new ImageIcon(url);

        try {
            try (InputStream is = Drawer.class.getResourceAsStream("/arial.ttf")) {
                ArialRegular = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            try (InputStream is = Drawer.class.getResourceAsStream("/arialbd.ttf")) {
                ArialBold = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            try (InputStream is = Drawer.class.getResourceAsStream("/Roboto-Medium.ttf")) {
                RobotoMedium = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (IOException | FontFormatException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static Icon greenIcon;
    public static Icon redIcon;

    // Fonts
    public static Font ArialRegular;
    public static Font ArialBold;
    public static Font RobotoMedium;

    // An instance of the private subclass of the default highlight painter
    public static final Highlighter.HighlightPainter errorHighlightPainter = new ErrorHighlightPainter();

    // A private subclass of the default highlight painter
    public static class ErrorHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public ErrorHighlightPainter() {
            super(new Color(255, 0, 0, 60));
        }
    }
}