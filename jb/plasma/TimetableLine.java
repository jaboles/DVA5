package jb.plasma;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.innahema.collections.query.queriables.Queryable;

// TimetableLine object. Contains a list of TimetableLineSchedule objects, one for each direction on the line.
// e.g. "Western Line" has two directions - Chatswood to Emu Plains/Richmond, and Emu Plains/Richmond to Chatswood
public class TimetableLine implements Serializable
{
    private static final long serialVersionUID = 7526471155622776148L;

    public Map<String, TimetableLineSchedule> directions;
    public List<String> directionNames;

    public TimetableLine()
    {
        directions = new HashMap<>();
        directionNames = new LinkedList<>();
    }

    public void addDirection(String directionName, TimetableLineSchedule schedule)
    {
        directions.put(directionName, schedule);
        directionNames.add(directionName);
    }

    public int getDirectionCount()
    {
        return directionNames.size();
    }

    public int getTrainCount()
    {
        return Queryable.from(directions.values()).where(s -> s != null).sum(TimetableLineSchedule::getTrainCount);
    }

    public int hashCode()
    {
        int hc = 0;
        for (TimetableLineSchedule tl : directions.values())
        {
            hc = 2 * hc + tl.hashCode();
        }
        hc += (int)serialVersionUID;
        return hc;
    }
}