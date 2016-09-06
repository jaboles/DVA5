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
import org.javatuples.Pair;
import org.javatuples.Triplet;
import com.innahema.collections.query.queriables.Queryable;

public class CityrailStandard extends Announcer
{
    protected boolean isMale;
    protected boolean coalesceStationSequences;
    protected Collection<String[]> substitutions;
    protected Collection<Triplet<String,String,String>> vias;
    protected Collection<List<String>> allStationsTos;

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
        loadSubstitutions();
        loadVias();
        loadAllStationsTos();
        
        String[] stops = d.Stops.clone();
        doSubstitutions(stops);
        String via = getVia(d);
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
                    ast = getAllStationsTo(stops, i);
                
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

    protected void doSubstitutions(String[] stops)
    {
        for (int i = 0; i < stops.length; i++)
        {
            for (String[] s : substitutions)
            {
                if (s.length >= 2)
                {
                    boolean substitute = s.length == 2 || StringUtilities.containsIgnoreCase(this.getSoundLibrary().getName(), s[2]);
                    if (substitute && stops[i].equalsIgnoreCase(s[0]))
                        stops[i] = s[1];
                }                
            }
        }
    }
    
    protected String getVia(DepartureData d)
    {
        if (d.Destination2.toLowerCase().trim().startsWith("via "))
        {
            return d.Destination2; 
        }
        else
        {
            for (Triplet<String,String,String> via : vias)
            {
                List<String> stops = Queryable.from(d.Stops).map((Converter<String, String>) String::toLowerCase).toList();
                int fromIndex = via.getValue0() != null && via.getValue0().length() > 0 ? stops.indexOf(via.getValue0().toLowerCase()) : 0;
                int viaIndex =  stops.indexOf(via.getValue1().toLowerCase());
                int destIndex = stops.indexOf(via.getValue2().toLowerCase());
                if (viaIndex >= 0 && destIndex >= 0 && fromIndex >= 0 && destIndex > viaIndex)
                {
                    return "via " + via.getValue1();
                }
            }
        }
        
        return null;
    }
    
    protected Pair<String,Integer> getAllStationsTo(String[] stops, int currentIndex)
    {
        String[] stopsLower = Queryable.from(stops).map((Converter<String, String>) String::toLowerCase).toArray();
        for (List<String> allStationsTo : allStationsTos)
        {
            List<String> allStationsToLower = Queryable.from(allStationsTo).map((Converter<String, String>) String::toLowerCase).toList();
            int startIndex = allStationsToLower.indexOf(stopsLower[currentIndex]);
            int lastStationInSequence = currentIndex > 0 ? allStationsToLower.indexOf(stopsLower[currentIndex - 1]) : -1;
            
            if (startIndex >= 0 && lastStationInSequence >= 0)
            {
                int matches = 0;
                for (int i = 0; (startIndex + i) < allStationsTo.size() && (currentIndex + i) < stops.length; i++)
                {
                    Queryable<String> items = Queryable.from(allStationsToLower.get(startIndex + i).split("\\|")).map(String::trim);
                    String currentStop = stopsLower[currentIndex + i];
                    if (items.any(currentStop::equals))
                    {
                        matches++;
                    }
                    else
                    {
                        break;
                    }
                }
                if (matches > 3)
                {
                    return new Pair<>("all stations to " + allStationsTo.get(startIndex + matches - 1), matches);
                }
            }
        }
        
        return null;
    }
    
    protected void loadVias()
    {
        vias = new LinkedList<>();
        for (String line : load("vias.txt"))
        {
            String[] pieces = Queryable.from(line.split(",")).map(String::trim).toArray();
            vias.add(new Triplet<>(pieces[0], pieces[1], pieces[2]));
        }
    }
    
    protected void loadAllStationsTos()
    {
        allStationsTos = new LinkedList<>();
        for (String line : load("allStationsTos.txt"))
        {
            List<String> list = Queryable.from(line.split(",")).map(String::trim).toList();
            List<String> reverseList = new LinkedList<>(list);
            Collections.reverse(reverseList);
            allStationsTos.add(list);
            allStationsTos.add(reverseList);
        }
    }
        
    protected void loadSubstitutions()
    {
        substitutions = new LinkedList<>();
        for (String line : load("substitutions.txt"))
        {
            substitutions.add(Queryable.from(line.split(",")).map(String::trim).toArray());
        }
    }
    
    protected static Collection<String> load(String filename)
    {
        List<String> lines = new LinkedList<>();
        try
        {
            File f = new File(FileUtilities.getJarFolder(CityrailStandard.class), filename);
            lines = Queryable.from(FileUtilities.readAllLines(f))
                .filter(line -> line.trim().length() > 0 && !line.trim().startsWith("#"))
                .toList();
        } catch (IOException e) {
            ExceptionReporter.reportException(e);
        }
        return lines;
    }
}