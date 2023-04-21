package jb.plasma.ui;

import jb.plasma.GtfsDepartureData;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GtfsTableModel extends AbstractTableModel {
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("dd/MM  HH:mm");
    private static final String[] Columns = {
            "At",
            "Run #",
            "Cars",
            "Destination",
            "Route",
            "Plat",
            "Type",
            "Stops",
            "Continues",
    };

    private final List<GtfsDepartureData> departureData;

    public GtfsTableModel(List<GtfsDepartureData> departureData) {
        this.departureData = departureData;
    }

    @Override
    public int getColumnCount() { return Columns.length; }
    @Override
    public String getColumnName(int col) { return Columns[col]; }
    @Override
    public int getRowCount() { return departureData.size(); }
    @Override
    public Object getValueAt(int row, int column) {
        GtfsDepartureData dd = departureData.get(row);
        switch (column) {
            case 0: return dd.tripInstance.At.format(TimeFormat);
            case 1: return dd.tripInstance.Trip.Id.split("\\.")[0].replace("-","");
            case 2: return dd.tripInstance.Trip.SetType;
            case 3: return dd.Destination;
            case 4: return dd.tripInstance.Trip.Route;
            case 5: return dd.Platform;
            case 6: return dd.Type;
            case 7: return dd.stopsAsString();
            case 8: return dd.tripInstance.BlockContinuingTrip != null ? dd.tripInstance.BlockContinuingTrip.Id.split("\\.")[0].replace("-","") : null;
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
}
