package jb.plasma;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jb.common.IntPair;
import jb.common.ObjectCache;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.innahema.collections.query.queriables.Queryable;

// Converts the CityRail timetable data into a format usable by DVA.
public class TimetableTranslator
{
    final static Logger logger = LoggerFactory.getLogger(TimetableTranslator.class);
    private static Map<Pair<String, String>, List<Pair<String, String>>> continuations;
    private Timetable tt;

    static {
        continuations = new HashMap<>();
        addContinuation("T1 North Shore & Northern",
                "Berowra to City via Gordon, Hornsby to City via Macquarie University", "T1 Western",
                "City to Emu Plains, City to Richmond");
        addContinuation("T1 Western", "Emu Plains to City, Richmond to City", "T1 North Shore & Northern",
                "City to Berowra via Gordon, City to Hornsby via Macquarie University");
    }

    public TimetableTranslator(Timetable tt) {
        this.tt = tt;
    }

    // Tidies up the schedule.
    public void cleanUpTimetable()
    {
        // For each line...
        for (String lineName : getLines()) {
            // For each direction in the line...
            for (String directionName : getDirectionsForLine(lineName)) {
                // Convert the times from strings into Calendar objects
                // representing a time
                TimetableLineSchedule sched = tt.lines.get(lineName).directions.get(directionName);
                if (sched == null) continue;
                sched.convertTimes();
                // For each station in the schedule...
                for (int i = 0; i < sched.stations.size(); i++) {
                    String stationName = sched.stations.get(i);
                    // Sort out the 'arr' and 'dep' stations.
                    if (stationName.endsWith(" arr") && sched.stations.size() >= i
                            && sched.stations.get(i + 1).endsWith(" dep")) {
                        // If there is 'arr' followed by 'dep'

                        IntPair[] arrTimes = sched.convertedTimes.get(i);
                        IntPair[] depTimes = sched.convertedTimes.get(i + 1);
                        for (int j = 0; j < arrTimes.length; j++) {
                            // If no 'dep' time, copy the 'arr' times into that
                            // row.
                            if (depTimes[j] == null)
                                depTimes[j] = arrTimes[j];
                        }

                        // Remove the 'arr' row from the timetable.
                        sched.stations.remove(i);
                        sched.times.remove(i);
                        sched.convertedTimes.remove(i);
                        i--;
                    } else if (stationName.endsWith(" arr")) {
                        // 'arr' station but no 'dep' station. Keep the row but
                        // fix up the station name
                        sched.stations.set(i, stationName.substring(0, stationName.length() - 4));
                    } else if (stationName.endsWith(" dep")) {
                        // 'dep' station. Keep the row but fix up the station
                        // name
                        sched.stations.set(i, stationName.substring(0, stationName.length() - 4));
                    }
                }
            }
        }

    }

    public String[] getLines()
    {
        return Queryable.from(tt.lineNames).toArray();
    }

    public String[] getDirectionsForLine(String lineName)
    {
        return Queryable.from(tt.lines.get(lineName).directionNames).toArray();
    }

    public String[] getStationsForLineAndDirection(String lineName, String directionName)
    {
        TimetableLineSchedule sched = tt.lines.get(lineName).directions.get(directionName);
        if (sched != null && sched.stations.size() > 0)
            return Queryable.from(sched.stations).toArray();
        else
            return new String[] {};
    }

    // Picks out a list of train departures given a line, direction, station,
    // and time.
    public List<DepartureData> getDepartureDataForStation(String lineName, String directionName, String stationName,
            Calendar afterTime, int platform, int cars) throws Exception
    {
        List<DepartureData> dd = new LinkedList<>();
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
                dd.add(d);
            }
        }

        return dd;
    }

    public List<String> findContinuation(String lineName, String directionName, List<String> stops, List<Calendar> times)
    {
        return null;
    }

    public static void addContinuation(String fromLine, String fromDirection, String toLine, String toDirection)
    {
        Pair<String, String> key = new Pair<>(fromLine, fromDirection);
        Pair<String, String> val = new Pair<>(toLine, toDirection);
        List<Pair<String, String>> valList;
        if (continuations.containsKey(key)) {
            valList = continuations.get(key);
        } else {
            valList = new LinkedList<>();
            continuations.put(key, valList);
        }
        valList.add(val);
    }
}