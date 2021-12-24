package jb.plasma.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import jb.common.ExceptionReporter;
import jb.common.ui.WindowUtils;
import jb.dva.DVAManager;
import jb.dva.SoundLibrary;
import jb.dvacommon.Settings;
import org.swixml.SwingEngine;

public class ScreenSaverSettingsDialog
{
    private JDialog dialog;
    private final PlasmaUI ui;

    public ScreenSaverSettingsDialog(DVAManager dvaManager, Map<String, SoundLibrary> availableSoundLibraries, File temp)
    {
        ui = new PlasmaUI(PlasmaUI.Mode.SCREENSAVER, dvaManager, availableSoundLibraries, temp);
        try {
            SwingEngine renderer = new SwingEngine(this);
            dialog = (JDialog) renderer.render(ScreenSaverSettingsDialog.class.getResource("/jb/plasma/ui/resources/screensaversettingsdialog.xml"));
            dialog.getContentPane().add(ui.getPanel(), BorderLayout.CENTER);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e)
                {
                    System.exit(0);
                }
            });
            dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
            dialog.getRootPane().getActionMap().put("Cancel", cancelAction);
            dialog.pack();
            WindowUtils.center(dialog);
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    public void setVisible(boolean b)
    {
        dialog.setVisible(b);
    }

    @SuppressWarnings("unused")
    public Action okAction = new AbstractAction("OK", null) {
        public void actionPerformed(ActionEvent e)
        {
            Settings.setIndicator("screenSaver", ui.getSettings());
            dialog.setVisible(false);
            System.exit(0);
        }
    };

    public final Action cancelAction = new AbstractAction("Cancel", null) {
        public void actionPerformed(ActionEvent e)
        {
            dialog.setVisible(false);
            System.exit(0);
        }
    };
}