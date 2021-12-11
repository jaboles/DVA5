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

    public static CityrailLine T1_NORTH_SHORE;
    public static CityrailLine T1_WESTERN;
    public static CityrailLine T2;
    public static CityrailLine T3;
    public static CityrailLine T4;
    public static CityrailLine T5;
    public static CityrailLine T7;
    public static CityrailLine T8;
    public static CityrailLine T9;

    public static CityrailLine BMT;
    public static CityrailLine CCN;
    public static CityrailLine SCO;
    public static CityrailLine SHL;
    public static CityrailLine HUN;

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
    //public static Color lightBlue = new Color(121, 213, 242);
    //public static Color lightPurple = new Color(140, 127, 179);

    static
    {
        T1_NORTH_SHORE = new CityrailLine("T1 North Shore Line", yellow, Color.black, "T1.png");
        T1_WESTERN = new CityrailLine("T1 Western Line", yellow, Color.black, "T1.png");
        T2 = new CityrailLine("T2 Inner West & Leppington Line", blue, Color.white, "T2.png");
        T3 = new CityrailLine("T3 Bankstown Line", brown, Color.black, "T3.png");
        T4 = new CityrailLine("T4 Eastern Suburbs & Illawarra Line", darkBlue, Color.white, "T4.png");
        T5 = new CityrailLine("T5 Cumberland Line", pink, Color.white, "T5.png");
        T7 = new CityrailLine("T7 Olympic Park Line", grey, Color.white, "T7.png");
        T8 = new CityrailLine("T8 Airport & South Line", green, Color.white, "T2.png");
        T9 = new CityrailLine("T9 Northern Line", red, Color.white, "T1.png");

        BMT = new CityrailLine("Blue Mountains Line", grey, yellow, Color.white, "T.png");
        CCN = new CityrailLine("Central Coast & Newcastle Line", grey, red, Color.white, "T.png");
        SCO = new CityrailLine("South Coast Line", grey, blue, Color.white, "T.png");
        SHL = new CityrailLine("Southern Highlands Line", grey, green, Color.white, "T.png");
        HUN = new CityrailLine("Hunter Line", grey, darkRed, Color.white, "T.png");

        CityrailLine[] lines = new CityrailLine[] {
                T1_NORTH_SHORE, T1_WESTERN, T2, T3, T4, T5, T7, T8, T9,
                BMT, CCN, SCO, SHL, HUN
        };
        for (CityrailLine line : lines) {
            allLines.put(line.Name, line);
            allLineNames.add(line.Name);
        }
    }

    public CityrailLine(String name, Color color1, Color color2, Color textColor, String logoImageFilename)
    {
        this.Name = name;
        this.Color1 = color1;
        this.Color2 = color2;
        this.TextColor = textColor;
        this.LogoImageFilename = logoImageFilename;
    }

    public CityrailLine(String name, Color color, Color textColor, String logoImageFilename)
    {
        this.Name = name;
        this.Color1 = color;
        this.Color2 = color;
        this.TextColor = textColor;
        this.LogoImageFilename = logoImageFilename;
    }

    public static CityrailLine get(String name)
    {
        return allLines.get(name);
    }

    // Given a line name (from the Cityrail timetable) and stop list try and guess the real line name
    public static CityrailLine find(String timetableLineName)
    {
        // Existing demo
        if (timetableLineName.equals("Newcastle & Central Coast Line")) {
            return CCN;
        }

        return allLines.get(timetableLineName);
    }
}