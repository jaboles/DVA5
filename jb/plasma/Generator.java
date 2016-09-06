package jb.plasma;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jb.common.FileUtilities;
import jb.dvacommon.DVA;
import jb.dvacommon.ProgressAdapter;

// Scrape Cityrail timetable data from sydneytrains.info
public class Generator
{
    PrintStream out;
    PrintStream err;
    String outputFile;
    Calendar date;
    ProgressAdapter pa;
    // Object to hold the data
    Timetable timetable;
    private static DateFormat CityRailDateFormat = new SimpleDateFormat("d/MM/yyyy");
    // Search for line infos on the main timetable page
    private static Pattern LinePattern = Pattern.compile("<option value=\"(\\w{2})\"(?:[^>]*)>(.*?) Line</option>");
    // Search for direction infos from the ajax response
    private static Pattern DirectionPattern = Pattern.compile("\"value\":\"([0-9A-F]+)\",\"label\":\"([^\"]+)\"");
    // Search for station infos on the line page
    private static Pattern StationPattern = Pattern.compile("stationId=[^>]+>(.+?)\\s?<");
    // Get a row of timetable data from the line page
    private static Pattern StationRowPattern = Pattern.compile("<tr class=\"(?:odd|even)\"><td>[^<]+?<[^<]+?hintText\">(?!Proceeds).*?((?:<td>.+?</td>){2,})");
    // Get an individual timetable data cell from a timetable row
    private static Pattern StopPattern = Pattern.compile("<td>(.+?)</td>");

    // Entry point
    public static void main(String[] args)
    {
        System.out.println("TTFetch - timetable fetcher");
        System.out.println("DVA Version " + DVA.VersionString);
        System.out.println(DVA.CopyrightMessage);
        if (args.length != 2)
        {
            System.err.println("Error: improper command line.");
            System.err.println("Usage: ttfetch <output file> <weekday|weekend>");
            System.exit(1);
        }
        int ttType = 0;
        if (args[1].toLowerCase().equals("weekday"))
        {
            ttType = Timetable.TIMETABLE_WEEKDAY;
        }
        else if (args[1].toLowerCase().equals("weekend"))
        {
            ttType = Timetable.TIMETABLE_WEEKEND;
        }
        else
        {
            System.err.println("Error: improper timetable type.");
            System.err.println("Usage: ttfetch <output file> <weekday|weekend>");
            System.exit(1);
        }

        Generator g = new Generator(System.out, System.err, args[0], null, ttType, false, null);
        try {
            g.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Generator(PrintStream out, PrintStream err, String outputFile, String ttName, int ttType, boolean banner, ProgressAdapter pa)
    {
        // Roll forward one day to handle TZ difference
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);

        // Roll calendar to match requested timetable type - weekday or weekend
        while (isWeekend(c) != (ttType == Timetable.TIMETABLE_WEEKEND) || isPublicHoliday(c))
        {
            c.add(Calendar.DATE, 1);
        }

        if (ttName == null)
            ttName = new File(outputFile).getName().replaceFirst("[.][^.]+$", "");
        initialize(new Timetable(ttName, ttType), out, err, outputFile, c, banner, pa);
    }

    private void initialize(Timetable timetable, PrintStream out, PrintStream err, String outputFile, Calendar date, boolean banner, ProgressAdapter pa)
    {
        if (banner)
        {
            out.println("TTFetch - timetable fetcher");
            out.println("DVA Version " + DVA.VersionString);
            out.println(DVA.CopyrightMessage);
        }

        this.timetable = timetable;
        this.out = out;
        this.err = err;
        this.outputFile = outputFile;
        this.date = date;
        this.pa = pa;
    }

    public Timetable run() throws Exception
    {
        out.println("Retrieving timetable for date: " + CityRailDateFormat.format(date.getTime()));

        // Get the main timetable page
        String content = getHTML("http://www.sydneytrains.info/timetables/");

        // For each line:
        Matcher lineMatcher = LinePattern.matcher(content);
        int progress = 0;
        int maxp = 50;
        while (lineMatcher.find())
        {
            String lineId = unescapeHtmlCodes(lineMatcher.group(1));
            String lineName = unescapeHtmlCodes(lineMatcher.group(2));
            out.println("Line: " + lineName);
            if (pa != null) pa.updateProgress(progress++, maxp, lineName, "");
            TimetableLine line = new TimetableLine();

            String lineDirsUrl = "http://www.sydneytrains.info/ajax/index.htm?lineId=" + lineId + "&load=directions";
            String dirContent = getHTML(lineDirsUrl);

            // For each direction:
            Matcher dirMatcher = DirectionPattern.matcher(dirContent);
            while (dirMatcher.find())
            {
                String dirId = dirMatcher.group(1);
                String dirName = dirMatcher.group(2);
                out.println("Direction: " + dirName);

                if (pa != null) pa.updateProgress(progress++, maxp, lineName, dirName);
                TimetableLineSchedule dirData = getDirectionData(lineId, dirId, date);
                if (dirData != null && dirData.getTrainCount() > 0) {
                    line.addDirection(dirName, dirData);
                }
            }

            if (line.directions.size() > 0)
                timetable.addLine(lineName, line);
        }

        // Save to file
        FileOutputStream fos = new FileOutputStream(outputFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(timetable);
        oos.close();
        fos.close();
        return timetable;
    }

    // Gets the timetable data for a single line and direction.
    public TimetableLineSchedule getDirectionData(String lineId, String directionId, Calendar date) throws Exception
    {
        Map<String,String> params = new HashMap<String,String>();
        params.put("selLine", lineId);
        params.put("hiddenDirection", directionId);
        params.put("leaveArrive", "true");
        params.put("selDay", CityRailDateFormat.format(date.getTime()));
        params.put("selHour", "00");
        params.put("selMin", "00");
        params.put("btnTimeTableSearch.x", "54");
        params.put("btnTimeTableSearch.y", "16");
        params.put("frmSearchTimetables_SUBMIT", "1");
        params.put("javax.faces.ViewState", "");

        // Get the list of stations
        String content = postHTML("http://www.sydneytrains.info/timetables/timetables_by_line.htm", params);

        Matcher stationMatcher = StationPattern.matcher(content);
        List<String> stationMatches = new LinkedList<>();
        while (stationMatcher.find())
        {
            String stationName = stationMatcher.group(1);
            stationMatches.add(stationName);
        }

        // Loop over the timetable rows, and each cell within the row
        Matcher stationRowMatcher = StationRowPattern.matcher(content);
        List<String> stationRowMatches = new LinkedList<>();
        List<String[]> allStopMatches = new LinkedList<>();
        while (stationRowMatcher.find())
        {
            String stationRow = stationRowMatcher.group(1);
            stationRowMatches.add(stationRow);

            Matcher stopMatcher = StopPattern.matcher(stationRow);
            List<String> stopMatches = new LinkedList<>();
            if (stopMatcher.find()) { // Skip first cell (empty with station name hint text)
                while (stopMatcher.find()) {
                    String stop = stopMatcher.group(1);
                    stopMatches.add(stop);
                }
            }

            allStopMatches.add(stopMatches.toArray(new String[stopMatches.size()]));
        }

        // Error checking. Make sure the count of stations and rows match, and make sure the table is 'rectangular'
        // i.e. all rows have the same length
        String[] stations = stationMatches.toArray(new String[stationMatches.size()]);
        String[] stationRows = stationMatches.toArray(new String[stationRowMatches.size()]);
        String[][] allStops = allStopMatches.toArray(new String[allStopMatches.size()][]);
        if (stations.length != stationRows.length) throw new Exception("Station row mismatch: " + stations.length + " stations, " + stationRows.length + " station rows");
        if (stations.length != allStops.length) throw new Exception("Station stop count mismatch");
        for (String[] row : allStops)
        {
            if (row.length != allStops[0].length) throw new Exception("Station stop data array is not rectangular");
        }

        // Add the data to the timetable
        TimetableLineSchedule schedule = new TimetableLineSchedule();
        if (allStops.length > 0)
        {
            out.println(Integer.toString(stations.length) + " stations, " + Integer.toString(allStops[0].length) + " trains");
            for (int i = 0; i < stations.length; i++)
            {
                schedule.addStationRow(stations[i], allStops[i]);
            }
        }
        return schedule;
    }

    // Save to file for debugging purposes
    static int contentCounter = 0;
    public void saveToFile(String content) {
        try {
            PrintWriter fout = new PrintWriter("output." + Integer.toString(contentCounter) + ".txt");
            fout.println(content);
            fout.close();
            contentCounter++;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String unescapeHtmlCodes(String s)
    {
        return s.replace("&amp;", "&");
    }

    // Gets the content at the given URL
    public String getHTML(String urlToRead) {
        try {
            //out.println("Read from " + urlToRead);
            return FileUtilities.readFromUrl(new URL(urlToRead));
        } catch (Exception e) {
            e.printStackTrace(err);
        }
        return null;
    }

    public String postHTML(String url, Map<String, String> params) {
        try {
            //out.println("Read from " + url);
            return FileUtilities.postToUrl(new URL(url), params);
        } catch (Exception e) {
            e.printStackTrace(err);
        }
        return null;
    }

    public boolean isWeekend(Calendar c)
    {
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public boolean isPublicHoliday(Calendar c)
    {
        return (c.get(Calendar.DAY_OF_MONTH) == 25 && c.get(Calendar.MONTH) == Calendar.APRIL); // ANZAC day
    }
}