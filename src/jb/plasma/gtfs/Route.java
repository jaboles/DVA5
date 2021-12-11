package jb.plasma.gtfs;

import java.io.Serializable;

public class Route implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Route(String id, String name, String description)
    {
        Id = id;
        Name = name;
        Description = description;
    }

    public String toString() {return Description;}

    public String Id;
    public String Name;
    public String Description;
}
