package jb.plasma;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import jb.common.StringUtilities;
import com.innahema.collections.query.queriables.Queryable;

// Represents info about a single departure being displayed on the indicator board
public class DepartureData
{
    // Default list of service types shown in the combo box
    public static String[] DefaultServiceTypes = new String[] { "All Stops", "Limited Stops", "Express" };
    // Format string for editing departure time
    private static SimpleDateFormat DateEditFormat = new SimpleDateFormat("HH:mm");

    public String Destination = "";  // Destination e.g. "Central"
    public String Destination2 = ""; // Optional secondary destination e.g. "via City Circle"
    public String Line = ""; // Line name (shown on CityRail V3 indicators)
    public String Type = ""; // Service type
    public int Cars = 0;     // No. of cars (shown on platform indicators)
    public int Platform = 0; // Platform no. (shown on concourse indicators)
    public Calendar DueOut = null;  // Departure time
    public String[] Stops = new String[] { }; // List of stops
    public Color Color1Override = null;
    public Color Color2Override = null;
    public Color TextColorOverride = null;
    public String CustomAnnouncementPath = null;

    // Gets the list of stops as a comma-separated string
    public String stopsAsString()
    {
        return StringUtilities.join(", ", Stops);
    }

    // Parses the list of stops from a comma-separated string
    public void stopsFromString(String stops)
    {
        List<String> l = new LinkedList<String>();
        for (String s : Queryable.from(stops.split(",")).map(String::trim))
        {
            if (s.length() > 0) l.add(s);
        }
        Stops = l.toArray(new String[0]);
    }

    // Gets the due-out time as a string
    public String dueOutAsString()
    {
        return DateEditFormat.format(DueOut.getTime());
    }

    // Sets the due-out time from a string
    public void dueOutFromString(String s)
    {
        DueOut = parseTime(s);
    }

    // Parse a time string
    private Calendar parseTime(String time)
    {
        String[] values = time.split(":");

        int h = Integer.parseInt(values[0]);
        int m = Integer.parseInt(values[1]);

        Calendar now = Calendar.getInstance();
        Calendar departure = Calendar.getInstance();
        departure.set(Calendar.HOUR_OF_DAY, h);
        departure.set(Calendar.MINUTE, m);
        departure.set(Calendar.SECOND, 40);
        if (departure.compareTo(now) < 0) departure.add(Calendar.DATE, 1);
        return departure;
    }

}