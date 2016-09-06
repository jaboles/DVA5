package jb.dvacommon.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.ui.ProgressWindow;
import jb.common.ui.SimpleEditorUndoRedoKit;
import jb.common.ui.WindowUtils;
import jb.dva.ui.DVAUI;
import jb.dvacommon.BaseUpdater;
import jb.dvacommon.DVA;
import jb.dvacommon.ProgressAdapter;
import jb.dvacommon.Settings;
import jb.dvacommon.Updater;
import jb.plasma.ui.PlasmaUI;
import org.swixml.SwingEngine;

public class DVAShell
{
    private SwingEngine renderer;
    private JFrame window;
    private DVA controller;
    public PlasmaUI plasmaUI;
    public JPanel dvaPanel;
    public JPanel indicatorsPanel;

    public JTabbedPane tabbedPane;
    public JLabel updateInfoLabel;
    public JTextPane updateVersionHistoryPane;
    
    public Action voiceLibraryToggleAction;
    public Action soundInfoAction;
    public Action playCurrentAction;
    public Action playSavedAction;
    public Action stopAction;
    public Action newAction;
    public Action openAction;
    public Action moveUpAction;
    public Action moveDownAction;
    public Action renameAction;
    public Action deleteAction;
    public Action exportAction;
    public Action saveAction;
    public Action undoAction;
    public Action redoAction;
    
    public Action indicatorWindowAction;
    public Action indicatorFullScreenAction;
    public Action indicatorPlayAnnouncementAction;
    public Action indicatorEditSubstitutionsAction;
    public Action indicatorEditViasAction;
    public Action indicatorEditAllStationsTosAction;

    public DVAShell(final DVA controller) {
        this.controller = controller;
        renderer = new SwingEngine(this);

        try {
            DVAUI dvaUI = new DVAUI(controller);
            voiceLibraryToggleAction = dvaUI.voiceLibraryToggleAction;
            soundInfoAction = dvaUI.soundInfoAction;
            playCurrentAction = dvaUI.playCurrentAction;
            playSavedAction = dvaUI.playSavedAction;
            stopAction = dvaUI.stopAction;
            newAction = dvaUI.newAction;
            openAction = dvaUI.openAction;
            moveUpAction = dvaUI.moveUpAction;
            moveDownAction = dvaUI.moveDownAction;
            renameAction = dvaUI.renameAction;
            deleteAction = dvaUI.deleteAction;
            exportAction = dvaUI.exportAction;
            saveAction = dvaUI.saveAction;
            undoAction = SimpleEditorUndoRedoKit.UndoAction;
            redoAction = SimpleEditorUndoRedoKit.RedoAction;
            
            plasmaUI = new PlasmaUI(PlasmaUI.Mode.REGULAR, controller);
            indicatorWindowAction = plasmaUI.windowAction;
            indicatorFullScreenAction = plasmaUI.fullScreenAction;
            indicatorPlayAnnouncementAction = plasmaUI.announceAction;
            indicatorEditSubstitutionsAction = plasmaUI.editSubstitutionsAction;
            indicatorEditViasAction = plasmaUI.editViasAction;
            indicatorEditAllStationsTosAction = plasmaUI.editAllStationsTosAction;

            window = (JFrame)renderer.render(DVAShell.class.getResource("/jb/dvacommon/ui/resources/dvashell.xml"));
            if (SwingEngine.isMacOSX())
            {
                Dimension size = window.getSize();
                window.setSize((int)size.getWidth(), (int)size.getHeight() + 20);
                window.getRootPane().putClientProperty("apple.awt.brushMetalLook", true);
            }
            dvaPanel.add(dvaUI.getPanel(), BorderLayout.CENTER);
            indicatorsPanel.add(plasmaUI.getPanel(), BorderLayout.CENTER);
            
            WindowUtils.center(window);
        }
        catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }
    
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            // If about to show the main UI, do version check.
            Thread t = new Thread()
            {
                public void run()
                {
                    final BaseUpdater updater = Updater.updateAvailable(DVA.VersionString, Settings.getUpdateSuppressed());
                    if (updater != null)
                        SwingUtilities.invokeLater(() -> promptToUpdate(updater, true));
                }
            };
            t.start();
        }

        window.setVisible(visible);
    }
    
    public void promptToUpdate(BaseUpdater updater, boolean allowSkip) {
        String skip = "Skip this version";
        String notnow = "Not now";
        String update = "Install and relaunch";
        String[] options;
        if (allowSkip)
            options = new String[] { update, notnow, skip };
        else
            options = new String[] { update, notnow };

        JPanel panel = null;

        try {
            panel = (JPanel)renderer.render(DVAShell.class.getResource("/jb/dvacommon/ui/resources/updateinfopanel.xml"));
            updateInfoLabel.setText("<html>A newer version of DVA is available. <br><br>It is recommended that you update DVA to the latest version to ensure<br>the most reliable and stable experience, as well as any improvements<br>made to it.<br><br>Installed version: " + DVA.VersionString + "<br>New version: " + updater.getLatestVersion());
            File tempHtml = File.createTempFile("dvatmp", ".html");
            FileUtilities.copyStream(updater.getVersionHistoryUrl(updater.getLatestVersion()).openStream(), tempHtml);
            updateVersionHistoryPane.setPage(tempHtml.toURI().toURL());
        } catch (MalformedURLException e) {
            updateVersionHistoryPane.setText(e.toString());
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }

        Object chosen = JOptionPane.showOptionDialog(
                null,
                panel,
                "Update available",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (chosen.equals(0))
        {
            ProgressWindow pw = new ProgressWindow("Download Progress", "Downloading update...");
            updater.downloadAndInstall(updater.getLatestVersion(), new ProgressAdapter(pw));
        }
        else if (chosen.equals(2))
        {
            Settings.markUpdateSuppressed(updater.getLatestVersion());
        }
    }
    
    @SuppressWarnings("serial")
    public Action showDVAPanelAction = new AbstractAction("Go to DVA") {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(0);
        }
    };

    @SuppressWarnings("serial")
    public Action showIndicatorsManualPanelAction = new AbstractAction("Go to Indicators, Manual") {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(1);
            plasmaUI.setTabIndex(0);
        }
    };

    @SuppressWarnings("serial")
    public Action showIndicatorsTimetablePanelAction = new AbstractAction("Go to Indicators, Timetable") {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(1);
            plasmaUI.setTabIndex(1);
        }
    };

    @SuppressWarnings("serial")
    public Action specialSoundsAction = new AbstractAction("Enable/Disable Special Sounds") {
        public void actionPerformed(ActionEvent e) {
            boolean specialSoundsEnabled = Settings.specialSoundsEnabled();
            Settings.setSpecialSoundsEnabled(!specialSoundsEnabled);
            JOptionPane.showMessageDialog(window, "Changing this setting requires re-launching DVA. DVA will now close.", "Restart Required", JOptionPane.WARNING_MESSAGE);
            controller.quit();
        }
    };
    
    @SuppressWarnings("serial")
    public Action aboutAction = new AbstractAction("About", null) {
        public void actionPerformed(ActionEvent e) {
            LoadWindow lw = new LoadWindow();
            lw.show(true, true, true);
        }
    };

    @SuppressWarnings("serial")
    public Action helpAction = new AbstractAction("Help", new ImageIcon(DVAUI.class.getResource("/toolbarButtonGraphics/general/Help24.gif"))) {
        public void actionPerformed(ActionEvent e) {
            new HelpWindow().showHelp();
        }
    };

    @SuppressWarnings("serial")
    public Action versionHistoryAction = new AbstractAction("Version History", null) {
        public void actionPerformed(ActionEvent e) {
            new HelpWindow().showVersionHistory();
        }
    };

    @SuppressWarnings("serial")
    public Action relaunchWithLogging = new AbstractAction("Relaunch with Logging", null) {
        public void actionPerformed(ActionEvent e)
        {
            try {
                File jar = new File(DVA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                if (SwingEngine.isMacOSX())
                {
                    File executable = new File(jar.getParent(), "dva");
                    new ProcessBuilder("open", "/Applications/Utilities/Terminal.app", executable.getPath()).start();
                }
                else
                {
                    File executable = new File(jar.getParent(), "dva.exe");
                    JOptionPane.showMessageDialog(null, executable.getPath());
                    Runtime.getRuntime().exec("cmd.exe /c start cmd /c \"" + executable.getPath() + "\" && pause");
                }
            } catch (URISyntaxException | IOException ex) {
                ExceptionReporter.reportException(ex);
            }
        }
    };

    @SuppressWarnings("serial")
    public Action checkForUpdateAction = new AbstractAction("Check for Update", null) {
        public void actionPerformed(ActionEvent e) {
            final BaseUpdater updater = Updater.updateAvailable(DVA.VersionString, Settings.getUpdateSuppressed());
            if (updater != null) {
                promptToUpdate(updater, false);
            } else {
                JOptionPane.showMessageDialog(window, "You already have the latest version (" + DVA.VersionString + ") installed.");
            }
        }
    };
    
    @SuppressWarnings("serial")
    public Action quitAction = new AbstractAction("Quit", new ImageIcon(DVAUI.class.getResource("/toolbarButtonGraphics/general/Stop24.gif"))) {
        public void actionPerformed(ActionEvent e) {
            controller.quit();
        }
    };
    
    public Action cutAction = new DefaultEditorKit.CutAction();
    public Action copyAction = new DefaultEditorKit.CopyAction();
    public Action pasteAction = new DefaultEditorKit.PasteAction();
    
    @SuppressWarnings("serial")
    public Action selectAllAction = new TextAction("Select All") {
        public void actionPerformed(ActionEvent e) {
            getFocusedComponent().selectAll();
        }
    };
}
