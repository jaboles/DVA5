package jb.plasma.gtfs;

import com.google.transit.realtime.GtfsRealtime1007Extension;
import jb.dvacommon.DVA;
import jb.plasma.DepartureData;
import jb.plasma.GtfsDepartureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GtfsTimetableTranslator
{
    private final static Logger logger = LoggerFactory.getLogger(GtfsTimetableTranslator.class);
    private final static ZoneId SydneyTimeZone = TimeZone.getTimeZone("Australia/Sydney").toZoneId();
    private final static long RealtimeUpdateIntervalSec = 120;
    private static GtfsTimetableTranslator instance;

    private GtfsTimetable tt;
    private Map<Trip, Map<Stop, GtfsRealtime1007Extension.TripUpdate.StopTimeUpdate>> realtimeTripUpdates;

    private GtfsTimetableTranslator(GtfsTimetable tt)
    {
        this.tt = tt;
        new Timer().schedule(new TimerTask()
        {
            public void run() {refreshRealtimeInfo();}
        }, 0, RealtimeUpdateIntervalSec * 1000);
    }

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

    public Stream<DepartureData> getDepartureDataForStation(
            String stationLocationName,
            String stationPlatformLocationName,
            String routeName,
            int limit)
    {
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

        LocalDate today = LocalDate.now();
        return platforms
            .flatMap(platform -> applyRealtimeInfo(tt.StopTimesByStop.get(platform).stream(), today))
            .filter(st -> st.Pickup)
            .filter(st -> routes == null || routes.contains(st.Trip.Route))
            .flatMap(st -> expandTrips(st, tt))
            .sorted((t1, t2) -> t1.At.compareTo(t2.At))
            .limit(limit > 0 ? limit : Integer.MAX_VALUE)
            .map(ti -> new GtfsDepartureData(ti));
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
                List<NormalizedStopTime> tripStopTimes = applyRealtimeInfo(tt.StopTimesByTrip.get(tripTimeAndPlace.Trip).stream(), dateCopy)
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
                        List<NormalizedStopTime> blockStopTimes = applyRealtimeInfo(tt.StopTimesByTrip.get(blockTrip).stream(), dateCopy)
                                .map(bst -> new NormalizedStopTime(bst, dateCopy))
                                .collect(Collectors.toList());;

                        for (NormalizedStopTime bst : blockStopTimes)
                        {
                            if (ChronoUnit.SECONDS.between(tripLastStopTime.NormalizedDeparture, bst.NormalizedDeparture) <= 1) {
                                /*logger.info("Detected trip {} continuing from trip {} (block id {})",
                                        blockTrip.Name, tripTimeAndPlace.Trip.Name, tripTimeAndPlace.Trip.BlockId);*/
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

    public void refreshRealtimeInfo()
    {
        try
        {
            GtfsRealtime1007Extension.FeedMessage realtimeInfo = GtfsRealtime.get();
            realtimeTripUpdates = realtimeInfo.getEntityList().stream()
                    .map(e -> e.getTripUpdate())
                    .collect(Collectors.toMap(
                            tu -> tt.Trips.get(tu.getTrip().getTripId()),
                            tu -> tu.getStopTimeUpdateList().stream()
                                .collect(Collectors.toMap(stu -> tt.Stops.get(stu.getStopId()), stu -> stu))));

            try (PrintWriter out = new PrintWriter(new File(DVA.getApplicationDataFolder(), "gtfsrealtime.txt"))) {
                out.println(realtimeInfo.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Stream<StopTime> applyRealtimeInfo(Stream<StopTime> stopTimes, LocalDate date)
    {
        if (realtimeTripUpdates == null)
        {
            return stopTimes;
        }

        return stopTimes.map(st ->
        {
            if (realtimeTripUpdates.containsKey(st.Trip))
            {
                Map<Stop, GtfsRealtime1007Extension.TripUpdate.StopTimeUpdate> tripUpdate = realtimeTripUpdates.get(st.Trip);

                // Time update at same stop
                if (tripUpdate.containsKey(st.Stop))
                {
                    GtfsRealtime1007Extension.TripUpdate.StopTimeUpdate stopTimeUpdate = tripUpdate.get(st.Stop);

                    logger.debug("Applying realtime update to {} at {}:", st.Trip.Name, st.Stop.Id);
                    String newArrival = null;
                    String newDeparture = null;
                    if (stopTimeUpdate.hasArrival()) {
                        newArrival = timestampToTimeString(new NormalizedStopTime(st, date).NormalizedDeparture, stopTimeUpdate.getArrival(), date);
                        if (newArrival != null) {
                            logger.debug("  Arrival time was {}, now {}", st.Arrival, newArrival);
                        }
                    }
                    if (stopTimeUpdate.hasDeparture()) {
                        newDeparture = timestampToTimeString(new NormalizedStopTime(st, date).NormalizedDeparture, stopTimeUpdate.getDeparture(), date);
                        if (newDeparture != null) {
                            logger.debug("  Departure time was {}, now {}", st.Departure, newDeparture);
                        }
                    }

                    return new StopTime(
                            st.Trip,
                            newArrival != null ? newArrival : st.Arrival,
                            newDeparture != null ? newDeparture : st.Departure,
                            st.Stop,
                            st.Pickup,
                            st.Dropoff
                    );
                }
                else
                {
                    // Not stopping at the location anymore
                    if (st.Dropoff || st.Pickup)
                    {
                        logger.debug("Applying realtime update to {}: no longer stopping at {}",
                                st.Trip.Name,
                                st.Stop.Name);
                    }
                    return null;
                }
            }
            else
            {
                return st;
            }
        }).filter(st -> st != null);
    }

    private String timestampToTimeString(LocalDateTime originalNormalized,
                                         GtfsRealtime1007Extension.TripUpdate.StopTimeEvent ste,
                                         LocalDate date)
    {
        LocalDateTime t = null;
        if (ste.hasTime() && ste.getTime() > 0)
        {
            t = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000L * ste.getTime()), SydneyTimeZone);
        }
        else if (ste.hasDelay() && ste.getDelay() > 0)
        {
            t = originalNormalized.plusSeconds(ste.getDelay());
        }
        else
        {
            return null;
        }

        LocalDateTime midnight = date.plusDays(1).atStartOfDay();
        String formatted = DateTimeFormatter.ofPattern("HH:mm:ss").format(t);
        if (t.isAfter(midnight))
        {
            return Integer.toString(Integer.parseInt(formatted.split(":")[0]) + 24) + formatted.substring(2);
        }
        else
        {
            return formatted;
        }
    }
}
