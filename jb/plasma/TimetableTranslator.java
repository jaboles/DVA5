package jb.plasma;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import jb.common.IntPair;
import jb.common.StringUtilities;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Converts the CityRail timetable data into a format usable by DVA.
public class TimetableTranslator
{
    final static Logger logger = LoggerFactory.getLogger(TimetableTranslator.class);
    private static Map<Pair<String, String>, List<Pair<String, String>>> continuations;
    private Timetable tt;
    private static DateFormat TimeFormat = new SimpleDateFormat("HH:mm");

    static {
        continuations = new HashMap<>();
        // North Shore -> West
        addContinuation(
                "T1 North Shore & Northern",
                "Berowra to City via Gordon, Hornsby to City via Macquarie University",
                "T1 Western",
                "City to Emu Plains, City to Richmond");
        // North Shore -> Northern
        addContinuation(
                "T1 North Shore & Northern",
                "Berowra to City via Gordon, Hornsby to City via Macquarie University",
                "T1 Northern",
                "City to Epping and Hornsby via Strathfield");
        // Western -> North Shore
        addContinuation(
                "T1 Western",
                "Emu Plains to City, Richmond to City",
                "T1 North Shore & Northern",
                "City to Berowra via Gordon, City to Hornsby via Macquarie University");
        // Northern -> North Shore
        addContinuation(
                "T1 Northern",
                "Hornsby and Epping to City via Strathfield",
                "T1 North Shore & Northern",
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
        return tt.lineNames.toArray(new String[0]);
    }

    public String[] getDirectionsForLine(String lineName)
    {
        return tt.lines.get(lineName).directionNames.toArray(new String[0]);
    }

    public String[] getStationsForLineAndDirection(String lineName, String directionName)
    {
        TimetableLineSchedule sched = tt.lines.get(lineName).directions.get(directionName);
        if (sched != null && sched.stations.size() > 0)
            return sched.stations.toArray(new String[0]);
        else
            return new String[] {};
    }

    // Picks out a list of train departures given a line, direction, station,
    // and time.
    public List<DepartureData> getDepartureDataForStation(String lineName, String directionName, String stationName,
            Calendar time, int atOrAfterTime, int platform, int cars, int limit, boolean throwIfNotFound) throws Exception
    {
        List<DepartureData> dd = new LinkedList<>();
        TimetableLine ttLine = tt.lines.get(lineName);
        if (ttLine == null) {
            if (throwIfNotFound)
                throw new Exception("Line " + lineName + " not found in the schedule");
            else
                return dd;
        }
        TimetableLineSchedule sched = ttLine.directions.get(directionName);
        if (sched == null) {
            if (throwIfNotFound)
                throw new Exception("Direction " + directionName + " not found in the schedule for line " + lineName);
            else
                return dd;
        }

        int stationIndex = sched.stations.indexOf(stationName);
        if (stationIndex < 0) {
            if (throwIfNotFound)
                throw new Exception("Station " + stationName + " not found in the schedule");
            else
                return dd;
        }

        int trainIndex;
        int trainCount = 0;

        Pair<String, String> continuationsKey = new Pair<>(lineName, directionName);
        List<Pair<String, String>> continuationList = continuations.get(continuationsKey);
        if (continuationList != null)
        {
            for (Pair<String, String> continuation : continuationList) {
                logger.info("Found continuation for {} / {}: {} / {}", lineName, directionName, continuation.getValue0(), continuation.getValue1());
            }
        }
        else
        {
            logger.info("No continuation for {} / {}", lineName, directionName);
        }

        for (int i = 0; i < sched.convertedTimes.get(stationIndex).length; i++) {
            // Find the first departure that is after the given time. Want to
            // get the list of trains after that time.
            IntPair departureRaw = sched.convertedTimes.get(stationIndex)[i];
            if (departureRaw == null)
                continue;

            Calendar departure = departureRaw.asCalendar();
            if ((departure.compareTo(time) > 0 && atOrAfterTime == AtOrAfter.AFTER)
              || departure.get(Calendar.HOUR_OF_DAY) == time.get(Calendar.HOUR_OF_DAY) && departure.get(Calendar.MINUTE) == time.get(Calendar.MINUTE) && atOrAfterTime == AtOrAfter.AT) {
                // Found a train leaving the requested station after the
                // requested time.
                // Generate info for the indicator board from the remainder of
                // the schedule.
                trainIndex = i;
                trainCount++;

                String destination = null;
                IntPair destinationTime = null;
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
                    IntPair stopTime = sched.convertedTimes.get(j)[trainIndex];
                    if (stopTime != null) {
                        destination = sched.stations.get(j);
                        destinationTime = stopTime;
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

                if (continuationList != null)
                {
                    for (Pair<String, String> continuation : continuationList) {
                        logger.info("Looking for a continuation from {} at {}", destination, destinationTime.asCalendar().getTime());
                        List<DepartureData> continuationDataList = getDepartureDataForStation(continuation.getValue0(),
                                continuation.getValue1(), destination, destinationTime.asCalendar(), AtOrAfter.AT,
                                0, 0, 1, false);
                        if (continuationDataList.size() > 0) {
                            DepartureData continuationData = continuationDataList.get(0);
                            destination = continuationData.Destination;
                            for (String s : continuationData.Stops) {
                                stops.add(s);
                            }
                            // TODO: don't use string compare here
                            isLimitedStops |= continuationData.Type.equals("Limited Stops");
                            break;
                        }
                    }
                }

                // Create a DepartureData object to hold the info and add it to
                // the list.
                CityrailLine line = CityrailLine.find(lineName/*, allStops*/);
                dd.add(new ManualDepartureData(
                        destination,
                        null, // TODO - new Phraser().getVia(d)
                        line != null ? line.Name : null,
                        isLimitedStops ? "Limited Stops" : "All Stops",
                        cars,
                        platform,
                        stops.toArray(new String[stops.size()]),
                        LocalDateTime.now(), // TODO - departure,
                        null,
                        null,
                        null,
                        null
                ));

                logger.info("Found departure to {} at {} stopping at: {}", destination, TimeFormat.format(departure.getTime()), StringUtilities.join(", ", stops));
                if (limit > 0 && trainCount >= limit) break;
            }
        }

        logger.info("getDepartureData returning {} train(s)", trainCount);
        return dd;
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

    public class AtOrAfter
    {
        public static final int AT = 1;
        public static final int AFTER = 2;
    }
}