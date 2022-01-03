package jb.plasma;

import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.StringUtilities;
import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phraser
{
    private Collection<String[]> substitutions;
    private Collection<Quartet<String,String,String,String>> vias;
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
            stops[i] = doSubstitution(stops[i], soundLibraryName);
        }
    }

    public String doSubstitution(String stop, String soundLibraryName)
    {
        for (String[] s : substitutions)
        {
            if (s.length >= 2)
            {
                boolean substitute = s.length == 2 || StringUtilities.containsIgnoreCase(soundLibraryName, s[2]);
                if (substitute && stop.equalsIgnoreCase(s[0]))
                    return s[1];
            }
        }

        return stop;
    }

    public String getVia(DepartureData d)
    {
        if (d != null && d.Destination2 != null && d.Destination2.toLowerCase().trim().startsWith("via "))
        {
            return d.Destination2;
        }
        else if (d != null)
        {
            for (Quartet<String,String,String,String> via : vias)
            {
                List<String> stops = Arrays.stream(d.Stops).map(st -> st.Name.toLowerCase()).collect(Collectors.toList());
                int fromIndex = via.getValue0() != null && via.getValue0().length() > 0 ? stops.indexOf(via.getValue0().toLowerCase()) : 0;
                int viaIndex =  stops.indexOf(via.getValue1().toLowerCase());
                int destIndex = stops.indexOf(via.getValue2().toLowerCase());
                if (viaIndex >= 0 && destIndex >= 0 && fromIndex >= 0 && destIndex > viaIndex)
                {
                    if (via.getValue3().length() > 0) {
                        return "via " + via.getValue3();
                    } else {
                        return "via " + via.getValue1();
                    }
                }
            }
        }

        return null;
    }

    public Pair<String,Integer> getAllStationsTo(String[] stops, int currentIndex)
    {
        String[] stopsLower = Arrays.stream(stops).map(String::toLowerCase).toArray(String[]::new);
        for (List<String> allStationsTo : allStationsTos)
        {
            List<String> allStationsToLower = allStationsTo.stream().map(String::toLowerCase).collect(Collectors.toList());
            int startIndex = allStationsToLower.indexOf(stopsLower[currentIndex]);
            int lastStationInSequence = currentIndex > 0 ? allStationsToLower.indexOf(stopsLower[currentIndex - 1]) : -1;

            if (startIndex >= 0 && lastStationInSequence >= 0)
            {
                int matches = 0;
                for (int i = 0; (startIndex + i) < allStationsTo.size() && (currentIndex + i) < stops.length; i++)
                {
                    Stream<String> items = Arrays.stream(allStationsToLower.get(startIndex + i).split("\\|")).map(String::trim);
                    String currentStop = stopsLower[currentIndex + i];
                    if (items.anyMatch(currentStop::equals))
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
            String[] pieces = Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
            vias.add(new Quartet<>(pieces[0], pieces[1], pieces[2], pieces.length > 3 ? pieces[3] : ""));
        }
    }

    protected void loadAllStationsTos()
    {
        allStationsTos = new LinkedList<>();
        for (String line : load("allStationsTos.txt"))
        {
            List<String> list = Arrays.stream(line.split(",")).map(String::trim).collect(Collectors.toList());
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
            substitutions.add(Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new));
        }
    }

    protected static Collection<String> load(String filename)
    {
        List<String> lines = new LinkedList<>();
        try
        {
            File f = new File(FileUtilities.getJarFolder(Phraser.class), filename);
            lines = FileUtilities.readAllLines(f)
                    .stream()
                    .filter(line -> line.trim().length() > 0 && !line.trim().startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ExceptionReporter.reportException(e);
        }
        return lines;
    }
}
