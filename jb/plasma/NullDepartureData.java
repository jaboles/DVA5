package jb.plasma;

public class NullDepartureData extends DepartureData {

    public NullDepartureData()
    {
        this.Destination = "";
        this.Destination2 = "";
        this.Line = "";
        this.Type = "";
        this.Cars = 0;
        this.Platform = 0;
        this.DueOut = null;
        this.Stops = new String[0];
        this.Color1Override = null;
        this.Color2Override = null;
        this.TextColorOverride = null;
        this.CustomAnnouncementPath = null;
    }

    public void logDetails()
    {
    }
}
