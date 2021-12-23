package jb.plasma.gtfs;

import com.google.transit.realtime.GtfsRealtime1007Extension;
import jb.plasma.DepartureData;
import jb.plasma.GtfsDepartureData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.io.File;
import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GtfsTimetableTranslator
{
    private final static Logger logger = LogManager.getLogger(GtfsTimetableTranslator.class);
    private final static ZoneId SydneyTimeZone = TimeZone.getTimeZone("Australia/Sydney").toZoneId();
    private final static long RealtimeUpdateIntervalSec = 120;
    private static GtfsTimetableTranslator instance;
    private final GtfsTimetable tt;
    private final File temp;
    private Map<Trip, Map<Stop, GtfsRealtime1007Extension.TripUpdate.StopTimeUpdate>> realtimeTripUpdates;

    private GtfsTimetableTranslator(GtfsTimetable tt, File temp)
    {
        this.tt = tt;
        this.temp = temp;
        new Timer().schedule(new TimerTask()
        {
            public void run() {refreshRealtimeInfo();}
        }, 0, RealtimeUpdateIntervalSec * 1000);
    }

    public static GtfsTimetableTranslator getInstance()
    {
        return instance;
    }

    public static void initialize(GtfsTimetable tt, File temp) {instance = new GtfsTimetableTranslator(tt, temp);}

    public LocalDateTime downloadTimestamp() {return tt.DownloadTimestamp;}
    public LocalDateTime expiryTime() {return tt.ExpiryTime;}

    public Stop[] getStations()
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent == null)
                .sorted(Comparator.comparing(s -> s.Name))
                .toArray(Stop[]::new);
    }

    public Stop[] getPlatformsForStation(Stop station)
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent == station)
                .sorted(Comparator.comparing(s -> s.Name))
                .toArray(Stop[]::new);
    }

    public String[] getRoutesForStation(Stop station)
    {
        return tt.RoutesByStation.get(station).stream()
                .map(r -> r.Description)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    public Stream<DepartureData> getDepartureDataForStation(
            Stop station,
            Stop platform,
            String routeName,
            int limit,
            Consumer<Pair<Double, String>> progressDelegate)
    {
        Set<Stop> platforms;
        if (platform != null)
        {
            platforms = Arrays.stream(new Stop[] {platform}).collect(Collectors.toSet());
        }
        else
        {
            platforms = tt.Stops.values().stream()
                    .filter(s -> s.Parent == station)
                    .collect(Collectors.toSet());
        }

        final Set<Route> routes = routeName != null
                ? tt.Routes.values().stream().filter(r -> r.Description.equals(routeName))
                    .collect(Collectors.toSet())
                : null;

        progressDelegate.accept(new Pair<>(0.0, "Filtering timepoint data to location " + station.Name));
        Set<StopTime> stopsAtSelectedLocation = tt.StopTimesReader.get()
                .filter(st -> platforms.contains(st.Stop))
                .collect(Collectors.toSet());
        progressDelegate.accept(new Pair<>(0.25, "Collecting timepoint data into trips"));
        Set<Trip> tripsStoppingAtSelectedLocation = stopsAtSelectedLocation.stream()
                .map(st -> st.Trip)
                .collect(Collectors.toSet());
        progressDelegate.accept(new Pair<>(0.5, "Calculating continuing trips"));
        Set<Trip> blockTripsStoppingAtSelectedLocation = tripsStoppingAtSelectedLocation.stream()
                .flatMap(t -> getLaterTripsInSameBlock(t).stream())
                .collect(Collectors.toSet());
        progressDelegate.accept(new Pair<>(0.75, "Filtering timepoint data by trips"));
        Map<Trip, List<StopTime>> stopTimesByTrip = tt.StopTimesReader.get()
                .filter(st -> tripsStoppingAtSelectedLocation.contains(st.Trip) || blockTripsStoppingAtSelectedLocation.contains(st.Trip))
                .collect(Collectors.groupingBy(st -> st.Trip));
        progressDelegate.accept(new Pair<>(1.0, ""));
        System.gc();

        LocalDate today = LocalDate.now();
        return applyRealtimeInfo(stopsAtSelectedLocation.stream(), today)
            .filter(st -> st.Pickup)
            .filter(st -> routes == null || routes.contains(st.Trip.Route))
            .flatMap(st -> expandTrips(st, stopTimesByTrip))
            .sorted(Comparator.comparing(t -> t.At))
            .limit(limit > 0 ? limit : Integer.MAX_VALUE)
            .map(GtfsDepartureData::new);
    }

    private Stream<TripInstance> expandTrips(StopTime tripTimeAndPlace, Map<Trip, List<StopTime>> stopTimesByTrip)
    {
        LinkedList<TripInstance> list = new LinkedList<>();
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
                List<NormalizedStopTime> tripStopTimes = applyRealtimeInfo(stopTimesByTrip.get(tripTimeAndPlace.Trip).stream(), dateCopy)
                        .map(tst -> new NormalizedStopTime(tst, dateCopy))
                        .collect(Collectors.toList());

                if (tripStopTimes.size() == 0)
                {
                    continue;
                }

                Trip blockContinuingTrip = null;
                if (tripTimeAndPlace.Trip.BlockId != null && tripTimeAndPlace.Trip.BlockId.length() > 0)
                {
                    tripStopTimes = new LinkedList<>(tripStopTimes);
                    NormalizedStopTime tripLastStopTime = tripStopTimes.get(tripStopTimes.size() - 1);

                    for (Trip blockTrip : getLaterTripsInSameBlock(tripTimeAndPlace.Trip))
                    {
                        List<NormalizedStopTime> blockStopTimes = applyRealtimeInfo(stopTimesByTrip.get(blockTrip).stream(), dateCopy)
                                .map(bst -> new NormalizedStopTime(bst, dateCopy))
                                .collect(Collectors.toList());

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

    public Set<Trip> getLaterTripsInSameBlock(Trip trip)
    {
        if (trip.BlockId == null || trip.BlockId.length() == 0) return Collections.emptySet();

        return tt.TripsByBlockId.get(trip.BlockId).stream()
            // Only look at up to 2 following trips later in the same block
            .filter(bt -> bt != trip)
            .filter(bt -> bt.Name.compareTo(trip.Name) > 0)
            .sorted(Comparator.comparing(bt -> bt.Name))
            .limit(2)
            .collect(Collectors.toSet());
    }

    public void refreshRealtimeInfo()
    {
        try
        {
            GtfsRealtime1007Extension.FeedMessage realtimeInfo = GtfsRealtime.get();
            realtimeTripUpdates = realtimeInfo.getEntityList().stream()
                    .map(GtfsRealtime1007Extension.FeedEntity::getTripUpdate)
                    .collect(Collectors.toMap(
                            tu -> tt.Trips.get(tu.getTrip().getTripId()),
                            tu -> tu.getStopTimeUpdateList().stream()
                                .collect(Collectors.toMap(stu -> tt.Stops.get(stu.getStopId()), stu -> stu))));

            try (PrintWriter out = new PrintWriter(new File(temp, "gtfsrealtime.txt"))) {
                out.println(realtimeInfo);
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

                    logger.debug("RT update {} at {} ({}):", st.Trip.Name, st.Stop.Id, st.Stop.Name);
                    String newDeparture = null;
                    if (stopTimeUpdate.hasDeparture()) {
                        newDeparture = timestampToTimeString(new NormalizedStopTime(st, date).NormalizedDeparture, stopTimeUpdate.getDeparture(), date);
                        if (newDeparture != null) {
                            logger.debug("  dep. time was {}, now {}", st.Departure, newDeparture);
                        }
                    }

                    return new StopTime(
                            st.Trip,
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
                        logger.debug("RT update {}: no longer stops at {} ({})",
                                st.Trip.Name,
                                st.Stop.Id,
                                st.Stop.Name);
                    }
                    return null;
                }
            }
            else
            {
                return st;
            }
        }).filter(Objects::nonNull);
    }

    private String timestampToTimeString(LocalDateTime originalNormalized,
                                         GtfsRealtime1007Extension.TripUpdate.StopTimeEvent ste,
                                         LocalDate date)
    {
        LocalDateTime t;
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
            return Integer.parseInt(formatted.split(":")[0]) + 24 + formatted.substring(2);
        }
        else
        {
            return formatted;
        }
    }
}