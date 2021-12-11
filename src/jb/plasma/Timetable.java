package jb.plasma;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Master timetable object. Contains a list of TimetableLine objects
public class Timetable implements Serializable
{
    private static final long serialVersionUID = 7526471155622776150L;

    public static final int TIMETABLE_WEEKDAY = 1;
    public static final int TIMETABLE_WEEKEND = 2;

    public Map<String, TimetableLine> lines;
    public List<String> lineNames;
    public int type;
    private String name;

    public Timetable(String name, int type)
    {
        this.name = name;
        this.type = type;
        lines = new HashMap<>();
        lineNames = new LinkedList<>();
    }

    public void addLine(String name, TimetableLine line)
    {
        lines.put(name, line);
        lineNames.add(name);
    }

    public String getName() { return name; }

    public int getLineCount()
    {
        return lineNames.size();
    }

    public int getDirectionCount()
    {
        return lines.values().stream()
                .map(TimetableLine::getDirectionCount)
                .reduce(0, Integer::sum);
    }

    public int getTrainCount()
    {
        return lines.values().stream()
                .map(TimetableLine::getTrainCount)
                .reduce(0, Integer::sum);
    }

    public String toString() { return getName(); }

    public int hashCode()
    {
        int hc = 0;
        for (TimetableLine tl : lines.values())
        {
            hc = 2 * hc + tl.hashCode();
        }
        hc += serialVersionUID;
        return hc;
    }
}