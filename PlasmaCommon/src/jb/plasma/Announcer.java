package jb.plasma;

import jb.dva.Script;
import jb.dva.SoundLibrary;

public abstract class Announcer
{
    private final String name;
    private final SoundLibrary soundLibrary;

    protected Announcer(String name, SoundLibrary soundLibrary)
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

    public SoundLibrary getSoundLibrary()
    {
        return this.soundLibrary;
    }

    public abstract String createAnnouncementText(DepartureData d, int minutesToDeparture);

    public Script createAnnouncement(DepartureData d, int minutesToDeparture)
    {
        return new Script(soundLibrary.getName(), createAnnouncementText(d, minutesToDeparture));
    }
}