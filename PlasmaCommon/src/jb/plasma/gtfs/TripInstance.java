package jb.plasma.gtfs;

import org.javatuples.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

// Represents a potential departure of a trip from a given place which will occur at a given time.
public class TripInstance
{
    public TripInstance(StopTime tripTimeAndPlace,
                        LocalDate date,
                        List<NormalizedStopTime> stopTimes,
                        Trip blockContinuingTrip)
    {
        Trip = tripTimeAndPlace.Trip;
        Platform = tripTimeAndPlace.Stop;
        BlockContinuingTrip = blockContinuingTrip;
        At = new NormalizedStopTime(tripTimeAndPlace, date).NormalizedDeparture;

        List<Stop> stops = new LinkedList<>();
        boolean found = false;
        boolean limitedStops = false;
        for (NormalizedStopTime nst : stopTimes)
        {
            if (nst.StopTime.Stop == Platform) found = true;
            else if (found) {
                if (nst.StopTime.Dropoff) {
                    stops.add(nst.StopTime.Stop);
                }
                // If any stops in the list without dropoff set, consider the service limited stops.
                limitedStops |= !nst.StopTime.Dropoff;
            }
        }
        LimitedStops = limitedStops;
        RemainingStopList = stops.stream()
                .map(s -> s.Name.split(" Station Platform ")[0])
                .toArray(String[]::new);
        RemainingStopsCarRanges = stops.stream()
                .map(s -> {
                    if (s.VehicleBoardings != null && s.VehicleBoardings.containsKey(tripTimeAndPlace.Trip.VehicleCategory)) {
                        return s.VehicleBoardings.get(tripTimeAndPlace.Trip.VehicleCategory);
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public final Trip Trip;
    public final Stop Platform;
    public final String[] RemainingStopList;
    public final List<Pair<Integer, Integer>> RemainingStopsCarRanges;
    public final Trip BlockContinuingTrip;
    public final LocalDateTime At;
    public final boolean LimitedStops;
}