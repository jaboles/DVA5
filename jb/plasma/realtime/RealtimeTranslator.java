package jb.plasma.realtime;

import jb.plasma.CityrailLine;
import jb.plasma.data.IDepartureDataSource;
import jb.plasma.IScheduleTranslator;
import jb.plasma.data.DepartureData;

import java.util.List;

/**
 * Created by jb on 16/2/17.
 */
public class RealtimeTranslator implements IScheduleTranslator, IDepartureDataSource {
    @Override
    public void notifyDeparture() {
    }

    @Override
    public List<DepartureData> getDepartureData() {
        return null;
    }

    @Override
    public String[] getLines() {
        return CityrailLine.allLineNames.toArray(new String[0]);
    }

    @Override
    public String[] getDirectionsForLine(String lineName) {
        return new String[] {"up", "down"};
    }

    @Override
    public String[] getStationsForLineAndDirection(String lineName, String directionName) {
        return new String[0];
    }
}
