package jb.common.ui;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;

public class SimpleEditorUndoRedoKit
{
    private static Map<JTextComponent, UndoManager> _undoManagers = new HashMap<>();
    
    public static void enableUndo(final JTextComponent c) {
        final UndoManager manager = new UndoManager();
        _undoManagers.put(c, manager);
        c.getDocument().addUndoableEditListener(e -> {
            manager.addEdit(e.getEdit());
            UndoAction.setEnabled(manager.canUndo());
            RedoAction.setEnabled(manager.canRedo());
        });
        c.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent arg0)
            {
                if (_undoManagers.containsKey(c)) {
                    UndoManager m = _undoManagers.get(c);
                    UndoAction.setEnabled(m.canUndo());
                    RedoAction.setEnabled(m.canRedo());
                }
            }
            public void focusLost(FocusEvent arg0)
            {
                UndoAction.setEnabled(false);
                RedoAction.setEnabled(false);
            }
        });
    }
    
    @SuppressWarnings("serial")
    public static final Action UndoAction = new TextAction("Undo") {
        public void actionPerformed(ActionEvent e) {
            JTextComponent c = getFocusedComponent();
            if (_undoManagers.containsKey(c))
            {
                UndoManager m = _undoManagers.get(c);
                m.undo();
                setEnabled(m.canUndo());
                RedoAction.setEnabled(m.canRedo());
            }
        }
    };

    @SuppressWarnings("serial")
    public static final Action RedoAction = new TextAction("Redo") {
        public void actionPerformed(ActionEvent e) {
            JTextComponent c = getFocusedComponent();
            if (_undoManagers.containsKey(c))
            {
                UndoManager m = _undoManagers.get(c);
                m.redo();
                UndoAction.setEnabled(m.canUndo());
                setEnabled(m.canRedo());
            }
        }
    };
}
