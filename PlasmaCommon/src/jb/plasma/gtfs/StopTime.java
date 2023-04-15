package jb.plasma.gtfs;

import java.io.Serializable;

public class StopTime implements Serializable
{
    private static final long serialVersionUID = 1L;

    public StopTime(Trip trip, String departure, Stop stop, String headsign, boolean pickup, boolean dropoff)
    {
        Trip = trip;
        Departure = departure;
        Stop = stop;
        Headsign = headsign;
        Pickup = pickup;
        Dropoff = dropoff;
    }

    public final Trip Trip;
    public final String Departure;
    public final Stop Stop;
    public final String Headsign;
    public final boolean Pickup;
    public final boolean Dropoff;
}