package jb.plasma.ui;

import jb.common.ExceptionReporter;
import jb.common.ui.AspectRatioLayout;
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

    @Override
    public Component getListCellRendererComponent(JList<? extends Drawer> list, Drawer value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0 || value instanceof NullDrawer) {
            return defaultRenderer.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
        }

        List<DepartureData> sampleData = new ArrayList<>();
        sampleData.add(new ManualDepartureData("Destination", "via Location", CityrailLine.T8.Name, "Limited Stops",
            8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(3),
            null, null, null, null));
        sampleData.add(new ManualDepartureData("Next Destination", "via Location", CityrailLine.T2.Name, "Limited Stops",
                8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(7),
                null, null, null, null));
        sampleData.add(new ManualDepartureData("Next Destination", "via Location", CityrailLine.T9.Name, "Limited Stops",
                8, 1, new String[] {"Stop 1", "Stop 2", "Stop 3"}, LocalDateTime.now().plusMinutes(10),
                null, null, null, null));

        try {
            Color background = defaultRenderer.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus).getBackground();

            Drawer d = (Drawer)value.clone();
            d.dataChanged(sampleData);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JPanel preview = new ProportionalPanel(d.getAspectRatio(), new PlasmaPanel(d), null);
            preview.setPreferredSize(new Dimension(100, 75));
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
