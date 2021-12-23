package jb.plasma.gtfs;

import org.javatuples.Pair;

import java.io.Serializable;
import java.util.Map;

public class Stop implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Stop(String id, String name, Stop parent)
    {
        Id = id;
        Name = name;
        Parent = parent;
    }

    public String toString() {return Name;}

    public final String Id;
    public final String Name;
    public final Stop Parent;
    public Map<VehicleCategory, Pair<Integer, Integer>> VehicleBoardings;
}