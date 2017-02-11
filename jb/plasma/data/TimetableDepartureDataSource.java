package jb.plasma.data;

import jb.common.IntPair;
import jb.plasma.CityrailLine;
import jb.plasma.Phraser;
import jb.plasma.Timetable;
import jb.plasma.TimetableLineSchedule;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jb on 11/2/17.
 */
public class TimetableDepartureDataSource implements IDepartureDataSource
{
    private List<DepartureData> data;

    // Picks out a list of train departures given a line, direction, station,
    // and time.
    public TimetableDepartureDataSource(Timetable tt, String lineName, String directionName, String stationName,
                                        Calendar afterTime, int platform, int cars) throws Exception
    {
        data = new LinkedList<>();
        TimetableLineSchedule sched = tt.lines.get(lineName).directions.get(directionName);

        int stationIndex = sched.stations.indexOf(stationName);
        if (stationIndex < 0)
            throw new Exception("Station " + stationName + " not found in the schedule");

        int trainIndex;

        for (int i = 0; i < sched.convertedTimes.get(stationIndex).length; i++) {
            // Find the first departure that is after the given time. Want to
            // get the list of trains after that time.
            IntPair departureRaw = sched.convertedTimes.get(stationIndex)[i];
            if (departureRaw == null)
                continue;

            Calendar departure = departureRaw.asCalendar();
            if (departure.compareTo(afterTime) > 0) {
                // Found a train leaving the requested station after the
                // requested time.
                // Generate info for the indicator board from the remainder of
                // the schedule.
                trainIndex = i;

                String destination = null;
                List<String> stops = new LinkedList<>();
                List<String> allStops = new LinkedList<>();
                boolean isLimitedStops = false;
                boolean foundEmptyTime = false;
                for (int j = 0; j <= stationIndex; j++) {
                    if (sched.convertedTimes.get(j)[trainIndex] != null) {
                        allStops.add(sched.stations.get(j));
                    }
                }

                // Generate the list of stops for the service. By the time this
                // loop is done, 'destination' will
                // hold the name of the last station, and 'isLimitedStops' will
                // indicate whether it is limited
                // or all stops.
                for (int j = stationIndex + 1; j < sched.stations.size(); j++) {
                    if (sched.convertedTimes.get(j)[trainIndex] != null) {
                        destination = sched.stations.get(j);
                        stops.add(sched.stations.get(j));
                        allStops.add(sched.stations.get(j));
                        // Found another time in the schedule, but have already
                        // seen an empty time, so it must be limited stops
                        if (foundEmptyTime)
                            isLimitedStops = true;
                    } else if (destination != null) {
                        foundEmptyTime = true;
                    }
                }
                if (destination == null)
                {
                    destination = "Terminates";
                }

                // Create a DepartureData object to hold the info and add it to
                // the list.
                DepartureData d = new DepartureData();
                d.Destination = destination;
                CityrailLine line = CityrailLine.find(lineName, allStops);
                if (line != null) {
                    d.Line = line.Name;
                }
                d.Type = isLimitedStops ? "Limited Stops" : "All Stops";
                d.Platform = platform;
                d.Cars = cars;
                d.DueOut = departure;
                d.Stops = stops.toArray(new String[stops.size()]);
                d.Destination2 = new Phraser().getVia(d);
                data.add(d);
            }
        }
    }

    @Override
    public void notifyDeparture()
    {
        if (data.size() > 0)
            data.remove(0);
    }

    @Override
    // Picks out a list of train departures given a line, direction, station,
    // and time.
    public List<DepartureData> getDepartureData()
    {
        return data;
    }
}
