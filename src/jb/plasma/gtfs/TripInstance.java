package jb.plasma.gtfs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

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

        List<String> stops = new LinkedList<>();
        boolean found = false;
        for (NormalizedStopTime nst : stopTimes)
        {
            if (nst.StopTime.Stop == Platform) found = true;
            else if (found && nst.StopTime.Dropoff) stops.add(nst.StopTime.Stop.Name);
        }
        RemainingStopList = stops.stream()
                .map(s -> s.split(" Station Platform ")[0])
                .toArray(String[]::new);
    }

    public Trip Trip;
    public Stop Platform;
    public String[] RemainingStopList;
    public Trip BlockContinuingTrip;
    public LocalDateTime At;
}
