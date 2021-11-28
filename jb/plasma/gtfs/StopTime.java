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

    public Trip Trip;
    public String Departure;
    public Stop Stop;
    public boolean Pickup;
    public boolean Dropoff;
}
