package jb.plasma.gtfs;

import org.javatuples.Pair;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GtfsTimetable implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final List<Pair<String, Consumer<GtfsTimetable>>> AnalysisSteps = Arrays.asList(
        new Pair<String, Consumer<GtfsTimetable>>("indexing trips", tt -> tt.StopsByName = tt.Stops.values().stream().collect(Collectors.toMap(s -> s.Name, s -> s))),
        new Pair<String, Consumer<GtfsTimetable>>("indexing routes", tt -> tt.TripsByBlockId = tt.Trips.values().stream().collect(Collectors.groupingBy(t -> t.BlockId))),
        new Pair<String, Consumer<GtfsTimetable>>("cleaning up un-needed data", tt -> tt.RoutesByStation = tt.StopTimesReader.get()
            .filter(st -> st.Pickup || st.Dropoff)
            .collect(Collectors.groupingBy(st -> st.Stop.Parent,
                Collectors.mapping(st -> st.Trip.Route,
                    Collectors.toSet())))),
        new Pair<String, Consumer<GtfsTimetable>>("", tt -> System.gc())
    );

    public GtfsTimetable(Map<String, Route> routes,
                         Map<String, ServicePeriod> calendar,
                         Map<String, Stop> stops,
                         Supplier<Stream<StopTime>> stopTimesReader,
                         Map<String, Trip> trips,
                         LocalDateTime downloadTimestamp,
                         LocalDateTime expiryTime)
    {
        Routes = routes;
        Calendar = calendar;
        Stops = stops;
        Trips = trips;
        StopTimesReader = stopTimesReader;
        DownloadTimestamp = downloadTimestamp;
        ExpiryTime = expiryTime;
    }

    public void analyse() {
        for (int i = 0; i < getAnalysisStepCount(); i++) {analyse(i);}
    }

    public static int getAnalysisStepCount() {return AnalysisSteps.size();}
    public String analyse(int stepIndex)
    {
        Pair<String, Consumer<GtfsTimetable>> step = AnalysisSteps.get(stepIndex);
        step.getValue1().accept(this);
        return step.getValue0();
    }

    public Map<String, Route> Routes;
    public Map<String, ServicePeriod> Calendar;
    public Map<String, Stop> Stops;
    public Map<String, Trip> Trips;
    public Supplier<Stream<StopTime>> StopTimesReader;
    public Map<String, Stop> StopsByName;
    public Map<String, List<Trip>> TripsByBlockId;
    public Map<Stop, Set<Route>> RoutesByStation;

    public LocalDateTime DownloadTimestamp;
    public LocalDateTime ExpiryTime;
}
