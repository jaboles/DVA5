package jb.dvacommon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.VersionComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleWebsiteUpdater extends BaseUpdater
{
    final static Logger logger = LogManager.getLogger(SimpleWebsiteUpdater.class);
    
    private String latestVersion = null;
    final static Pattern VersionPattern = Pattern.compile("<a href=\"(\\d+\\.\\d+\\.\\d+)/\">");
    
    public SimpleWebsiteUpdater(URL baseUrl)
    {
        super(baseUrl);
    }

    public String getLatestVersion() {
        if (latestVersion == null)
        {
            List<String> versions = null;
            String protocol = baseUrl.getProtocol().toLowerCase();
            if (protocol.equals("file"))
            {
                versions = getAllVersionsFromFileBaseUrl(baseUrl);
            }
            else if (protocol.equals("http") || protocol.equals("https"))
            {
                versions = getAllVersionsFromHttpBaseUrl(baseUrl);
            }
            latestVersion = versions.stream().max(VersionComparator.Instance).get();
        }
        return latestVersion;
    }
    
    public URL getBaseUrl(String version) throws MalformedURLException
    {
        return new URL(baseUrl, version + "/");
    }

    private static List<String> getAllVersionsFromHttpBaseUrl(URL baseUrl)
    {
        List<String> versions = new ArrayList<>();
        try {
            String sb = FileUtilities.readFromUrl(baseUrl);
            Matcher m = VersionPattern.matcher(sb);
            while (m.find())
            {
                versions.add(m.group(1));
            }
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
        return versions;
    }
    
    private static List<String> getAllVersionsFromFileBaseUrl(URL baseUrl)
    {
        List<String> versions = new ArrayList<>();
        try {
            File rootDir = new File(baseUrl.toURI());
            logger.debug("Finding versions at {} from baseURL {}", rootDir.getAbsolutePath(), baseUrl);

            File[] files = rootDir.listFiles();
            if (files != null) {
                versions = Arrays.stream(files).filter(File::isDirectory).map(File::getName).collect(Collectors.toList());
            }
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
        return versions;
    }
}
