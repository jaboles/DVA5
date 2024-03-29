package jb.plasma.gtfs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NormalizedStopTime
{
    public NormalizedStopTime(StopTime st, LocalDate date)
    {
        StopTime = st;
        String[] timeParts = st.Departure.split(":");
        int h = Integer.parseInt(timeParts[0]);
        int m = Integer.parseInt(timeParts[1]);
        int s = Integer.parseInt(timeParts[2]);

        if (h >= 24) {
            date = date.plusDays(1);
            h -= 24;
        }

        NormalizedDeparture = LocalDateTime.of(date, LocalTime.of(h, m, s));
    }

    public final StopTime StopTime;
    public final LocalDateTime NormalizedDeparture;
}