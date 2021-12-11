package jb.plasma.gtfs;

import java.io.Serializable;

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

    public String Id;
    public String Name;
    public Stop Parent;
}
