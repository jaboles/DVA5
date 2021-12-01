package jb.plasma.gtfs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NormalizedStopTime
{
    public NormalizedStopTime(StopTime st, LocalDate date)
    {
        StopTime = st;
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
        String[] timeParts = st.Departure.split(":");
        int h = Integer.parseInt(timeParts[0]);
        int m = Integer.parseInt(timeParts[1]);
        int s = Integer.parseInt(timeParts[2]);

        if (h >= 24) {
            normalizedDate = normalizedDate.plusDays(1);
            h -= 24;
        }

        NormalizedDeparture = LocalDateTime.of(normalizedDate, LocalTime.of(h, m, s));
    }

    public StopTime StopTime;
    public LocalDateTime NormalizedDeparture;
}
