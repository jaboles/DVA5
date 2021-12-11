package jb.plasma.announcers;

import jb.dva.SoundLibrary;
import jb.plasma.DepartureData;
import jb.plasma.Phraser;

public class NswCountry extends CityrailStandard
{
    public NswCountry(SoundLibrary soundLibrary, boolean isMale)
    {
        super("Sydney Terminal country platforms " + (isMale ? "(Male)" : "(Female)"), soundLibrary, isMale);
    }

    public String createAnnouncementText(DepartureData d, int minutesToDeparture)
    {
        Phraser phraser = new Phraser();
        String[] stops = d.Stops.clone();
        phraser.doSubstitutions(stops, getSoundLibrary().getName());

        StringBuilder s = new StringBuilder(getSoundLibrary().initialSoundName());
        s.append(" The train on platform");
        if (isMale) {
            s.append(" number");
        }
        s.append(" ").append(d.Platform);

        // "... terminates here"
        if (stops.length == 0)
        {
            s.append(" terminates here. Please do not join this train.");
            return s.toString();
        }

        s.append(" is the ");
        s.append(d.DueOut.getHour()).append(" ");
        s.append(d.DueOut.getMinute()).append(" ");
        s.append(d.Destination).append(" stopping at ");

        s.append(stops[0]);
        if (stops.length > 1) {
            if (stops.length > 2) {
                s.append(" then");
                for (int i = 1; i < stops.length - 1; i++) {
                    s.append(" ").append(stops[i]);
                }
            }
            s.append(" then ").append(stops[stops.length - 1]).append(".");
        } else {
            s.append(" only.");
        }
        s.append(" All aboard please.");

        return s.toString();
    }
}