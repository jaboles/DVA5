package jb.plasma.announcers;

import jb.plasma.Announcer;
import jb.plasma.DepartureData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SydneyBusMuseum extends Announcer
{
    public SydneyBusMuseum() {
        super("Sydney Bus Museum", "Sydney-Female");
    }

    public String createAnnouncementText(DepartureData d, int minutesToDeparture)
    {
        StringBuilder s = new StringBuilder("CHIMES");
        s.append(" The next bus to ");
        s.append(d.Destination);

        int mins = (int)ChronoUnit.MINUTES.between(LocalDateTime.now(), d.DueOut);
        int hours = mins / 60;
        mins = mins % 60;
        if (hours == 0 && mins == 0) {
            s.append(" will soon depart. ");
        } else {
            s.append(" will depart in");
            if (hours > 0) {
                s.append(" ").append(hours).append(" hours");
            }
            if (hours > 0 && mins > 0) {
                s.append(" and");
            }
            if (mins > 1) {
                s.append(" ").append(mins).append(" minutes.");
            } else if (mins == 1) {
                s.append(" ").append(mins).append(" minute.");
            }
        }

        return s.toString();
    }
}
