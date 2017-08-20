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
    
    public static Icon greenIcon = new ImageIcon(DVAUI.class.getResource("/resources/indicatorIconGreen4.png"));
    public static Icon redIcon = new ImageIcon(DVAUI.class.getResource("/resources/indicatorIconRed4.png"));

    // An instance of the private subclass of the default highlight painter
    public static Highlighter.HighlightPainter errorHighlightPainter = new Resources().new ErrorHighlightPainter();
    
    // A private subclass of the default highlight painter
    public class ErrorHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public ErrorHighlightPainter() {
            super(new Color(255, 0, 0, 60));
        }
    }
}
