package jb.plasma.gtfs;

import java.io.Serializable;

public class VehicleCategory implements Serializable
{
    private static final long serialVersionUID = 1L;

    public VehicleCategory(String id, String name)
    {
        Id = id;
        Name = name;
    }

    public String Id;
    public String Name;
}
