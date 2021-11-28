package jb.plasma.gtfs;

import jb.plasma.DepartureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GtfsTimetableTranslator
{
    final static Logger logger = LoggerFactory.getLogger(GtfsTimetableTranslator.class);
    private GtfsTimetable tt;
    private static GtfsTimetableTranslator instance;

    private GtfsTimetableTranslator(GtfsTimetable tt) { this.tt = tt; }

    public static GtfsTimetableTranslator getInstance()
    {
        return instance;
    }

    public static void initialize(GtfsTimetable tt) {instance = new GtfsTimetableTranslator(tt);}

    public String[] getStations()
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent == null)
                .map(s -> s.Name)
                .sorted()
                .toArray(String[]::new);
    }

    public String[] getPlatformsForStation(String stationName)
    {
        return tt.Stops.values().stream()
                .filter(s -> s.Parent != null && s.Parent.Name.equals(stationName))
                .map(s -> s.Name)
                .sorted()
                .toArray(String[]::new);
    }

    public List<DepartureData> getDepartureDataForStation(int limit)
    {
        List<DepartureData> dd = new LinkedList<DepartureData>();

        Stop here = tt.Stops.get("207261");
        LocalDateTime now = LocalDateTime.now();
        List<StopTime> stopsHere = tt.StopTimesByStop.get(here);
        List<TripInstance> nexts = stopsHere.stream()
            .flatMap(st -> expandTrips(st.Trip, tt))
            .filter(t -> getTimeTripStopsAt(t, here).isAfter(now))
            .sorted((t1, t2) -> getTimeTripStopsAt(t1, here).compareTo(getTimeTripStopsAt(t2, here)))
                .limit(limit > 0 ? limit : Integer.MAX_VALUE)
                .collect(Collectors.toList());

        logger.info("Next {} trips for {}:", limit > 0 ? Integer.toString(limit) : "(all)", here.Name);
        for (TripInstance ti : nexts) {
            logger.info("Trip {} to {} here at {}", ti.Trip.Id, ti.Trip.Headsign, getTimeTripStopsAt(ti, here));

            DepartureData d = new DepartureData();
            String[] headsignParts = ti.Trip.Headsign.split(" via ");
            d.Destination = headsignParts[0];
            d.Destination2 = headsignParts.length >= 2 ? headsignParts[1] : "";
            d.Line = ti.Trip.Route.Description;
            d.Stops = getStopsAfter(ti, here);
            d.Platform = Integer.parseInt(here.Name.split(" Station Platform ")[1]);
            d.Cars = Integer.parseInt(ti.Trip.Id.split("\\.")[5]);
            dd.add(d);
        }

        return dd;
    }

    private Stream<TripInstance> expandTrips(Trip t, GtfsTimetable tt)
    {
        LinkedList<TripInstance> list = new LinkedList<TripInstance>();

        if (t.Route.Id.equals("RTTA_REV") || t.Route.Id.equals("RTTA_DEF")) return Stream.empty();
        if (t.ServicePeriod == null) {
            System.out.println("Null service period for " + t.Id);
        }

        for (LocalDate date = t.ServicePeriod.StartDate; !date.isAfter(t.ServicePeriod.EndDate); date = date.plusDays(1))
        {
            if ((date.getDayOfWeek() == DayOfWeek.MONDAY && t.ServicePeriod.Monday) ||
                    (date.getDayOfWeek() == DayOfWeek.TUESDAY && t.ServicePeriod.Tuesday) ||
                    (date.getDayOfWeek() == DayOfWeek.WEDNESDAY && t.ServicePeriod.Wednesday) ||
                    (date.getDayOfWeek() == DayOfWeek.THURSDAY && t.ServicePeriod.Thursday) ||
                    (date.getDayOfWeek() == DayOfWeek.FRIDAY && t.ServicePeriod.Friday) ||
                    (date.getDayOfWeek() == DayOfWeek.SATURDAY && t.ServicePeriod.Saturday) ||
                    (date.getDayOfWeek() == DayOfWeek.SUNDAY && t.ServicePeriod.Sunday))
            {
                final LocalDate dateCopy = date;
                List<NormalizedStopTime> normalizedStopTimes = tt.StopTimesByTrip.get(t).stream()
                        .map(st -> new NormalizedStopTime(st, dateCopy))
                        .collect(Collectors.toList());

                list.add(new TripInstance(t, date, normalizedStopTimes));
            }
        }
        return list.stream();
    }

    private LocalDateTime getTimeTripStopsAt(TripInstance t, Stop s)
    {
        return t.NormalizedStopTimes.stream().filter(st -> st.Stop == s).findFirst().get().NormalizedDeparture;
    }

    private String[] getStopsAfter(TripInstance trip, Stop stop)
    {
        List<String> stops = new LinkedList<String>();
        boolean found = false;
        for (NormalizedStopTime nst : trip.NormalizedStopTimes)
        {
            if (nst.Stop == stop) found = true;
            else if (found) stops.add(nst.Stop.Name);
        }
        return stops.stream()
                .map(s -> s.split(" Station Platform ")[0])
                .toArray(String[]::new);
    }

    private class TripInstance
    {
        public TripInstance(Trip trip, LocalDate date, List<NormalizedStopTime> stopTimes)
        {
            Trip = trip;
            Date = date;
            NormalizedStopTimes = stopTimes;
        }

        public Trip Trip;
        public LocalDate Date;
        public List<NormalizedStopTime> NormalizedStopTimes;
    }

    private class NormalizedStopTime
    {
        public NormalizedStopTime(StopTime st, LocalDate date)
        {
            Stop = st.Stop;
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

        public Stop Stop;
        public LocalDateTime NormalizedDeparture;
    }
}
