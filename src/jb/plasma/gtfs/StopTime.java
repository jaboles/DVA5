package jb.plasma.gtfs;

import java.io.Serializable;

public class StopTime implements Serializable
{
    private static final long serialVersionUID = 1L;

    public StopTime(Trip trip, String departure, Stop stop, boolean pickup, boolean dropoff)
    {
        Trip = trip;
        Departure = departure;
        Stop = stop;
        Pickup = pickup;
        Dropoff = dropoff;
    }

    public final Trip Trip;
    public final String Departure;
    public final Stop Stop;
    public final boolean Pickup;
    public final boolean Dropoff;
}