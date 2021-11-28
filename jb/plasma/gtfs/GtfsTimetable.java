package jb.plasma.gtfs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GtfsTimetable implements Serializable
{
    private static final long serialVersionUID = 1L;

    public GtfsTimetable(Map<String, Route> routes,
                         Map<String, ServicePeriod> calendar,
                         Map<String, Stop> stops,
                         List<StopTime> stopTimes,
                         Map<String, Trip> trips)
    {
        Routes = routes;
        Calendar = calendar;
        Stops = stops;
        StopsByName = stops.values().stream().collect(Collectors.toMap(s -> s.Name, s -> s));
        StopTimesByStop = stopTimes.stream().collect(Collectors.groupingBy(st -> st.Stop));
        StopTimesByTrip = stopTimes.stream().collect(Collectors.groupingBy(st -> st.Trip));
        Trips = trips;
        TripsByBlockId = trips.values().stream().collect(Collectors.groupingBy(t -> t.BlockId));
    }

    public Map<String, Route> Routes;
    public Map<String, ServicePeriod> Calendar;
    public Map<String, Stop> Stops;
    public Map<String, Stop> StopsByName;
    public Map<Stop, List<StopTime>> StopTimesByStop;
    public Map<Trip, List<StopTime>> StopTimesByTrip;
    public Map<String, Trip> Trips;
    public Map<String, List<Trip>> TripsByBlockId;
}
