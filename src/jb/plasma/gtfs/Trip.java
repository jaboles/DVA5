package jb.plasma.gtfs;

import java.io.Serializable;

public class Trip implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Trip(String id, Route route, ServicePeriod servicePeriod, String headsign, String blockId)
    {
        Id = id;
        Route = route;
        ServicePeriod = servicePeriod;
        Headsign = headsign;
        BlockId = blockId;
        Cars = Integer.parseInt(Id.split("\\.")[5]);
        Name = Id.split("\\.")[0];
    }

    public final String Id;
    public final Route Route;
    public final ServicePeriod ServicePeriod;
    public final String Headsign;
    public final String BlockId;
    public final int Cars;
    public final String Name;
}