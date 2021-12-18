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

    public GtfsDepartureData(TripInstance ti)
    {
        String[] headsignParts = ti.Trip.Headsign.split(" via ");
        this.Destination = headsignParts[0];
        this.Destination2 = headsignParts.length >= 2 ? "via " + headsignParts[1] : "";
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

            stops.add(new Stop(stopName, carRangeString, airport));
        }
        this.Stops = stops.toArray(new Stop[0]);
        this.DueOut = ti.At;

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
}
