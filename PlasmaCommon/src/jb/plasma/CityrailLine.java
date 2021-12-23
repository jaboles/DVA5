package jb.plasma;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Info about CityRail lines
public class CityrailLine
{
    public static final Map<String,CityrailLine> allLines = new HashMap<>();
    public static final List<String> allLineNames = new LinkedList<>();

    public String Name;
    public Color Color1;
    public Color Color2;
    public Color TextColor;
    public boolean IsNswTrainlink;
    public String LogoImageFilename;

    public static final CityrailLine T1_NORTH_SHORE;
    public static final CityrailLine T1_WESTERN;
    public static final CityrailLine T2;
    public static final CityrailLine T3;
    public static final CityrailLine T4;
    public static final CityrailLine T5;
    public static final CityrailLine T7;
    public static final CityrailLine T8;
    public static final CityrailLine T9;

    public static final CityrailLine BMT;
    public static final CityrailLine CCN;
    public static final CityrailLine SCO;
    public static final CityrailLine SHL;
    public static final CityrailLine HUN;

    public static final CityrailLine NRC;
    public static final CityrailLine NRW;
    public static final CityrailLine STH;
    public static final CityrailLine WST;

    // 'Official' Cityrail colours
    public static final Color grey = new Color(154, 164, 168);
    public static final Color red = new Color(207, 17, 43);
    public static final Color yellow = new Color(246, 193, 72);
    public static final Color green = new Color(75, 159, 71);
    public static final Color brown = new Color(242, 103, 38);
    public static final Color blue = new Color(27, 130, 197);
    public static final Color pink = new Color(204, 0, 160);
    public static final Color darkBlue = new Color(0, 43, 127);
    public static final Color darkRed = new Color(129, 47, 51);
    //public static final Color lightBlue = new Color(121, 213, 242);
    //public static final Color lightPurple = new Color(140, 127, 179);
    public static final Color orange = new Color(229, 109, 44);

    static
    {
        T1_NORTH_SHORE = new CityrailLine("T1 North Shore Line", yellow, Color.black, false, "T1.svg");
        T1_WESTERN = new CityrailLine("T1 Western Line", yellow, Color.black, false, "T1.svg");
        T2 = new CityrailLine("T2 Inner West & Leppington Line", blue, Color.white, false, "T2.svg");
        T3 = new CityrailLine("T3 Bankstown Line", brown, Color.black, false, "T3.svg");
        T4 = new CityrailLine("T4 Eastern Suburbs & Illawarra Line", darkBlue, Color.white, false, "T4.svg");
        T5 = new CityrailLine("T5 Cumberland Line", pink, Color.white, false, "T5.svg");
        T7 = new CityrailLine("T7 Olympic Park Line", grey, Color.white, false, "T7.svg");
        T8 = new CityrailLine("T8 Airport & South Line", green, Color.white, false, "T8.svg");
        T9 = new CityrailLine("T9 Northern Line", red, Color.white, false, "T9.svg");

        BMT = new CityrailLine("Blue Mountains Line", grey, yellow, Color.white, true, "T.svg");
        CCN = new CityrailLine("Central Coast & Newcastle Line", grey, red, Color.white, true, "T.svg");
        SCO = new CityrailLine("South Coast Line", grey, blue, Color.white, true, "T.svg");
        SHL = new CityrailLine("Southern Highlands Line", grey, green, Color.white, true, "T.svg");
        HUN = new CityrailLine("Hunter Line", grey, darkRed, Color.white, true, "T.svg");

        NRC = new CityrailLine("NSW TrainLink North Coast", grey, orange, Color.white, true, "T.svg");
        NRW = new CityrailLine("NSW TrainLink North Western", grey, orange, Color.white, true, "T.svg");
        STH = new CityrailLine("NSW TrainLink Southern", grey, orange, Color.white, true, "T.svg");
        WST = new CityrailLine("NSW TrainLink Western", grey, orange, Color.white, true, "T.svg");

        CityrailLine[] lines = new CityrailLine[] {
                T1_NORTH_SHORE, T1_WESTERN, T2, T3, T4, T5, T7, T8, T9,
                BMT, CCN, SCO, SHL, HUN, NRC, NRW, STH, WST
        };
        for (CityrailLine line : lines) {
            allLines.put(line.Name, line);
            allLineNames.add(line.Name);
        }
    }

    public CityrailLine(String name, Color color1, Color color2, Color textColor, boolean isNswTrainlink, String logoImageFilename)
    {
        this.Name = name;
        this.Color1 = color1;
        this.Color2 = color2;
        this.TextColor = textColor;
        this.IsNswTrainlink = isNswTrainlink;
        this.LogoImageFilename = logoImageFilename;
    }

    public CityrailLine(String name, Color color, Color textColor, boolean isNswTrainlink, String logoImageFilename)
    {
        this.Name = name;
        this.Color1 = color;
        this.Color2 = color;
        this.TextColor = textColor;
        this.IsNswTrainlink = isNswTrainlink;
        this.LogoImageFilename = logoImageFilename;
    }

    public static CityrailLine get(String name)
    {
        CityrailLine line = allLines.get(name);

        if (line == null)
            line = allLines.get(name.replace(" Train Services", ""));

        return line;
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