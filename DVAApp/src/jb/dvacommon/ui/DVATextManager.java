package jb.dvacommon.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import jb.common.ExceptionReporter;
import jb.common.ui.TextComponentHighlighter;
import jb.dva.DVAManager;
import jb.dva.Script;
import jb.dva.SoundReference;
import jb.dva.ui.SoundListModel;
import org.javatuples.Pair;

public class DVATextManager
{
    private static final int LOOKBACK_LIMIT = 6;
    private DVAManager dvaManager;
    private Script currentScript;
    public JLabel indicatorIconLabel;
    private JList<SoundReference> suggestedSoundList;
    private SoundListModel suggestedSoundListModel;
    public boolean documentModified = false;
    private DVATextVerifyListener listener;
    private JTextComponent component;

    public DVATextManager()
    {
    }

    public void initialize(JTextComponent component, final DVAManager dvaManager, final Script currentScript)
    {
        this.component = component;
        this.dvaManager = dvaManager;
        this.currentScript = currentScript;

        component.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {textAreaChanged(e);}
            public void insertUpdate(DocumentEvent e) {textAreaChanged(e);}
            public void removeUpdate(DocumentEvent e) {textAreaChanged(e);}
        });
    }

    public void initialize(JTextComponent component, final DVAManager dvaManager, final Script currentScript, JLabel indicatorIconLabel)
    {
        initialize(component, dvaManager, currentScript);
        this.indicatorIconLabel = indicatorIconLabel;
    }

    public void initialize(JTextComponent component, final DVAManager dvaManager, final Script currentScript, JLabel indicatorIconLabel, DVATextVerifyListener listener, final JList<SoundReference> suggestedSoundList, SoundListModel suggestedSoundListModel)
    {
        initialize(component, dvaManager, currentScript, indicatorIconLabel);
        this.listener = listener;
        this.suggestedSoundList = suggestedSoundList;
        this.suggestedSoundListModel = suggestedSoundListModel;

        component.setTransferHandler(new DVATextTransferHandler(currentScript));

        component.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                boolean shift = false;
                switch (e.getKeyCode())
                {
                case KeyEvent.VK_UP:
                    shift = true;
                case KeyEvent.VK_PAGE_UP:
                    if (!shift || e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
                        if (suggestedSoundList.getSelectedIndex() > 0)
                            suggestedSoundList.setSelectedIndex(suggestedSoundList.getSelectedIndex() - 1);
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    shift = true;
                case KeyEvent.VK_PAGE_DOWN:
                    if (!shift || e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
                        if (suggestedSoundList.getSelectedIndex() < suggestedSoundList.getModel().getSize() - 1)
                            suggestedSoundList.setSelectedIndex(suggestedSoundList.getSelectedIndex() + 1);
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_TAB:
                    autoComplete();
                    e.consume();
                    break;
                case KeyEvent.VK_RIGHT:
                    if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
                        autoComplete();
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    autoComplete();
                    break;
                }
            }
        });
    }

    public void autoComplete()
    {
        if (suggestedSoundList == null || suggestedSoundListModel == null) return;

        Pair<Integer, List<URL>> verifyResult = dvaManager.verify(currentScript);
        int errorPos = verifyResult.getValue0();
        int cursorPos = component.getCaretPosition();
        int startPos;

        if (errorPos >= 0 && cursorPos >= errorPos) {
            startPos = errorPos;
        } else {
            startPos = cursorPos;
        }

        if (suggestedSoundList.getSelectedIndex() >= 0) {
            String s = component.getText();
            String highlightedSound = suggestedSoundListModel.getElementAt(suggestedSoundList.getSelectedIndex()).toString();
            int overlap = overlapLength(s.substring(0, startPos), highlightedSound);
            if (overlap > 0)
            {
                startPos -= overlap;
            }
            String toInsert = (startPos > 0 && !Character.isWhitespace(s.charAt(startPos - 1))? " " : "")
                    + highlightedSound
                    + (cursorPos > 0 && !s.substring(cursorPos).startsWith(" ")? " " : "");
            try {
                component.getDocument().remove(startPos, cursorPos - startPos);
                component.getDocument().insertString(startPos, toInsert, null);
            } catch (BadLocationException ex) {
                ExceptionReporter.reportException(ex);
            }
        }
    }

    private int overlapLength(String s1, String s2)
    {
        int overlapLength = -1;
        for (int i = 1; i < s2.length(); i++)
        {
            if (s1.toLowerCase().endsWith(s2.substring(0, i).toLowerCase())) {
                overlapLength = i;
            }
        }

        return overlapLength;
    }

    public void textAreaChanged(DocumentEvent e)
    {
        currentScript.setScript(component.getText());
        int errorPos = verify();

        if (suggestedSoundList != null && suggestedSoundListModel != null)
        {
            updateFilter(errorPos, e);
        }
    }

    public int verify()
    {
        Pair<Integer, List<URL>> verifyResult = dvaManager.verify(currentScript);
        if (verifyResult.getValue0() >= 0)
        {
            if (indicatorIconLabel != null)
                indicatorIconLabel.setIcon(Resources.redIcon);
            TextComponentHighlighter.addHighlight(component, Resources.errorHighlightPainter, verifyResult.getValue0());
            if (listener != null)
                listener.OnFailed();
        }
        else
        {
            if (indicatorIconLabel != null)
                indicatorIconLabel.setIcon(Resources.greenIcon);
            TextComponentHighlighter.removeHighlight(component, Resources.errorHighlightPainter);
            if (listener != null)
                listener.OnVerified(verifyResult.getValue1());
        }
        return verifyResult.getValue0();
    }

    public void canonicalise()
    {
        String cs = dvaManager.getCanonicalScript(currentScript);
        if (cs != null) {
            int caretPos = component.getCaretPosition();
            component.setText(cs);
            component.setCaretPosition(caretPos);
            documentModified = false;
        }
    }

    public void updateFilter(int errorPos, DocumentEvent e)
    {
        String text = component.getText();
        ArrayList<Integer> translatedDataOffsets = currentScript.getTranslatedDataOffsets();

        int changePosition;
        if (e == null) {
            changePosition = component.getCaretPosition();
        } else {
            changePosition = e.getOffset();
            if (e.getType().equals(DocumentEvent.EventType.INSERT) || e.getType().equals(DocumentEvent.EventType.CHANGE)) {
                changePosition += e.getLength();
            }
        }

        if (text.length() == 0) {
            suggestedSoundListModel.clearFilter();
        } else if (errorPos >= 0) {

            // Filter the list to suggest possibilities for what has been typed.
            String filter = text.substring(errorPos, changePosition).toLowerCase();
            suggestedSoundListModel.clearFilter();

            suggestedSoundListModel.addFilter(filter);
            for (int i = translatedDataOffsets.size() - 1; i >= Math.max(0, translatedDataOffsets.size()-LOOKBACK_LIMIT); i--) {
                int offset = translatedDataOffsets.get(i);
                suggestedSoundListModel.addFilter(text.substring(offset, changePosition).toLowerCase());
            }
        } else {
            boolean wholeDocumentReplaced = e != null && e.getOffset() == 0 && e.getLength() == text.length();
            if ((changePosition > 0 && text.charAt(changePosition - 1) == ' ') || wholeDocumentReplaced) {
                suggestedSoundListModel.clearFilter();
            } else {
                suggestedSoundListModel.clearFilter();
                for (int i = translatedDataOffsets.size() - 1; i >= Math.max(0, translatedDataOffsets.size()-LOOKBACK_LIMIT); i--) {
                    int offset = translatedDataOffsets.get(i);
                    if (changePosition > offset)
                        suggestedSoundListModel.addFilter(text.substring(offset, changePosition).toLowerCase());
                }
            }
        }
        if (suggestedSoundList.getSelectedIndex() >= suggestedSoundListModel.getSize())
        {
            suggestedSoundList.setSelectedIndex(0);
        }
    }


}
