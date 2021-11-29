package jb.plasma.gtfs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GtfsTimetable implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static Object[] AnalysisSteps = new Object[]
    {
        (Consumer<GtfsTimetable>)tt -> {tt.StopsByName = tt.Stops.values().stream().collect(Collectors.toMap(s -> s.Name, s -> s));},
        (Consumer<GtfsTimetable>)tt -> {tt.StopTimesByStop = tt.StopTimes.stream().collect(Collectors.groupingBy(st -> st.Stop));},
        (Consumer<GtfsTimetable>)tt -> {tt.StopTimesByTrip = tt.StopTimes.stream().collect(Collectors.groupingBy(st -> st.Trip));},
        (Consumer<GtfsTimetable>)tt -> {tt.TripsByBlockId = tt.Trips.values().stream().collect(Collectors.groupingBy(t -> t.BlockId));},
        (Consumer<GtfsTimetable>)tt -> {tt.RoutesByStation = tt.StopTimes.stream().collect(Collectors.groupingBy(st -> st.Stop.Parent,
            Collectors.mapping(st -> st.Trip.Route,
                Collectors.toSet())));},
    };

    public GtfsTimetable(Map<String, Route> routes,
                         Map<String, ServicePeriod> calendar,
                         Map<String, Stop> stops,
                         List<StopTime> stopTimes,
                         Map<String, Trip> trips)
    {
        Routes = routes;
        Calendar = calendar;
        Stops = stops;
        Trips = trips;
        StopTimes = stopTimes;
    }

    public void analyse() {
        for (int i = 0; i < getAnalysisStepCount(); i++) {analyse(i);}
    }

    public static int getAnalysisStepCount() {return AnalysisSteps.length;}
    public void analyse(int step) {((Consumer<GtfsTimetable>)AnalysisSteps[step]).accept(this);}

    public Map<String, Route> Routes;
    public Map<String, ServicePeriod> Calendar;
    public Map<String, Stop> Stops;
    public Map<String, Trip> Trips;
    public List<StopTime> StopTimes;
    public Map<String, Stop> StopsByName;
    public Map<Stop, List<StopTime>> StopTimesByStop;
    public Map<Trip, List<StopTime>> StopTimesByTrip;
    public Map<String, List<Trip>> TripsByBlockId;
    public Map<Stop, Set<Route>> RoutesByStation;
}
