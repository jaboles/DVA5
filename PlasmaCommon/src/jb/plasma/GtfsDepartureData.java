package jb.plasma;

import jb.plasma.gtfs.TripInstance;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class GtfsDepartureData extends DepartureData
{
    private static final Logger Logger = LogManager.getLogger(GtfsDepartureData.class);
    private final TripInstance tripInstance;

    public GtfsDepartureData(TripInstance ti, Phraser phraser)
    {
        String headsign = ti.Trip.Headsign;
        if (headsign.equals("Empty Train")) {
            headsign = ti.RemainingStopList[ti.RemainingStopList.length - 1];
        }

        String[] headsignParts = headsign.split(" via ");
        CityrailLine line = CityrailLine.get(ti.Trip.Route.Description);

        this.Destination = substitute(headsignParts[0], true, line);
        this.Line = ti.Trip.Route.Description;
        this.Type = ti.LimitedStops ? "Limited Stops" : "All Stops";
        this.Cars = ti.Trip.Cars;
        this.Platform = Integer.parseInt(ti.Platform.Name.split(" Station Platform ")[1]);

        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < ti.RemainingStopList.length; i++)
        {
            String stopName = ti.RemainingStopList[i];
            String carRangeString = null;
            Pair<Integer, Integer> carRange = ti.RemainingStopsCarRanges.get(i);
            if (carRange != null && carRange.getValue0().equals(carRange.getValue1())) {
                carRangeString = "Car " + carRange.getValue0();
            } else if (carRange != null && carRange.getValue1() - carRange.getValue0() + 1 < Cars) {
                carRangeString = "Car " + carRange.getValue0() + "-" + carRange.getValue1();
            }
            boolean airport = stopName.equals("International") || stopName.equals("Domestic");

            stops.add(new Stop(substitute(stopName, false, line), carRangeString, airport));
        }

        this.Stops = stops.toArray(new Stop[0]);
        this.DueOut = ti.At;

        if (headsignParts.length >= 2) {
            this.Destination2 = "via " + substitute(headsignParts[1], true, line);
        } else {
            this.Destination2 = phraser.getVia(this);
        }

        this.tripInstance = ti;
    }

    public void logDetails()
    {
        Logger.info("Trip: {} departs from '{}' at {} to '{}' with {} cars. Continues as {}",
                tripInstance.Trip.Name,
                tripInstance.Platform.Name,
                tripInstance.At,
                tripInstance.Trip.Headsign,
                tripInstance.Trip.Cars,
                tripInstance.BlockContinuingTrip != null ? tripInstance.BlockContinuingTrip.Name : "<no continuation>");
    }

    private String substitute(String name, boolean destination, CityrailLine line) {
        if (name.equals("Newcastle Interchange")) {
            return "Newcastle Intg";
        } else if (line != null && line.IsNswTrainlink && name.equals("Central")) {
            if (destination)
                return "Syd Central";
            else
                return "Central (i)";
        } else {
            return name;
        }
    }
}
