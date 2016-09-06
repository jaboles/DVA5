package jb.dvacommon.ui;

import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jb.common.ui.TextComponentHighlighter;

public class FileTextField extends JTextField
{
    private static final long serialVersionUID = 1L;
    public JLabel indicatorIconLabel;
    
    public FileTextField()
    {
        getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {textAreaChanged();}
            public void insertUpdate(DocumentEvent e) {textAreaChanged();}
            public void removeUpdate(DocumentEvent e) {textAreaChanged();}
        });
    }
    
    public void initialize(JLabel indicatorIconLabel)
    {
        this.indicatorIconLabel = indicatorIconLabel;
    }
    
    public void textAreaChanged()
    {
        String path = this.getText();
        if (path.isEmpty() || new File(path).exists())
        {
            TextComponentHighlighter.removeHighlight(this, Resources.errorHighlightPainter);
            if (indicatorIconLabel != null)
                indicatorIconLabel.setIcon(Resources.greenIcon);
        }
        else
        {
            TextComponentHighlighter.addHighlight(this, Resources.errorHighlightPainter);
            if (indicatorIconLabel != null)
                indicatorIconLabel.setIcon(Resources.redIcon);
        }
    }
}
