package jb.plasma;

import com.innahema.collections.query.queriables.Queryable;

/**
 * Created by jb on 16/2/17.
 */
public interface IScheduleTranslator {
    String[] getLines();
    String[] getDirectionsForLine(String lineName);
    String[] getStationsForLineAndDirection(String lineName, String directionName);
}
