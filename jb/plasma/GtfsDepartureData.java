package jb.plasma;

import jb.plasma.gtfs.TripInstance;

public class GtfsDepartureData extends DepartureData
{
    public GtfsDepartureData(TripInstance ti)
    {
        String[] headsignParts = ti.Trip.Headsign.split(" via ");
        this.Destination = headsignParts[0];
        this.Destination2 = headsignParts.length >= 2 ? "via " + headsignParts[1] : "";
        this.Line = ti.Trip.Route.Description;
        this.Type = null; // TODO
        this.Cars = ti.Trip.Cars;
        this.Platform = Integer.parseInt(ti.Platform.Name.split(" Station Platform ")[1]);
        this.Stops = ti.getRemainingStopList();
        this.DueOut = ti.At;
    }
}
