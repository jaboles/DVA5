package jb.plasma;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jb.common.IntPair;

// Contains the schedule data for a particular line/direction.
public class TimetableLineSchedule implements Serializable
{
    private static final long serialVersionUID = 7526471155622776148L;
    private static final Pattern TimePattern = Pattern.compile("(\\d+:\\d+)");

    public List<String> stations;
    public List<String[]> times;
    public List<IntPair[]> convertedTimes;

    public TimetableLineSchedule()
    {
        stations = new LinkedList<>();
        times = new LinkedList<>();
    }

    public void addStationRow(String stationName, String[] row)
    {
        stations.add(stationName);
        times.add(row);
    }

    // Converts the timetable data (stored as string objects) into usable times (Calendar objects)
    public void convertTimes()
    {
        if (convertedTimes == null)
        {
            convertedTimes = new LinkedList<>();
            for (String[] row : times)
            {
                IntPair[] t = new IntPair[row.length];

                for (int j = 0; j < row.length; j++)
                {
                    Matcher match = TimePattern.matcher(row[j]);
                    if (match.find()) {
                        String[] values = match.group(1).split(":");

                        int h = Integer.parseInt(values[0]);
                        int m = Integer.parseInt(values[1]);
                        t[j] = new IntPair(h, m);
                    }
                    else {
                        t[j] = null;
                    }
                }
                convertedTimes.add(t);
            }
        }
    }

    public int getTrainCount()
    {
        return times.size() > 0 ? times.get(0).length : 0;
    }

    public int hashCode()
    {
        int hc = 0;
        for (String[] tl : times)
        {
            hc = 2 * hc + Arrays.deepHashCode(tl);
        }
        hc += (int)serialVersionUID;
        return hc;
    }
}