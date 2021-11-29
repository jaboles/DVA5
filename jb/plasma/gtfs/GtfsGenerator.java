package jb.plasma.gtfs;

import jb.common.FileUtilities;
import jb.dvacommon.DVA;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GtfsGenerator {
    private final String APIKEY = "3LP1iesTsBqiO2rzMGdmPJ3EJV1ubm3FqyP0";
    private Path wd;
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
            if (LocalDateTime.now().isAfter(expiryTime()))
            {
                delete();
            }
        }

        if (!Files.exists(wd))
        {
            getHTMLZip("https://api.transport.nsw.gov.au/v1/gtfs/schedule/sydneytrains", APIKEY, wd);
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

    private void getHTMLZip(String url, String apikey, Path targetDir) throws IOException {
        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        con.setRequestProperty("Authorization", "apikey " + apikey);

        try (ZipInputStream zis = new ZipInputStream(con.getInputStream())) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                boolean isDirectory = false;
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = targetDir.resolve(zipEntry.getName()).normalize();

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    // example 1.2
                    // some zip stored file path only, need create parent directories
                    // e.g data/folder/file.txt
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }
}
