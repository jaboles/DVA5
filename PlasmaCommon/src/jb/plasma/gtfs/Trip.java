package jb.plasma.gtfs;

import java.io.Serializable;

public class Trip implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Trip(String id, Route route, ServicePeriod servicePeriod, VehicleCategory vehicleCategory, String headsign, String blockId)
    {
        Id = id;
        Route = route;
        ServicePeriod = servicePeriod;
        VehicleCategory = vehicleCategory;
        Headsign = headsign;
        BlockId = blockId;
        String[] idParts = Id.split("\\.");
        Cars = Integer.parseInt(idParts[5]);
        SetType = idParts[5] + idParts[4];
        Name = idParts[0];
    }

    public final String Id;
    public final Route Route;
    public final ServicePeriod ServicePeriod;
    public final String Headsign;
    public final String BlockId;
    public final int Cars;
    public final String SetType;
    public final String Name;
    public final VehicleCategory VehicleCategory;
}