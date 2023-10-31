package jb.plasma.announcers;

import jb.plasma.Announcer;
import jb.plasma.DepartureData;
import jb.plasma.Phraser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.util.Arrays;

public class CityrailStandard extends Announcer
{
    private static final Logger logger = LogManager.getLogger(Phraser.class);
    protected final boolean isMale;
    protected boolean coalesceStationSequences;

    public CityrailStandard(String soundLibrary, boolean isMale)
    {
        this("CityRail " + (isMale ? "(Male)" : "(Female)"), soundLibrary, isMale);
    }

    protected CityrailStandard(String name, String soundLibrary, boolean isMale)
    {
        super(name, soundLibrary);
        this.isMale = isMale;
    }

    public void setCoalesceStationSequences(boolean b)
    {
        this.coalesceStationSequences = b;
    }

    public String createAnnouncementText(DepartureData d, int minutesToDeparture)
    {
        Phraser phraser = new Phraser();
        String[] stops = Arrays.stream(d.Stops).map(st -> st.Name).toArray(String[]::new);
        String destination = phraser.doSubstitution(d.Destination, this.getSoundLibrary());
        phraser.doSubstitutions(stops, this.getSoundLibrary());
        String via = phraser.getVia(d);
        Pair<String,Integer> ast = null;

        StringBuilder s = new StringBuilder(isMale ? "CHIME" : "CHIMES");
        if (minutesToDeparture > 0)
            s.append(" The next train to arrive on platform");
        else
            s.append(" The train on platform");

        if (isMale)
            s.append(" number");

        // "... 4 ..."
        s.append(" ").append(d.Platform);

        // "... terminates here"
        if (stops.length == 0)
        {
            s.append(" terminates here. Please do not join this train.");
            return s.toString();
        }

        // "... goes to X ..."
        s.append(" goes to ").append(destination);

        // "... via Y. ..."
        if (via != null)
            s.append(" ").append(via);

        s.append(".");

        // "First stop A"
        s.append(" First stop ").append(stops[0]);
        if (stops.length >= 2) {
            boolean addedAllStationsTo = false;
            boolean addedThen = false;
            for (int i = 1; i < stops.length; i++)
            {
                logger.debug("Determining phrasing for {}", stops[i]);
                if (coalesceStationSequences)
                    ast = phraser.getAllStationsTo(stops, i - 1);

                if (i == 1 || addedAllStationsTo || ast != null || (i > 1 && i == stops.length - 1))
                {
                    if (ast != null)
                        i += ast.getValue1() - 1;

                    // In 4 cases, add "then"
                    // "... (First stop A) then B ..."
                    // "... (all stations to B) then C ..."
                    // "... (B) then all stations to C ..."
                    // Before last station, "... X, Y, then Z."

                    boolean lastStation = (i + 1) == stops.length;
                    if (ast != null && lastStation) {
                        if (isMale)
                            s.append(" then");
                        else
                            // If last "all stations to" add "and" before "then".
                            // "all stations to B and then all stations to C."
                            s.append(" and then");
                    }
                    else if (addedThen && lastStation)
                        s.append(" and");
                    else
                        s.append(" then");
                    addedThen = true;
                }
                else
                {
                    addedThen = false;
                }

                // If there was a matching sequence of all stations
                if (ast != null)
                {
                    // Add "all stations to C ..."
                    s.append(" ").append(ast.getValue0());
                    addedAllStationsTo = true;
                }
                else
                {
                    // Add individual station name
                    s.append(" ").append(stops[i]);
                    addedAllStationsTo = false;
                }
            }
        } else {
            // One stop. "First stop A, only".
            s.append(", only");
        }

        s.append(".");
        return s.toString();
    }

}