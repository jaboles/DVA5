package jb.plasma.ui;

import jb.common.ExceptionReporter;
import jb.common.ui.WindowUtils;
import jb.plasma.Timetable;
import org.swixml.SwingEngine;

import javax.swing.*;
import java.util.List;
import java.awt.event.ActionEvent;

public class DownloadTimetableDialog
{
    private JDialog dialog;
    public JTextField timetableName;
    public JRadioButton weekday;
    public JRadioButton weekend;
    private boolean accepted;
    private List<String> existingNames;

    public DownloadTimetableDialog(String newName, int type, List<String> existingNames)
    {
        try {
            this.existingNames = existingNames;
            SwingEngine renderer = new SwingEngine(this);
            dialog = (JDialog) renderer.render(ScreenSaverSettingsDialog.class.getResource("/jb/plasma/ui/resources/downloadtimetable.xml"));
            timetableName.setText(newName);
            dialog.pack();
            WindowUtils.center(dialog);
            if (type == Timetable.TIMETABLE_WEEKDAY) {
                weekday.setSelected(true);
            } else if (type == Timetable.TIMETABLE_WEEKEND) {
                weekend.setSelected(true);
            }
            dialog.setVisible(true);
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    public Action okAction = new AbstractAction("OK") {
        public void actionPerformed(ActionEvent e) {
            if (existingNames.contains(timetableName.getText()))
            {
                JOptionPane.showMessageDialog(null, "A timetable with name '" + timetableName.getText() + "' already exists.");
            }
            else
            {
                accepted = true;
                dialog.setVisible(false);
            }
        }
    };

    public Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
    };

    public boolean accepted() { return accepted; }

    public String timetableName() { return timetableName.getText(); }

    public int timetableType() { return weekday.isSelected() ? Timetable.TIMETABLE_WEEKDAY : Timetable.TIMETABLE_WEEKEND; }
}
