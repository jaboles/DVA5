package jb.plasma;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Info about CityRail lines
public class CityrailLine
{
    public static Map<String,CityrailLine> allLines = new HashMap<>();
    public static List<String> allLineNames = new LinkedList<>();

    public String Name;
    public Color Color1;
    public Color Color2;
    public Color TextColor;
    public String LogoImageFilename;
    public List<CityrailLine> Continuations;
    public String RealtimeId;

    public static CityrailLine NORTHERN;
    public static CityrailLine NORTH_SHORE;
    public static CityrailLine WESTERN;
    public static CityrailLine AIRPORT_EAST_HILLS;
    public static CityrailLine SOUTH;
    public static CityrailLine INNER_WEST;
    public static CityrailLine BANKSTOWN;
    public static CityrailLine EASTERN_SUBURBS_ILLAWARRA;
    public static CityrailLine CUMBERLAND;
    public static CityrailLine CARLINGFORD;
    public static CityrailLine OLYMPIC_PARK;

    public static CityrailLine BLUE_MOUNTAINS;
    public static CityrailLine NEWCASTLE_CENTRAL_COAST;
    public static CityrailLine SOUTH_COAST;
    public static CityrailLine SOUTHERN_HIGHLANDS;
    public static CityrailLine HUNTER;

    // 'Official' Cityrail colours
    public static Color grey = new Color(154, 164, 168);
    public static Color red = new Color(207, 17, 43);
    public static Color yellow = new Color(246, 193, 72);
    public static Color green = new Color(75, 159, 71);
    public static Color brown = new Color(242, 103, 38);
    public static Color blue = new Color(27, 130, 197);
    public static Color pink = new Color(204, 0, 160);
    public static Color darkBlue = new Color(0, 43, 127);
    public static Color darkRed = new Color(129, 47, 51);
    public static Color lightBlue = new Color(121, 213, 242);
    public static Color lightPurple = new Color(140, 127, 179);

    static
    {
        NORTHERN = new CityrailLine("Northern Line", red, Color.white, "T1.png", "nta");
        NORTH_SHORE = new CityrailLine("North Shore Line", yellow, Color.black, "T1.png", "ns");
        WESTERN = new CityrailLine("Western Line", yellow, Color.black, "T1.png", "wt");
        AIRPORT_EAST_HILLS = new CityrailLine("Airport & East Hills Line", green, Color.white, "T2.png", "eh");
        SOUTH = new CityrailLine("South Line", lightBlue, Color.black, "T2.png", "st");
        INNER_WEST = new CityrailLine("Inner West Line", lightPurple, Color.white, "T2.png", "iw");
        BANKSTOWN = new CityrailLine("Bankstown Line", brown, Color.black, "T3.png", "bk");
        EASTERN_SUBURBS_ILLAWARRA = new CityrailLine("Eastern Suburbs & Illawarra Line", blue, Color.white, "T4.png", "il");
        CUMBERLAND = new CityrailLine("Cumberland Line", pink, Color.white, "T5.png", null);
        CARLINGFORD = new CityrailLine("Carlingford Line", darkBlue, Color.white, "T6.png", null);
        OLYMPIC_PARK = new CityrailLine("Olympic Park Line", grey, Color.white, "T7.png", null);
        BLUE_MOUNTAINS = new CityrailLine("Blue Mountains Line", grey, yellow, Color.white, null, "bm");
        NEWCASTLE_CENTRAL_COAST = new CityrailLine("Newcastle & Central Coast Line", grey, red, Color.white, null, "nc");
        SOUTH_COAST = new CityrailLine("South Coast Line", grey, blue, Color.white, null, "sc");
        SOUTHERN_HIGHLANDS = new CityrailLine("Southern Highlands Line", grey, green, Color.white, null, null);
        HUNTER = new CityrailLine("Hunter Line", grey, darkRed, Color.white, null, null);

        CityrailLine[] lines = new CityrailLine[] {
                NORTHERN, NORTH_SHORE, WESTERN, AIRPORT_EAST_HILLS, SOUTH, INNER_WEST, BANKSTOWN, EASTERN_SUBURBS_ILLAWARRA,
                CUMBERLAND, CARLINGFORD, OLYMPIC_PARK, BLUE_MOUNTAINS, NEWCASTLE_CENTRAL_COAST, SOUTH_COAST, SOUTHERN_HIGHLANDS, HUNTER
        };
        for (CityrailLine line : lines) {
            allLines.put(line.Name, line);
            allLineNames.add(line.Name);
        }
    }

    public CityrailLine(String name, Color color1, Color color2, Color textColor, String logoImageFilename, String realtimeId)
    {
        this.Name = name;
        this.Color1 = color1;
        this.Color2 = color2;
        this.TextColor = textColor;
        this.LogoImageFilename = logoImageFilename;
        this.RealtimeId = realtimeId;
    }

    public CityrailLine(String name, Color color, Color textColor, String logoImageFilename, String realtimeId)
    {
        this.Name = name;
        this.Color1 = color;
        this.Color2 = color;
        this.TextColor = textColor;
        this.LogoImageFilename = logoImageFilename;
        this.RealtimeId = realtimeId;
    }

    public void addContinuation(CityrailLine line)
    {
        this.Continuations.add(line);
    }

    public static CityrailLine get(String name)
    {
        return allLines.get(name);
    }

    // Given a line name (from the Cityrail timetable) and stop list try and guess the real line name
    public static CityrailLine find(String timetableLineName, List<String> stops)
    {
        switch (timetableLineName) {
            case "T1 North Shore & Northern":
                // If stops at Epping, is Northern line
                return stops.contains("Epping") ? CityrailLine.NORTHERN : CityrailLine.NORTH_SHORE;
            case "T1 Northern":
                return CityrailLine.NORTHERN;
            case "T1 Western":
                return CityrailLine.WESTERN;
            case "T2 Airport":
                return CityrailLine.AIRPORT_EAST_HILLS;
            case "T2 Inner West & South":
                // If stops at Croydon, assume Inner West, otherwise South
                return stops.contains("Croydon") ? CityrailLine.INNER_WEST : CityrailLine.SOUTH;
            case "T3 Bankstown":
                return CityrailLine.BANKSTOWN;
            case "T4 Eastern Suburbs & Illawarra":
                return CityrailLine.EASTERN_SUBURBS_ILLAWARRA;
            case "T5 Cumberland":
                return CityrailLine.CUMBERLAND;
            case "T6 Carlingford":
                return CityrailLine.CARLINGFORD;
            case "T7 Olympic Park":
                return CityrailLine.OLYMPIC_PARK;
            case "Blue Mountains":
                return CityrailLine.BLUE_MOUNTAINS;
            case "Newcastle & Central Coast":
            case "Central Coast":
            case "Central Coast & Newcastle":
                return CityrailLine.NEWCASTLE_CENTRAL_COAST;
            case "South Coast":
                return CityrailLine.SOUTH_COAST;
            case "Southern Highlands":
                return CityrailLine.SOUTHERN_HIGHLANDS;
            case "Hunter":
                return CityrailLine.HUNTER;
            default:
                return null;
        }
    }
}