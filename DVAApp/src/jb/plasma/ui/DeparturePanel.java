package jb.plasma.ui;

import java.awt.*;
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

import jb.common.ExceptionReporter;
import jb.common.ui.ColorComboBox;
import jb.common.ui.Filler;
import jb.common.ui.SimpleEditorUndoRedoKit;
import jb.dva.DVAManager;
import jb.dva.Script;
import jb.dvacommon.ui.DVATextArea;
import jb.dvacommon.ui.DVATextField;
import jb.dvacommon.ui.FileTextField;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ManualDepartureData;
import org.swixml.SwingEngine;

// Panel for editing a train departure.
public class DeparturePanel
{
    private Script script;
    @SuppressWarnings("UnusedDeclaration") private JLabel titleLabel;
    @SuppressWarnings("UnusedDeclaration") private JPanel gridPanel;
    @SuppressWarnings("UnusedDeclaration") private DVATextField destinationValue;
    @SuppressWarnings("UnusedDeclaration") private JLabel destinationIndicatorIconLabel;
    @SuppressWarnings("UnusedDeclaration") private DVATextField destination2Value;
    @SuppressWarnings("UnusedDeclaration") private JLabel destination2IndicatorIconLabel;
    @SuppressWarnings("UnusedDeclaration") private JComboBox<String> serviceTypeValue;
    @SuppressWarnings("UnusedDeclaration") private JTextField departureTimeValue;
    @SuppressWarnings("UnusedDeclaration") private JSpinner carsValue;
    @SuppressWarnings("UnusedDeclaration") private JSpinner platformValue;
    @SuppressWarnings("UnusedDeclaration") private JLabel indicatorIconLabel;
    @SuppressWarnings("UnusedDeclaration") private DVATextArea stationsValue;
    @SuppressWarnings("UnusedDeclaration") private JComboBox<String> lineValue;
    @SuppressWarnings("UnusedDeclaration") private ColorComboBox color1Value;
    @SuppressWarnings("UnusedDeclaration") private ColorComboBox color2Value;
    @SuppressWarnings("UnusedDeclaration") private ColorComboBox textColorValue;
    @SuppressWarnings("UnusedDeclaration") private FileTextField customAnnouncementText;
    @SuppressWarnings("UnusedDeclaration") private JLabel customAnnouncementIndicatorIconLabel;
    private Container panel;

    public DeparturePanel(String title, DVAManager dvaManager, String soundLibraryName)
    {
        SwingEngine renderer = new SwingEngine(this);
        renderer.getTaglib().registerTag("dvatextarea", DVATextArea.class);
        renderer.getTaglib().registerTag("dvatextfield", DVATextField.class);
        renderer.getTaglib().registerTag("filetextfield", FileTextField.class);
        renderer.getTaglib().registerTag("filler", Filler.class);
        try {
            panel = renderer.render(DeparturePanel.class.getResource("/jb/plasma/ui/resources/departurepanel.xml"));
            if (dvaManager != null && soundLibraryName != null)
            {
                this.script = new Script(soundLibraryName, "");
                stationsValue.initialize(dvaManager, script, indicatorIconLabel);
                destinationValue.initialize(dvaManager, script, destinationIndicatorIconLabel);
                destination2Value.initialize(dvaManager, script, destination2IndicatorIconLabel);
            }
            customAnnouncementText.initialize(customAnnouncementIndicatorIconLabel);

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

            carsValue.setModel(new SpinnerNumberModel(8, 0, 32, 1));
            platformValue.setModel(new SpinnerNumberModel(1, 0, 99, 1));

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
            ExceptionReporter.reportException(e);
        }
    }

    public Container getPanel() {return panel;}

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
            lineValue.getSelectedItem() != null ? lineValue.getSelectedItem().toString() : "",
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

    @SuppressWarnings("unused")
    public Action customAnnouncementBrowseAction = new AbstractAction("Choose custom announcement", null) {
        public void actionPerformed(ActionEvent e) {
            JFileChooser dlg = new JFileChooser();
            FileNameExtensionFilter wavFilter = new FileNameExtensionFilter("WAV Sound Files", "wav");
            FileNameExtensionFilter mp3Filter = new FileNameExtensionFilter("MP3 Sound Files", "mp3");
            dlg.addChoosableFileFilter(mp3Filter);
            dlg.addChoosableFileFilter(wavFilter);
            dlg.setFileFilter(mp3Filter);
            int retval = dlg.showOpenDialog(DeparturePanel.this.panel);
            if (retval == JFileChooser.APPROVE_OPTION)
            {
                customAnnouncementText.setText(dlg.getSelectedFile().getPath());
            }
        }
    };
}