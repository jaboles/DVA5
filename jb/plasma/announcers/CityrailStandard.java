package jb.plasma.announcers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.innahema.collections.query.functions.Converter;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.StringUtilities;
import jb.dva.SoundLibrary;
import jb.plasma.Announcer;
import jb.plasma.DepartureData;
import jb.plasma.Phraser;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import com.innahema.collections.query.queriables.Queryable;

public class CityrailStandard extends Announcer
{
    protected boolean isMale;
    protected boolean coalesceStationSequences;

    public CityrailStandard(SoundLibrary soundLibrary, boolean isMale)
    {
        this("CityRail " + (isMale ? "(Male)" : "(Female)"), soundLibrary, isMale);
    }

    protected CityrailStandard(String name, SoundLibrary soundLibrary, boolean isMale)
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
        String[] stops = d.Stops.clone();
        phraser.doSubstitutions(stops, this.getSoundLibrary().getName());
        String via = phraser.getVia(d);
        Pair<String,Integer> ast = null;
        
        StringBuilder s = new StringBuilder(getSoundLibrary().initialSoundName());
        if (minutesToDeparture > 0)
            s.append(" The next train to arrive on platform");
        else
            s.append(" The train on platform");
        
        if (isMale)
            s.append(" number");

        // "... 4 ..."
        s = s.append(" ").append(d.Platform);

        // "... terminates here"
        if (stops.length == 0)
        {
            s = s.append(" terminates here. Please do not join this train.");
            return s.toString();
        }

        // "... goes to X ..."
        s = s.append(" goes to ").append(d.Destination);
        
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
                if (coalesceStationSequences)
                    ast = phraser.getAllStationsTo(stops, i);
                
                if (i == 1 || addedAllStationsTo || ast != null || (i > 1 && i == stops.length - 1))
                {
                    if (ast != null)
                        i += ast.getValue1() - 1;

                    // In 4 cases, add "then"
                    // "... (First stop A) then B ..."
                    // "... (all stations to B) then C ..."
                    // "... (B) then all stations to C ..."
                    // Before last station, "... X, Y, then Z."

                    if (!isMale && addedAllStationsTo && ast != null && (i + 1) == stops.length)
                        // If last "all stations to" add "and" before "then".
                        // "all stations to B) and (then all stations to C)."
                        s.append(" and");
                    
                    if (addedThen && !addedAllStationsTo && (i + 1) == stops.length)
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

        return s.toString();
    }

}