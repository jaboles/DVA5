package jb.plasma.gtfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GtfsHttpClient
{
    private static final Logger Logger = LoggerFactory.getLogger(GtfsHttpClient.class);
    private static final String APIKEY = "3LP1iesTsBqiO2rzMGdmPJ3EJV1ubm3FqyP0";

    public static byte[] getByteArray(String url) throws IOException
    {
        Logger.info("Requesting to {}", url);
        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        con.setRequestProperty("Authorization", "apikey " + APIKEY);

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            try (InputStream is = con.getInputStream()) {
                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                return buffer.toByteArray();
            }
        }
    }

    public static void getHTMLZip(String url, Path targetDir) throws IOException {

        Logger.info("Requesting to {}", url);
        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        con.setRequestProperty("Authorization", "apikey " + APIKEY);

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
