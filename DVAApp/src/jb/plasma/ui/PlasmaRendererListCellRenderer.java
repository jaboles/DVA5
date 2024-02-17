package jb.plasma.ui;

import jb.common.ExceptionReporter;
import jb.common.ui.ProportionalPanel;
import jb.plasma.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlasmaRendererListCellRenderer implements ListCellRenderer<Drawer> {
    private static final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    private static final List<DepartureData> SampleData = new ArrayList<>();

    static
    {
        SampleData.add(new ManualDepartureData("Destination", "via Location", CityrailLine.T8.Name, "Limited Stops",
                8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(3),
                null, null, null, null));
        SampleData.add(new ManualDepartureData("Next Destination", "via Location", CityrailLine.T2.Name, "Limited Stops",
                8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(7),
                null, null, null, null));
        SampleData.add(new ManualDepartureData("Next Destination", "via Location", CityrailLine.T9.Name, "Limited Stops",
                8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(10),
                null, null, null, null));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Drawer> list, Drawer value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0 || value instanceof NullDrawer) {
            return defaultRenderer.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
        }

        try {
            Color background = defaultRenderer.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus).getBackground();

            Drawer d = (Drawer)value.clone();
            d.dataChanged(SampleData);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JPanel preview = new ProportionalPanel(Drawer.convertAspectRatio(d.getAspectRatio(), null), new PlasmaPanel(d, false), null);
            preview.setPreferredSize(new Dimension(120, 75));
            panel.setBackground(background);
            panel.add(preview, BorderLayout.WEST);
            JTextArea label = new JTextArea(d.toString());
            label.setBorder(new EmptyBorder(3,3,3,3));
            label.setWrapStyleWord(true);
            label.setLineWrap(true);
            label.setOpaque(false);
            label.setForeground(defaultRenderer.getForeground());
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
        catch (Exception e) {
            ExceptionReporter.reportException(e);
            return new JLabel("Error instantiating renderer");
        }
    }
}
