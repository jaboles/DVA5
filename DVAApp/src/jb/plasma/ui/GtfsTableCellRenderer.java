package jb.plasma.ui;

import jb.plasma.DepartureData;
import jb.plasma.GtfsDepartureData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.List;

public class GtfsTableCellRenderer extends DefaultTableCellRenderer {
    private final List<GtfsDepartureData> departureData;

    public GtfsTableCellRenderer(List<GtfsDepartureData> departureData) {
        this.departureData = departureData;
    }

    public java.awt.Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        GtfsDepartureData dd = departureData.get(row);
        if (column == 1) {
            c.setToolTipText(dd.tripInstance.Trip.Id);
        } else if (column == 7) {
            c.setToolTipText(dd.stopsAsString());
        } else if (column == 8 && dd.tripInstance.BlockContinuingTrip != null) {
            c.setToolTipText(dd.tripInstance.BlockContinuingTrip.Id + " to " + dd.tripInstance.BlockContinuingTrip.Headsign);
        } else {
            c.setToolTipText(null);
        }
        return c;
    }
}
