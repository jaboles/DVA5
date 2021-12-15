package jb.dvacommon.ui;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import jb.dva.ui.DVAUI;

public class Resources
{
    private Resources() { }

    public static final Icon greenIcon = new ImageIcon(DVAUI.class.getResource("/indicatorIconGreen4.png"));
    public static final Icon redIcon = new ImageIcon(DVAUI.class.getResource("/indicatorIconRed4.png"));

    // An instance of the private subclass of the default highlight painter
    public static final Highlighter.HighlightPainter errorHighlightPainter = new ErrorHighlightPainter();

    // A private subclass of the default highlight painter
    public static class ErrorHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public ErrorHighlightPainter() {
            super(new Color(255, 0, 0, 60));
        }
    }
}