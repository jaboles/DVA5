package jb.plasma.gtfs;

import java.io.Serializable;

public class Trip implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Trip(String id, Route route, ServicePeriod servicePeriod, String headsign, int directionId, String blockId)
    {
        Id = id;
        Route = route;
        ServicePeriod = servicePeriod;
        Headsign = headsign;
        DirectionId = directionId;
        BlockId = blockId;
    }

    public String Id;
    public Route Route;
    public ServicePeriod ServicePeriod;
    public String Headsign;
    public int DirectionId;
    public String BlockId;
}
