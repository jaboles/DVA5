package jb.dvacommon.ui;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import jb.common.ExceptionReporter;
import jb.common.ui.TextComponentHighlighter;
import jb.dva.Script;

public class DVATextTransferHandler extends TransferHandler
{
    private static final long serialVersionUID = 1L;

    private Script currentScript;
    
    public DVATextTransferHandler(Script currentScript)
    {
        this.currentScript = currentScript;
    }
    
    int importPos = 0;

    public boolean canImport(TransferSupport supp) {
        // Check for String flavor
        if (!supp.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        
        if (!supp.isDrop()) {
            return true;
        }

        // Return whether we accept the location
        if (supp.getComponent() instanceof JTextComponent)
        {
            JTextComponent component = (JTextComponent)supp.getComponent();
            int index = ((JTextComponent.DropLocation)supp.getDropLocation()).getIndex();
            ArrayList<Integer> insertPositions = new ArrayList<>();
            insertPositions.addAll(currentScript.getTranslatedDataOffsets());
            insertPositions.add(component.getText().length());

            // Find nearest possible insert position to the TransferSupport's index.
            int distance = Math.abs(insertPositions.get(0) - index);
            int idx = 0;
            for(int compareIndex = 1; compareIndex < insertPositions.size(); compareIndex++){
                int compareDistance = Math.abs(insertPositions.get(compareIndex) - index);
                if(compareDistance < distance){
                    idx = compareIndex;
                    distance = compareDistance;
                }
            }
            importPos = insertPositions.get(idx);

            // Highlight it.
            int highlightPos = importPos - 1;
            if (highlightPos < 0) highlightPos = 0;
            TextComponentHighlighter.addHighlight(component, insertHighlightPainter, highlightPos, highlightPos + 1);
            return true;
        }

        return false;
    }

    public boolean importData(TransferSupport supp) {
        if (!canImport(supp)) {
            return false;
        }

        // Fetch the Transferable and its data
        try
        {
            String data = (String)supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
            JTextArea component = (JTextArea)supp.getComponent();
            TextComponentHighlighter.removeHighlight(component, insertHighlightPainter);
            if (supp.isDrop())
            {

                // Fetch the drop location
                int index = importPos;
                String text = component.getText();
                int length = text.length();

                data = (index > 0 && !Character.isWhitespace(text.charAt(index - 1))? " " : "")
                        + data +
                        ((index < length && !Character.isWhitespace(text.charAt(index)) || index == length)? " " : "");

                // Insert the data at this location
                component.insert(data, index);
            }
            else
            {
                component.replaceSelection(data);
            }
        }
        catch (Exception e)
        {
            ExceptionReporter.reportException(e);
        }
        return super.importData(supp);
    }
    
    protected Transferable createTransferable(JComponent c) {
        JTextComponent source = (JTextComponent) c;
        int start = source.getSelectionStart();
        int end = source.getSelectionEnd();
        Document doc = source.getDocument();
        if (start == end) {
          return null;
        }
        try {
          p0 = doc.createPosition(start);
          p1 = doc.createPosition(end);
        } catch (BadLocationException e) {
          System.out
              .println("Can't create position - unable to remove text from source.");
        }
        String data = source.getSelectedText();
        return new StringSelection(data);
      }

    /**
     * These text fields handle both copy and move actions.
     */
    public int getSourceActions(JComponent c) {
      return COPY_OR_MOVE;
    }

    /**
     * When the export is complete, remove the old text if the action was a move.
     */
    protected void exportDone(JComponent c, Transferable data, int action) {
      if (action != MOVE) {
        return;
      }

      if ((p0 != null) && (p1 != null) && (p0.getOffset() != p1.getOffset())) {
        try {
          JTextComponent tc = (JTextComponent) c;
          tc.getDocument()
              .remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
        } catch (BadLocationException e) {
          System.out.println("Can't remove text from source.");
        }
      }
    }
    
    private Position p0, p1;
    
    Highlighter.HighlightPainter insertHighlightPainter = new InsertHighlightPainter();

    class InsertHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public InsertHighlightPainter() {
            super(new Color(0, 0, 255, 150));
        }
    }
}
