package jb.dvacommon;
import java.awt.Color;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jb.common.RangeFactory;
import jb.common.Utilities;
import jb.dva.Script;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.IndicatorSettings;
import jb.plasma.ui.PlasmaUI;

public class Settings {
    static Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    static Script[] DVA_DEMOS;
    static DepartureData[] PLASMA_DEMOS;
    static {
        DVA_DEMOS = new Script[] {
            new Script("Demo 1", "Sydney-Male", "3 ascending chimes; Attention, defective announcements and young children are causing delays to the CityRail system. We are unable to continue this defective announcement. Please move towards the centre of the tulip festival as we rectify the problem. the CityRail system thanks you for your co-operation. "),
            new Script("Demo 2", "Sydney M-set", "chime This train will stop at The Harbour Bridge. Passengers should alight at The Harbour Bridge for ferry services to Circular Quay"),
            new Script("Demo 3", "Sydney M-set", "CHIME3 This train is delayed by 1 Hour. This train will depart in 1 hour and 2 minutes. CityRail appreciates your co-operation."),
            new Script("Demo A", "AnnouncementRail", "3 ascending chimes Instead of train services, lengthy inconvenience conveying passengers the opposite direction will be replacing trains on the new CityRail line."),
            new Script("Inflection Demo 1", "Sydney-Male", "This train goes to Central only. First stop Central."),
            new Script("Inflection Demo 2", "Sydney-Male", "This train goes to `Central only. First stop ^Central."),
        };
        PLASMA_DEMOS = new DepartureData[] {
            new DepartureData() {{
                Destination = "Newcastle";
                Type = DepartureData.DefaultServiceTypes[2];
                Platform = 1;
                Cars = 8;
                Stops = new String[] { "Redfern", "Strathfield", "Epping", "Hornsby", "Berowra", "Gosford", "Wyong", "Broadmeadow", "Newcastle" };
                DueOut = PlasmaUI.inXMinutes(Calendar.getInstance(), 5);
                Line = "Newcastle & Central Coast Line";
                Color1Override = CityrailLine.grey;
                Color2Override = CityrailLine.red;
                TextColorOverride = Color.white;
            }},
            new DepartureData() {{
                Destination = "Lithgow";
                Type = DepartureData.DefaultServiceTypes[1];
                Platform = 1;
                Cars = 8;
                Stops = new String[] { "Redfern", "Strathfield", "Granville", "Parramatta", "Westmead", "Blacktown",
                        "Penrith", "Emu Plains", "Lapstone", "Glenbrook", "Blaxland", "Warrimoo", "Valley Heights",
                        "Springwood", "Faulconbridge", "Linden", "Hazelbrook", "Lawson", "Bullaburra",
                        "Wentworth Falls", "Leura", "Katoomba", "Medlow Bath", "Blackheath", "Mount Victoria", "Bell",
                        "Zig Zag", "Lithgow" };
                DueOut = PlasmaUI.inXMinutes(Calendar.getInstance(), 9);
                Line = "Blue Mountains Line";
                Color1Override = CityrailLine.grey;
                Color2Override = CityrailLine.yellow;
                TextColorOverride = Color.white;
            }},
            new DepartureData() {{
                Destination = "Nowra";
                Type = DepartureData.DefaultServiceTypes[1];
                Platform = 1;
                Cars = 8;
                Stops = new String[] { "Redfern", "Hurstville", "Sutherland", "Waterfall", "Thirroul",
                        "North Wollongong", "Wollongong", "Coniston", "Unanderra", "Kembla Grange", "Dapto", "Kiama",
                        "Berry", "Nowra" };
                DueOut = PlasmaUI.inXMinutes(Calendar.getInstance(), 15);
                Line = "South Coast Line";
                Color1Override = CityrailLine.grey;
                Color2Override = CityrailLine.blue;
                TextColorOverride = Color.white;
            }}
        };
    }
    
    public static void deleteAll() {
        try {
            prefs.clear();
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    protected static void putColor(String key, Color c)
    {
        String s = c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha();
        prefs.put(key, s);
    }

    protected static Color getColor(String key, Color defaultValue)
    {
        String[] s = prefs.get(key, "").split(",");
        if (s.length == 4)
            return new Color(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]));
        else
            return defaultValue;
    }

    public static boolean isLicenceRead() {
        String licenceReadAtVersion = prefs.get("licenceReadAtVersion", "");
        return !licenceReadAtVersion.isEmpty() && Utilities.compareVersion(DVA.VersionString, licenceReadAtVersion) <= 0;
    }

    public static void setLicenceRead() {
        prefs.put("licenceReadAtVersion", DVA.VersionString);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    public static boolean isSoundJarsDownloaded() {
        String soundJarsDownloadedAtVersion = prefs.get("soundJarsDownloadedAtVersion", "");
        return !soundJarsDownloadedAtVersion.isEmpty() && Utilities.compareVersion(DVA.VersionString, soundJarsDownloadedAtVersion) <= 0;
    }

    public static void setSoundJarsDownloaded() {
        prefs.put("soundJarsDownloadedAtVersion", DVA.VersionString);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    public static List<Script> loadAnnouncements(Collection<String> availableSoundLibraries) {
        List<Script> savedAnnouncements = StreamSupport.stream(RangeFactory.range(1, prefs.getInt("savedAnnouncementsCount", 0)).spliterator(), false)
            .map(i -> loadAnnouncement("ann" + Integer.toString(i - 1)))
                .collect(Collectors.toList());

        for (Script demo : DVA_DEMOS) {
            if (!savedAnnouncements.contains(demo) && !getDemoAdded(demo.getName()) && availableSoundLibraries.contains(demo.getVoice())) {
                savedAnnouncements.add(demo);
                setDemoAdded(demo.getName());
                saveAnnouncements(savedAnnouncements);
            }
        }

        return savedAnnouncements;
    }
    
    public static Script loadAnnouncement(String key) {
        String annName = prefs.get(key + "Name", "");
        String annVoice = prefs.get(key + "Voice", "");
        String annScript = prefs.get(key + "Script", "");
        return new Script(annName, annVoice, annScript); 
    }

    public static void saveAnnouncements(List<Script> anns) {
        prefs.putInt("savedAnnouncementsCount", anns.size());
        Iterator<Script> it = anns.iterator();
        for (int i = 0; it.hasNext(); i++) {
            saveAnnouncement("ann" + Integer.toString(i), it.next());
        }
        setAnyAnnouncementsEverSaved();
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }
    
    public static void saveAnnouncement(String key, Script s) {
        prefs.put(key + "Name", s.getName());
        prefs.put(key + "Voice", s.getVoice());
        prefs.put(key + "Script", s.getScript());
    }

    public static void markUpdateSuppressed(String version) {
        prefs.put("updateSuppressed", version);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    public static String getUpdateSuppressed() {
        return prefs.get("updateSuppressed", "");
    }

    private static void setAnyAnnouncementsEverSaved() {
        prefs.putBoolean("anyAnnouncementsEverSaved", true);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    public static boolean specialSoundsEnabled() {
        return prefs.getBoolean("specialSounds", false);
    }

    public static void setSpecialSoundsEnabled(boolean value) {
        prefs.putBoolean("specialSounds", value);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    private static boolean getDemoAdded(String name) {
        return prefs.getBoolean("demoAdded " + name, false);
    }

    private static void setDemoAdded(String name) {
        prefs.putBoolean("demoAdded " + name, true);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }

    public static IndicatorSettings getIndicator(String key) {

        boolean useSchedule = prefs.getBoolean(key + "UseSchedule", false);
        List<String> renderers = new LinkedList<>();
        for (int i = 0; i < prefs.getInt(key + "RenderersCount", 0); i++)
        {
            renderers.add(prefs.get(key + "Renderer" + Integer.toString(i), ""));
        }
        boolean playAnnouncements = prefs.getBoolean(key + "PlayAnnouncements", true);
        String announcementTimes = prefs.get(key + "AnnouncementTimes", "5,3,1,0");
        String announcementVoice = prefs.get(key + "AnnouncementVoice", "CityRail (Male)");
        boolean coalesceStationSequences = prefs.getBoolean(key + "CoalesceStationSequences", true);

        List<DepartureData> departureData = new LinkedList<>();
        int departureDataCount = prefs.getInt(key + "DepartureDataCount", 0);
        if (departureDataCount == 0)
        {
            departureData.addAll(Arrays.asList(PLASMA_DEMOS));
        }
        else
        {
            for (int i = 0; i < departureDataCount; i++)
            {
                departureData.add(getIndicatorDepartureData(key + "DepartureData" + Integer.toString(i)));
            }
        }

        String line = prefs.get(key + "Line", "");
        String direction = prefs.get(key + "Direction", "");
        String station = prefs.get(key + "Station", "");
        int platform = prefs.getInt(key + "Platform", 1);
        int cars = prefs.getInt(key + "Cars", 4);

        return new IndicatorSettings(useSchedule, renderers, playAnnouncements, announcementTimes, announcementVoice, coalesceStationSequences, departureData, line, direction, station, platform, cars);
    }
    
    public static DepartureData getIndicatorDepartureData(String key) {
        DepartureData dd = new DepartureData();
        dd.Destination = prefs.get(key + "Destination", "");
        dd.Destination2 = prefs.get(key + "Destination2", "");
        dd.Line = prefs.get(key + "Line", "");
        dd.Type = prefs.get(key + "Type", "");
        dd.Cars = prefs.getInt(key + "Cars", 8);
        dd.Platform = prefs.getInt(key + "Platform", 1);
        try {
            dd.dueOutFromString(prefs.get(key + "DueOut", ""));
        } catch (NumberFormatException e) {
            dd.DueOut = Calendar.getInstance();
            dd.DueOut.add(Calendar.MINUTE, 5);
        }
        dd.stopsFromString(prefs.get(key + "Stops", ""));
        dd.Color1Override = getColor(key + "Color1", null);
        dd.Color2Override = getColor(key + "Color2", null);
        dd.TextColorOverride = getColor(key + "TextColor", null);
        dd.CustomAnnouncementPath = prefs.get(key + "CustomAnnouncement", null);
        return dd;
    }

    public static void setIndicator(String key, IndicatorSettings is) {
        prefs.putBoolean(key + "UseSchedule", is.useSchedule());
        prefs.putInt(key + "RenderersCount", is.getRenderers().size());
        for (int i = 0; i < is.getRenderers().size(); i++)
        {
            prefs.put(key + "Renderer" + Integer.toString(i), is.getRenderers().get(i));
        }
        prefs.putBoolean(key + "PlayAnnouncements", is.playAnnouncements());

        if (is.announcementTimes() != null)
            prefs.put(key + "AnnouncementTimes", is.announcementTimes());
        if (is.announcementVoice() != null)
            prefs.put(key + "AnnouncementVoice", is.announcementVoice());
        prefs.putBoolean(key + "CoalesceStationSequences", is.coalesceStationSequences());

        prefs.putInt(key + "DepartureDataCount", is.getDepartureData().size());
        for (int i = 0; i < is.getDepartureData().size(); i++)
        {
            setIndicatorDepartureData(key + "DepartureData" + Integer.toString(i), is.getDepartureData().get(i));
        }

        prefs.put(key + "Line", is.getLine());
        prefs.put(key + "Direction", is.getDirection());
        prefs.put(key + "Station", is.getStation());
        prefs.putInt(key + "Platform", is.getPlatform());
        prefs.putInt(key + "Cars", is.getCars());
        prefs.putBoolean(key + "Indicator", true);
        try {
            prefs.flush();
        } catch (BackingStoreException e) { e.printStackTrace(System.err); }
    }
    
    public static void setIndicatorDepartureData(String k, DepartureData d) {
        prefs.put(k + "Destination", d.Destination);
        prefs.put(k + "Destination2", d.Destination2);
        prefs.put(k + "Line", d.Line);
        prefs.put(k + "Type", d.Type);
        prefs.putInt(k + "Cars", d.Cars);
        prefs.putInt(k + "Platform", d.Platform);
        if (d.DueOut != null)
            prefs.put(k + "DueOut", d.dueOutAsString());
        prefs.put(k + "Stops", d.stopsAsString());
        if (d.Color1Override != null)
            putColor(k + "Color1", d.Color1Override);
        if (d.Color2Override != null)
            putColor(k + "Color2", d.Color2Override);
        if (d.TextColorOverride != null)
            putColor(k + "TextColor", d.TextColorOverride);
        if (d.CustomAnnouncementPath != null)
            prefs.put(k + "CustomAnnouncement", d.CustomAnnouncementPath);
    }
}