package jb.plasma;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.queriables.Queryable;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.StringUtilities;
import jb.plasma.announcers.CityrailStandard;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Phraser
{
    private Collection<String[]> substitutions;
    private Collection<Triplet<String,String,String>> vias;
    private Collection<List<String>> allStationsTos;

    public Phraser()
    {
        loadVias();
        loadSubstitutions();
        loadAllStationsTos();
    }

    public void doSubstitutions(String[] stops, String soundLibraryName)
    {
        for (int i = 0; i < stops.length; i++)
        {
            for (String[] s : substitutions)
            {
                if (s.length >= 2)
                {
                    boolean substitute = s.length == 2 || StringUtilities.containsIgnoreCase(soundLibraryName, s[2]);
                    if (substitute && stops[i].equalsIgnoreCase(s[0]))
                        stops[i] = s[1];
                }
            }
        }
    }

    public String getVia(DepartureData d)
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

    public Pair<String,Integer> getAllStationsTo(String[] stops, int currentIndex)
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
