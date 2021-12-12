package jb.plasma.gtfs;

import jb.dvacommon.DVA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GtfsGenerator {
    private final static Logger logger = LogManager.getLogger(GtfsGenerator.class);
    private final Path wd;
    private static GtfsGenerator instance;

    public static void main(String[] args) {
        System.out.println("TTFetch - timetable fetcher");
        System.out.println("DVA Version " + DVA.VersionString);
        System.out.println(DVA.CopyrightMessage);

        GtfsGenerator.initialize(FileSystems.getDefault().getPath("./testing"));
        try {
            GtfsGenerator.getInstance().download();
            GtfsGenerator.getInstance().read().analyse();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GtfsGenerator(Path wd) { this.wd = wd; }

    public static GtfsGenerator getInstance()
    {
        return instance;
    }

    public static void initialize(Path wd) {instance = new GtfsGenerator(wd);}

    public void download() throws IOException
    {
        if (Files.exists(wd))
        {
            logger.info("GTFS data {} exists", wd);
            if (LocalDateTime.now().isAfter(expiryTime()))
            {
                logger.info("GTFS data expired, deleting.");
                delete();
            }
        }

        if (!Files.exists(wd))
        {
            logger.info("GTFS data doesn't exist, downloading new data.");
            GtfsHttpClient.getHTMLZip("https://api.transport.nsw.gov.au/v1/gtfs/schedule/sydneytrains", wd);
        }
    }

    public LocalDateTime downloadTimestamp() throws IOException
    {
        BasicFileAttributes attrs = Files.readAttributes(wd, BasicFileAttributes.class);
        return LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
    }

    public LocalDateTime expiryTime() throws IOException
    {
        return downloadTimestamp().plusDays(14);
    }

    public GtfsTimetable read() throws Exception
    {
        HashMap<String, Stop> stops = GtfsCsvReader.readStops(wd.resolve("stops.txt"));
        HashMap<String, Route> routes = GtfsCsvReader.readRoutes(wd.resolve("routes.txt"));
        HashMap<String, ServicePeriod> servicePeriods = GtfsCsvReader.readServicePeriods(wd.resolve("calendar.txt"));
        HashMap<String, Trip> trips = GtfsCsvReader.readTrips(wd.resolve("trips.txt"), routes, servicePeriods);
        List<StopTime> stopTimes = GtfsCsvReader.readStopTimes(wd.resolve("stop_times.txt"), trips, stops);

        return new GtfsTimetable(routes, servicePeriods, stops, stopTimes, trips, downloadTimestamp(), expiryTime());
    }

    public void delete() throws IOException
    {
        Files.walk(wd)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
