package jb.plasma.gtfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GtfsCsvReader
{
    public static HashMap<String, Stop> readStops(Path stopsTxt) throws IOException
    {
        HashMap<String, Stop> map = new HashMap<>();
        Files.lines(stopsTxt).skip(1).forEach(line ->
        {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
            }
            Stop data = new Stop(
                    parts[0], // e.g. "26401"
                    parts[2], // e.g. "Albury Station Platform 1"
                    map.getOrDefault(parts[9], null) // e.g. "26401"
            );
            map.put(parts[0], data);
        });

        return map;
    }

    public static HashMap<String, Route> readRoutes(Path routesTxt) throws IOException
    {
        HashMap<String, Route> map = new HashMap<>();
        Files.lines(routesTxt).skip(1).forEach(line ->
        {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
            }
            Route data = new Route(
                    parts[0], // e.g. "APS_1a"
                    parts[3], // e.g. "City Circle to Macarthur via Airport"
                    parts[4]  // e.g. "T8 Airport & South Line"
            );
            map.put(parts[0], data);
        });

        return map;
    }

    public static HashMap<String, ServicePeriod> readServicePeriods(Path calendarTxt) throws IOException
    {
        HashMap<String, ServicePeriod> map = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);

        Files.lines(calendarTxt).skip(1).forEach(line ->
        {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
            }
            ServicePeriod data = new ServicePeriod(
                    parts[0], // e.g. "955.134.128"
                    Integer.parseInt(parts[1]) > 0, // e.g. "0" or "1"
                    Integer.parseInt(parts[2]) > 0,
                    Integer.parseInt(parts[3]) > 0,
                    Integer.parseInt(parts[4]) > 0,
                    Integer.parseInt(parts[5]) > 0,
                    Integer.parseInt(parts[6]) > 0,
                    Integer.parseInt(parts[7]) > 0,
                    LocalDate.parse(parts[8], formatter), // e.g. "20211129"
                    LocalDate.parse(parts[9], formatter)
            );
            map.put(parts[0], data);
        });

        return map;
    }

    public static HashMap<String, Trip> readTrips(Path tripsTxt, Map<String, Route> routes, Map<String, ServicePeriod> calendars) throws IOException
    {
        HashMap<String, Trip> map = new HashMap<>();

        Files.lines(tripsTxt).skip(1).forEach(line ->
        {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
            }

            String tripId = parts[2];
            String routeId = parts[0];
            Trip data = new Trip(
                    tripId, // e.g. "108B.959.129.12.T.8.68357311"
                    routes.get(routeId), // e.g. "WST_2c"
                    calendars.get(parts[1]), // e.g. "959.129.12"
                    parts[3], // e.g. "Gordon via Lindfield"
                    Integer.parseInt(parts[5].trim()), // e.g. "0" or "1"
                    parts[6]
            );
            map.put(parts[2], data);
        });

        return map;
    }

    public static List<StopTime> readStopTimes(Path stoptimesTxt, Map<String, Trip> trips, Map<String, Stop> stops) throws IOException
    {
        List<StopTime> list = new LinkedList<>();

        Files.lines(stoptimesTxt).skip(1).forEach(line ->
        {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
            }

            StopTime data = new StopTime(
                    trips.get(parts[0]), // e.g. "108B.959.129.12.T.8.68357311"
                    parts[1], // e.g. "04:46:06"
                    parts[2], // e.g. "04:46:06"
                    stops.get(parts[3]), // e.g. "2135234"
                    Integer.parseInt(parts[6].trim()) == 0, // e.g. "0" or "1". 0 actually means "yes"
                    Integer.parseInt(parts[7].trim()) == 0 // e.g. "0" or "1". 0 actually means "yes"
            );
            list.add(data);
        });

        return list;
    }
}
