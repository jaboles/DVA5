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

        String s = getSoundLibrary().initialSoundName();
        s = s + " The train on platform";
        if (isMale) {
            s = s + " number";
        }
        s = s + " " + d.Platform;

        // "... terminates here"
        if (stops.length == 0)
        {
            s = s + " terminates here. Please do not join this train.";
            return s.toString();
        }

        s = s + " is the ";
        s = s + Integer.toString(d.DueOut.getHour()) + " ";
        s = s + Integer.toString(d.DueOut.getMinute()) + " ";
        s = s + d.Destination + " stopping at ";

        s = s + stops[0];
        if (stops.length > 1) {
            if (stops.length > 2) {
                s = s + " then";
                for (int i = 1; i < stops.length - 1; i++) {
                    s = s + " " + stops[i];
                }
            }
            s = s + " then " + stops[stops.length - 1] + ".";
        } else {
            s = s + " only.";
        }
        s = s + " All aboard please.";

        return s;
    }
}