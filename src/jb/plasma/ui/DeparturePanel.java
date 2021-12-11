package jb.plasma.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import jb.common.ui.ColorComboBox;
import jb.common.ui.SimpleEditorUndoRedoKit;
import jb.dva.Script;
import jb.dvacommon.DVA;
import jb.dvacommon.ui.DVATextArea;
import jb.dvacommon.ui.DVATextField;
import jb.dvacommon.ui.FileTextField;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ManualDepartureData;
import org.swixml.SwingEngine;

// Panel for editing a train departure.
@SuppressWarnings("serial")
public class DeparturePanel extends JPanel
{
    private Script script;
    public JLabel titleLabel;
    public JPanel gridPanel;
    public DVATextField destinationValue;
    public JLabel destinationIndicatorIconLabel;
    public DVATextField destination2Value;
    public JLabel destination2IndicatorIconLabel;
    public JComboBox<String> serviceTypeValue;
    public JTextField departureTimeValue;
    public JSpinner carsValue;
    public JSpinner platformValue;
    public JLabel indicatorIconLabel;
    public DVATextArea stationsValue;
    public JComboBox<String> lineValue;
    public ColorComboBox color1Value;
    public ColorComboBox color2Value;
    public ColorComboBox textColorValue;
    public FileTextField customAnnouncementText;
    public JLabel customAnnouncementIndicatorIconLabel;

    public DeparturePanel(String title, DVA dva, String soundLibraryName)
    {
        SwingEngine renderer = new SwingEngine(this);
        renderer.getTaglib().registerTag("dvatextarea", DVATextArea.class);
        renderer.getTaglib().registerTag("dvatextfield", DVATextField.class);
        renderer.getTaglib().registerTag("filetextfield", FileTextField.class);
        try {
            JPanel panel = (JPanel)renderer.render(DeparturePanel.class.getResource("/jb/plasma/ui/resources/departurepanel.xml"));
            if (dva != null && soundLibraryName != null)
            {
                this.script = new Script(soundLibraryName, "");
                stationsValue.initialize(dva, script, indicatorIconLabel);
                destinationValue.initialize(dva, script, destinationIndicatorIconLabel);
                destination2Value.initialize(dva, script, destination2IndicatorIconLabel);
            }
            customAnnouncementText.initialize(customAnnouncementIndicatorIconLabel);
            add(panel);
            
            SimpleEditorUndoRedoKit.enableUndo(destinationValue);
            SimpleEditorUndoRedoKit.enableUndo(destination2Value);
            SimpleEditorUndoRedoKit.enableUndo(departureTimeValue);
            SimpleEditorUndoRedoKit.enableUndo(stationsValue);
            SimpleEditorUndoRedoKit.enableUndo(customAnnouncementText);
            SimpleEditorUndoRedoKit.enableUndo((JTextComponent)lineValue.getEditor().getEditorComponent());
            SimpleEditorUndoRedoKit.enableUndo((JTextComponent)serviceTypeValue.getEditor().getEditorComponent());

            stationsValue.setFont(titleLabel.getFont());
            titleLabel.setText(title);
            if (!SwingEngine.isMacOSX())
                ((GridLayout)gridPanel.getLayout()).setHgap(5);

            carsValue.setModel(new SpinnerNumberModel(8, 1, 32, 1));
            platformValue.setModel(new SpinnerNumberModel(1, 1, 99, 1));

            for (String s : DepartureData.DefaultServiceTypes)
            {
                serviceTypeValue.addItem(s);
            }
            serviceTypeValue.setSelectedIndex(0);

            for (String s : CityrailLine.allLineNames)
            {
                lineValue.addItem(s);

                CityrailLine line = CityrailLine.get(s);
                color1Value.addItem(line.Color1);
                color2Value.addItem(line.Color2);
                textColorValue.addItem(line.TextColor);
            }

            lineValue.addActionListener(e -> {
                String lineName = (String)lineValue.getSelectedItem();
                CityrailLine line = CityrailLine.get(lineName);
                if (line != null)
                {
                    color1Value.setSelectedItem(line.Color1);
                    color2Value.setSelectedItem(line.Color2);
                    textColorValue.setSelectedItem(line.TextColor);
                }
            });

            lineValue.setSelectedIndex(0);

        } catch (Exception e) {
            jb.common.ExceptionReporter.reportException(e);
        }
    }

    public void setData(DepartureData d)
    {
        destinationValue.setText(d.Destination);
        destination2Value.setText(d.Destination2);
        serviceTypeValue.setSelectedItem(d.Type);
        carsValue.setValue(d.Cars);
        platformValue.setValue(d.Platform);
        departureTimeValue.setText(d.DueOut != null ? d.dueOutAsString() : "");
        stationsValue.setText(d.stopsAsString());
        CityrailLine line = CityrailLine.get(d.Line);
        
        if (d.Color1Override != null)
            color1Value.setSelectedItem(d.Color1Override);
        else if (line != null)
            color1Value.setSelectedItem(line.Color1);
        
        if (d.Color2Override != null)
            color2Value.setSelectedItem(d.Color2Override);
        else if (line != null)
            color2Value.setSelectedItem(line.Color2);
        
        if (d.TextColorOverride != null)
            textColorValue.setSelectedItem(d.TextColorOverride);
        else if (line != null)
            textColorValue.setSelectedItem(line.TextColor);
        
        lineValue.setSelectedItem(d.Line);
        customAnnouncementText.setText(d.CustomAnnouncementPath != null ? d.CustomAnnouncementPath : "");
    }

    public DepartureData getData()
    {
        return new ManualDepartureData(
            destinationValue.getText(),
            destination2Value.getText(),
            lineValue.getSelectedItem().toString(),
            serviceTypeValue.getSelectedItem() != null ? serviceTypeValue.getSelectedItem().toString() : "",
            (Integer)carsValue.getValue(),
            (Integer)platformValue.getValue(),
            stationsValue.getText(),
            departureTimeValue.getText(),
            color1Value.getSelectedItem() instanceof Color? (Color)color1Value.getSelectedItem() : null,
            color2Value.getSelectedItem() instanceof Color? (Color)color2Value.getSelectedItem() : null,
            textColorValue.getSelectedItem() instanceof Color? (Color)textColorValue.getSelectedItem() : null,
            (customAnnouncementText.getText() != null && customAnnouncementText.getText().length() > 0) ? customAnnouncementText.getText() : null
        );
    }
    
    public void setScriptVoice(String voiceName)
    {
        if (script != null)
            script.setVoice(voiceName);
        stationsValue.verify();
    }

    public Action customAnnouncementBrowseAction = new AbstractAction("Choose custom announcement", null) {
        public void actionPerformed(ActionEvent e) {
            JFileChooser dlg = new JFileChooser();
            FileNameExtensionFilter wavFilter = new FileNameExtensionFilter("WAV Sound Files", "wav");
            FileNameExtensionFilter mp3Filter = new FileNameExtensionFilter("MP3 Sound Files", "mp3");
            dlg.addChoosableFileFilter(mp3Filter);
            dlg.addChoosableFileFilter(wavFilter);
            dlg.setFileFilter(mp3Filter);
            int retval = dlg.showOpenDialog(DeparturePanel.this);
            if (retval == JFileChooser.APPROVE_OPTION)
            {
                customAnnouncementText.setText(dlg.getSelectedFile().getPath());
            }
        }
    };
}