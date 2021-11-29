package jb.plasma.gtfs;

import jb.plasma.DepartureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GtfsTimetableTranslator
{
    final static Logger logger = LoggerFactory.getLogger(GtfsTimetableTranslator.class);
    private GtfsTimetable tt;
    private static GtfsTimetableTranslator instance;

    private GtfsTimetableTranslator(GtfsTimetable tt) { this.tt = tt; }

    public static GtfsTimetableTranslator getInstance()
    {
        return instance;
    }

    public static void initialize(GtfsTimetable tt) {instance = new GtfsTimetableTranslator(tt);}

    public LocalDateTime downloadTimestamp() {return tt.DownloadTimestamp;}
    public LocalDateTime expiryTime() {return tt.ExpiryTime;}

    public String[] getStations()
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent == null)
                .map(s -> s.Name)
                .sorted()
                .toArray(String[]::new);
    }

    public String[] getPlatformsForStation(String stationName)
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent != null && s.Parent.Name.equals(stationName))
                .map(s -> s.Name)
                .sorted()
                .toArray(String[]::new);
    }

    public String[] getRoutesForStation(String stationName)
    {
        return tt.RoutesByStation.get(tt.StopsByName.get(stationName)).stream()
                .map(r -> r.Description)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    public List<DepartureData> getDepartureDataForStation(
            String stationLocationName,
            String stationPlatformLocationName,
            String routeName,
            int limit)
    {
        List<DepartureData> dd = new LinkedList<DepartureData>();

        Stream<Stop> platforms = null;
        if (stationPlatformLocationName != null)
        {
            platforms = Arrays.stream(new Stop[] {tt.StopsByName.get(stationPlatformLocationName)});
        }
        else
        {
            Stop station = tt.StopsByName.get(stationLocationName);
            platforms = tt.Stops.values().stream()
                    .filter(s -> s.Parent == station);
        }

        final Set<Route> routes = routeName != null
                ? tt.Routes.values().stream().filter(r -> r.Description.equals(routeName))
                    .collect(Collectors.toSet())
                : null;

        Stream<TripInstance> upcoming = platforms
            .flatMap(platform -> tt.StopTimesByStop.get(platform).stream())
            .filter(st -> st.Pickup)
            .filter(st -> routes == null || routes.contains(st.Trip.Route))
            .flatMap(st -> expandTrips(st, tt))
            .sorted((t1, t2) -> t1.At.compareTo(t2.At))
            .limit(limit > 0 ? limit : Integer.MAX_VALUE);

        for (TripInstance ti : upcoming.collect(Collectors.toList())) {
            if (limit > 0)
                logger.info("Trip {} to {} due out {} {}", ti.Trip.Name, ti.Trip.Headsign, ti.At,
                        ti.BlockContinuingTrip != null ? " continues as " + ti.BlockContinuingTrip.Name : "");

            DepartureData d = new DepartureData();
            String[] headsignParts = ti.Trip.Headsign.split(" via ");
            d.Destination = headsignParts[0];
            d.Destination2 = headsignParts.length >= 2 ? "via " + headsignParts[1] : "";
            d.Line = ti.Trip.Route.Description;
            d.Stops = ti.getRemainingStopList();
            d.Platform = Integer.parseInt(ti.Platform.Name.split(" Station Platform ")[1]);
            d.Cars = ti.Trip.Cars;
            Calendar c = Calendar.getInstance();
            c.setTime(Date.from(ti.At
                    .atZone(ZoneId.systemDefault())
                    .toInstant()));
            d.DueOut = c;
            dd.add(d);
        }

        return dd;
    }

    private Stream<TripInstance> expandTrips(StopTime tripTimeAndPlace, GtfsTimetable tt)
    {
        LinkedList<TripInstance> list = new LinkedList<TripInstance>();
        LocalDateTime now = LocalDateTime.now();

        if (tripTimeAndPlace.Trip.Route.Id.equals("RTTA_REV") || tripTimeAndPlace.Trip.Route.Id.equals("RTTA_DEF") || tripTimeAndPlace.Trip.Headsign.equals("Empty Train"))
        {
            return Stream.empty();
        }
        else if (tripTimeAndPlace.Trip.ServicePeriod.StartDate.isAfter(LocalDate.now().plusDays(14)))
        {
            return Stream.empty();
        }

        for (LocalDate date = tripTimeAndPlace.Trip.ServicePeriod.StartDate;
             !date.isAfter(tripTimeAndPlace.Trip.ServicePeriod.EndDate);
             date = date.plusDays(1))
        {
            if ((date.getDayOfWeek() == DayOfWeek.MONDAY && tripTimeAndPlace.Trip.ServicePeriod.Monday) ||
                    (date.getDayOfWeek() == DayOfWeek.TUESDAY && tripTimeAndPlace.Trip.ServicePeriod.Tuesday) ||
                    (date.getDayOfWeek() == DayOfWeek.WEDNESDAY && tripTimeAndPlace.Trip.ServicePeriod.Wednesday) ||
                    (date.getDayOfWeek() == DayOfWeek.THURSDAY && tripTimeAndPlace.Trip.ServicePeriod.Thursday) ||
                    (date.getDayOfWeek() == DayOfWeek.FRIDAY && tripTimeAndPlace.Trip.ServicePeriod.Friday) ||
                    (date.getDayOfWeek() == DayOfWeek.SATURDAY && tripTimeAndPlace.Trip.ServicePeriod.Saturday) ||
                    (date.getDayOfWeek() == DayOfWeek.SUNDAY && tripTimeAndPlace.Trip.ServicePeriod.Sunday))
            {
                final LocalDate dateCopy = date;
                List<NormalizedStopTime> tripStopTimes = tt.StopTimesByTrip.get(tripTimeAndPlace.Trip).stream()
                        .map(tst -> new NormalizedStopTime(tst, dateCopy))
                        .collect(Collectors.toList());

                Trip blockContinuingTrip = null;
                if (tripTimeAndPlace.Trip.BlockId != null && tripTimeAndPlace.Trip.BlockId.length() > 0)
                {
                    tripStopTimes = new LinkedList<NormalizedStopTime>(tripStopTimes);
                    NormalizedStopTime tripLastStopTime = tripStopTimes.get(tripStopTimes.size() - 1);

                    for (Trip blockTrip : tt.TripsByBlockId.get(tripTimeAndPlace.Trip.BlockId).stream()
                            // Only look at up to 2 following trips later in the same block
                            .filter(bt -> bt != tripTimeAndPlace.Trip)
                            .filter(bt -> bt.Name.compareTo(tripTimeAndPlace.Trip.Name) > 0)
                            .sorted((bt1, bt2) -> bt1.Name.compareTo(bt2.Name))
                            .limit(2)
                            .collect(Collectors.toList()))
                    {
                        List<NormalizedStopTime> blockStopTimes = tt.StopTimesByTrip.get(blockTrip).stream()
                                .map(bst -> new NormalizedStopTime(bst, dateCopy))
                                .collect(Collectors.toList());;

                        for (NormalizedStopTime bst : blockStopTimes)
                        {
                            if (ChronoUnit.SECONDS.between(tripLastStopTime.NormalizedDeparture, bst.NormalizedDeparture) <= 1) {
                                logger.info("Detected trip {} continuing from trip {} (block id {})",
                                        blockTrip.Name, tripTimeAndPlace.Trip.Name, tripTimeAndPlace.Trip.BlockId);
                                blockContinuingTrip = blockTrip;
                            } else if (blockContinuingTrip != null) {
                                // Detect train that terminates + returns
                                // On a double track line it will likely be a different platform so Stop.Id will be
                                // different so check Stop.Parent.Id instead.
                                if (tripStopTimes.stream().anyMatch(tst -> tst.StopTime.Stop.Parent == bst.StopTime.Stop.Parent)) {
                                    break;
                                } else{
                                    tripStopTimes.add(bst);
                                }
                            }
                        }

                        if (blockContinuingTrip != null) break;
                    }
                }

                TripInstance ti = new TripInstance(tripTimeAndPlace, date, tripStopTimes, blockContinuingTrip);

                if (ti.At.isAfter(now) && ti.At.isBefore(now.plusDays(7)))
                    list.add(ti);
            }
        }
        return list.stream();
    }

    // Represents a potential departure of a trip from a given place which will occur at a given time.
    private class TripInstance
    {
        public TripInstance(StopTime tripTimeAndPlace,
                            LocalDate date,
                            List<NormalizedStopTime> stopTimes,
                            Trip blockContinuingTrip)
        {
            Trip = tripTimeAndPlace.Trip;
            Date = date;
            Platform = tripTimeAndPlace.Stop;
            NormalizedStopTimes = stopTimes;
            BlockContinuingTrip = blockContinuingTrip;
            At = new NormalizedStopTime(tripTimeAndPlace, date).NormalizedDeparture;
        }

        public Trip Trip;
        public LocalDate Date;
        public Stop Platform;
        public List<NormalizedStopTime> NormalizedStopTimes;
        public Trip BlockContinuingTrip;
        public LocalDateTime At;

        String[] getRemainingStopList()
        {
            List<String> stops = new LinkedList<String>();
            boolean found = false;
            for (NormalizedStopTime nst : NormalizedStopTimes)
            {
                if (nst.StopTime.Stop == Platform) found = true;
                else if (found && nst.StopTime.Dropoff) stops.add(nst.StopTime.Stop.Name);
            }

            return stops.stream()
                    .map(s -> s.split(" Station Platform ")[0])
                    .toArray(String[]::new);
        }
    }

    private class NormalizedStopTime
    {
        public NormalizedStopTime(StopTime st, LocalDate date)
        {
            StopTime = st;
            LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
            String[] timeParts = st.Departure.split(":");
            int h = Integer.parseInt(timeParts[0]);
            int m = Integer.parseInt(timeParts[1]);
            int s = Integer.parseInt(timeParts[2]);

            if (h >= 24) {
                normalizedDate = normalizedDate.plusDays(1);
                h -= 24;
            }

            NormalizedDeparture = LocalDateTime.of(normalizedDate, LocalTime.of(h, m, s));
        }

        public StopTime StopTime;
        public LocalDateTime NormalizedDeparture;
    }
}
