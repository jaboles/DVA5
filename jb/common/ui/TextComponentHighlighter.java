package jb.common.ui;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.JTextComponent;
import jb.common.ExceptionReporter;

public class TextComponentHighlighter
{
    public static void addHighlight(JTextComponent component, Highlighter.HighlightPainter painter)
    {
        addHighlight(component, painter, 0, component.getText().length());
    }

    public static void addHighlight(JTextComponent component, Highlighter.HighlightPainter painter, int start)
    {
        addHighlight(component, painter, start, component.getText().length());
    }

    public static void addHighlight(JTextComponent component, Highlighter.HighlightPainter painter, int start, int end)
    {
        removeHighlight(component, painter);

        try {
            Highlighter highlight = component.getHighlighter();
            highlight.addHighlight(start, end, painter);
        } catch (BadLocationException e) {
            ExceptionReporter.reportException(e);
        }
    }
    
    public static void removeHighlight(JTextComponent component, Highlighter.HighlightPainter painter)
    {
        Highlighter highlight = component.getHighlighter();
        for (Highlight h : highlight.getHighlights()) {
            if (h.getPainter() == painter) {
                highlight.removeHighlight(h);
            }
        }
    }
}
