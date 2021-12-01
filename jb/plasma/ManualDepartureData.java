package jb.plasma;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ManualDepartureData extends DepartureData
{
    public ManualDepartureData(
            String destination,
            String destination2,
            String line,
            String type,
            int cars,
            int platform,
            String stopsString,
            String dueOut,
            Color color1Override,
            Color color2Override,
            Color textColorOverride,
            String customAnnouncementPath
    )
    {
        this(destination,
                destination2,
                line,
                type,
                cars,
                platform,
                color1Override,
                color2Override,
                textColorOverride,
                customAnnouncementPath);

        // Parses the list of stops from a comma-separated string
        this.Stops = Arrays.stream(stopsString.split(","))
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);

        // Parses due out from hh:mm
        try {
            String[] values = dueOut.split(":");

            int h = Integer.parseInt(values[0]);
            int m = Integer.parseInt(values[1]);

            LocalDateTime now = LocalDateTime.now();
            this.DueOut = LocalDateTime.now()
                    .withHour(h)
                    .withMinute(m)
                    .withSecond(40);
            if (DueOut.isBefore(now)) DueOut = DueOut.plusDays(1);
        } catch (NumberFormatException e) {
            this.DueOut = LocalDateTime.now().plusMinutes(5);
        }
    }

    public ManualDepartureData(
            String destination,
            String destination2,
            String line,
            String type,
            int cars,
            int platform,
            String[] stops,
            LocalDateTime dueOut,
            Color color1Override,
            Color color2Override,
            Color textColorOverride,
            String customAnnouncementPath
    )
    {
        this(destination,
                destination2,
                line,
                type,
                cars,
                platform,
                color1Override,
                color2Override,
                textColorOverride,
                customAnnouncementPath);

        this.Stops = stops;
        this.DueOut = dueOut;
    }

    private ManualDepartureData(
            String destination,
            String destination2,
            String line,
            String type,
            int cars,
            int platform,
            Color color1Override,
            Color color2Override,
            Color textColorOverride,
            String customAnnouncementPath
    )
    {
        this.Destination = destination;
        this.Destination2 = destination2;
        this.Line = line;
        this.Type = type;
        this.Cars = cars;
        this.Platform = platform;
        this.Color1Override = color1Override;
        this.Color2Override = color2Override;
        this.TextColorOverride = textColorOverride;
        this.CustomAnnouncementPath = customAnnouncementPath;
    }
}
