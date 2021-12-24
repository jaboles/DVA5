package jb.plasma;

public abstract class Announcer
{
    private final String name;
    private final String soundLibrary;

    protected Announcer(String name, String soundLibrary)
    {
        this.name = name;
        this.soundLibrary = soundLibrary;
    }

    public String getName()
    {
        return this.name;
    }

    public String toString()
    {
        return getName();
    }

    public String getSoundLibrary()
    {
        return this.soundLibrary;
    }

    public abstract String createAnnouncementText(DepartureData d, int minutesToDeparture);
}