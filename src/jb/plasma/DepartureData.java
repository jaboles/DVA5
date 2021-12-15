package jb.plasma;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jb.common.StringUtilities;

// Represents info about a single departure being displayed on the indicator board
public abstract class DepartureData
{
    // Default list of service types shown in the combo box
    public static final String[] DefaultServiceTypes = new String[] { "All Stops", "Limited Stops", "Express" };
    // Format string for editing departure time
    private static final DateTimeFormatter DateEditFormat = DateTimeFormatter.ofPattern("HH:mm");

    public String Destination = "";  // Destination e.g. "Central"
    public String Destination2 = ""; // Optional secondary destination e.g. "via City Circle"
    public String Line = ""; // Line name (shown on CityRail V3 indicators)
    public String Type = ""; // Service type
    public int Cars = 0;     // No. of cars (shown on platform indicators)
    public int Platform = 0; // Platform no. (shown on concourse indicators)
    public LocalDateTime DueOut = null;  // Departure time
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

    // Gets the due-out time as a string
    public String dueOutAsString()
    {
        return DueOut.format(DateEditFormat);
    }

    public abstract void logDetails();
}