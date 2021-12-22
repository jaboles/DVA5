package jb.dvacommon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jb.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;
import org.swixml.SwingEngine;

public abstract class BaseUpdater
{
    private final static Logger logger = LogManager.getLogger(BaseUpdater.class);
    protected final URL baseUrl;

    public BaseUpdater(URL baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public abstract String getLatestVersion();
    public abstract URL getBaseUrl(String version) throws MalformedURLException;

    // Run the update
    public void downloadAndInstall(final String version, final ProgressAdapter pw)
    {
        pw.show();
        final Thread t = new Thread() {
            public void run()
            {
                try
                {
                    URL baseUrl = getBaseUrl(version);
                    if (downloadIncrementalJarUpdates(baseUrl, new URL(baseUrl, "jars.list"), FileUtilities.getJarFolder(this), pw) >= 0)
                    {
                        pw.updateProgressComplete("Download complete - restarting");
                        restart();
                    }
                    else
                    {
                        File downloaded = downloadToTemp(getDownloadUrl(version), pw);
                        logger.debug("Downloaded update to {}", downloaded != null ? downloaded.getAbsolutePath() : "<null>");
                        pw.updateProgressComplete("Download complete - " + (OSDetection.isMac() ? "installing" : "launching installer"));
                        if (downloaded != null)
                            launchInstaller(downloaded);
                    }
                }
                catch (InterruptedException ex)
                {
                    // do nothing, user cancelled
                    System.out.println("interuupted");
                }
                catch (IOException ex)
                {
                    ExceptionReporter.reportException(ex);
                }
                finally
                {
                    pw.dispose();
                }
            }
        };
        pw.enableCancel(t);
        t.start();
    }

    private static boolean downloadTo(URL url, File destination, ProgressAdapter pw, Integer startingProgressPosition, Integer totalProgress, String currentFile) throws IOException
    {
        if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {logger.warn("Failed to mkdir {}", destination.getParentFile());}
        if (destination.exists() && !destination.delete()) {logger.warn("Failed to delete {}", destination);}

        if (startingProgressPosition == null) startingProgressPosition = 0;

        URLConnection uc = url.openConnection();
        final int contentLength = uc.getContentLength();
        if (totalProgress == null) totalProgress = contentLength;

        byte[] data = new byte[102400];
        int totalRead = 0;

        try (
            OutputStream fout = new BufferedOutputStream(new FileOutputStream(destination), 1048576);
            InputStream in = new BufferedInputStream(url.getPath().toLowerCase().endsWith(".gz")
                ? new GZIPInputStream(uc.getInputStream())
                : uc.getInputStream())) {
            for (int count; (count = in.read(data, 0, data.length)) != -1; totalRead += count) {
                if (Thread.interrupted()) return false;
                fout.write(data, 0, count);
                pw.updateProgress(startingProgressPosition + totalRead, totalProgress, "Downloading", currentFile);
            }
            pw.disableCancel();
        }

        return totalRead == contentLength;
    }

    private static File downloadToTemp(URL url, ProgressAdapter pw) throws IOException
    {
        final File downloaded = new File(System.getProperty("java.io.tmpdir"), url.getFile());
        return downloadTo(url, downloaded, pw, null, null, null) ? downloaded : null;
    }

    public URL getDownloadUrl(String version) throws MalformedURLException
    {
        String filename;
        if (OSDetection.isMac())
            filename = "DVA5.dmg.bz2";
        else if (OSDetection.isWindows())
            filename = "DVA5Setup.exe";
        else
            filename = "DVA5-" + System.getProperty("os.arch") + ".deb";
        return new URL(getBaseUrl(version), filename);
    }

    public URL getVersionHistoryUrl(String version) throws MalformedURLException
    {
        return new URL(getBaseUrl(version), "new.html");
    }

    public static int downloadIncrementalJarUpdates(URL baseUrl, URL artifactList, File localArtifactRoot, ProgressAdapter pw)
    {
        List<Triplet<URL,URLStat,File>> rv = new LinkedList<>();
        File dest = null;
        try {
            if (!localArtifactRoot.exists() && !localArtifactRoot.mkdirs()) {
                logger.error("Could not create local artifact root {}", localArtifactRoot);
                return -1;
            }
            List<String> urls = FileUtilities.readLinesFromUrl(artifactList);
            logger.info("Downloading {} artifacts", urls.size());
            for (String s : urls) {
                logger.info("Base URL {}", baseUrl);
                URL url = new URL(baseUrl, s.replaceAll(" ", "%20"));
                URLStat stat = FileUtilities.statUrl(url);
                String urlPath = url.getPath();
                String artifactName = urlPath.substring(urlPath.lastIndexOf('/') + 1).replaceAll("%20", " ");
                if (artifactName.toLowerCase().endsWith(".gz")) artifactName = artifactName.substring(0, artifactName.length() - 3);
                dest = new File(localArtifactRoot, artifactName);

                boolean willUpdate = dest.lastModified() != getRealLastModified(stat) || dest.length() != stat.Size;
                logger.info("Artifact {} local stats ({}, {}), remote ({}, {}), will update: {}", artifactName, dest.length(), new Date(dest.lastModified()), stat.Size, new Date(getRealLastModified(stat)), willUpdate);
                if (willUpdate) {
                    rv.add(new Triplet<>(url, stat, dest));
                }
            }

            int total = rv.size() > 0 ? rv.stream().map(u -> u.getValue1().Size)
                    .reduce(0L, Long::sum).intValue() : 0;
            int starting = 0;
            for (Triplet<URL,URLStat,File> updateFile : rv) {
                URL url = updateFile.getValue0();
                URLStat stat = updateFile.getValue1();
                dest = updateFile.getValue2();
                logger.info("Updating artifact {} at path {}, starting progress {} total {}", url, dest, starting, total);
                downloadTo(url, dest, pw, starting, total, dest.getName());
                dest.setLastModified(getRealLastModified(stat));
                dest.setReadable(true, false);
                dest.setWritable(true, false);
                starting += stat.Size;
            }
            return rv.size();
        } catch (Exception e) {
            if (dest != null && dest.exists() && !dest.delete()) {logger.warn("Failed to delete {}", dest);}
            logger.info("Caught exception doing incremental update:", e);
            e.printStackTrace(System.err);
        }
        return -1;
    }

    private void launchInstaller(File downloadedInstaller) throws IOException, InterruptedException
    {
        String installerPath = downloadedInstaller.getPath();
        if (OSDetection.isMac()) // Mac OSX
        {
            Process p;
            // Unzip
            int extensionLength = 3;
            p = new ProcessBuilder("bunzip2", "-f", installerPath).start();
            p.waitFor();

            // Mount
            p = new ProcessBuilder("hdiutil", "attach", "-mount", "required", installerPath.substring(0, installerPath.length() - extensionLength - 1)).start();
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            br.readLine();
            String volumeInfo = br.readLine();
            String volume = volumeInfo.split("\\s+", 3)[2];

            // Write script to copy to Applications folder, unmount and relaunch.
            File updateScript = File.createTempFile("dva_update", "");
            logger.debug("Created update launcher script: {}", updateScript.getAbsolutePath());
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(updateScript)));
            w.println("sleep 2");
            w.println("cp -Rfp \"" + volume + "/DVA.app\" \"" + getMacAppFolder() + "\"");
            w.println("umount \"" + volume + "\"");
            w.println("open \"" + getMacAppPath() + "\"");
            w.close();
            if (updateScript.canExecute() || updateScript.setExecutable(true)) {
                new ProcessBuilder(updateScript.getPath()).start();
            }
        }
        else if (OSDetection.isWindows()) // Windows
        {
            // Run the installer quietly
            ProcessBuilder updater = new ProcessBuilder(installerPath, "/silent");
            int attempts = 5;
            while (attempts > 0)
            {
                try
                {
                    updater.start();
                    break;
                }
                catch (IOException e)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    attempts--;
                }
            }
        }
        else if (OSDetection.isUnix())
        {
            // Run the installer quietly
            Process p = new ProcessBuilder("xdg-open", installerPath, "&>/dev/null").start();
            p.waitFor();
        }
        System.exit(0);
    }

    protected void restart() throws IOException
    {
        if (SwingEngine.isMacOSX()) // Mac OSX
        {
            // Write script to copy to Applications folder, unmount and relaunch.
            File updateScript = File.createTempFile("dva_restart", "");
            logger.debug("Created restart launcher script: {}", updateScript.getAbsolutePath());
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(updateScript)));
            w.println("sleep 2");
            w.println("open \"" + getMacAppPath() + "\"");
            w.close();
            updateScript.setExecutable(true);
            new ProcessBuilder(updateScript.getPath()).start();
        }
        else
        {
            String path = new File(FileUtilities.getJarFolder(this), "DVA.exe").getPath();
            new ProcessBuilder(path).start();
        }
        System.exit(0);
    }

    private String getMacAppPath() {
        String appName = "DVA.app";
        String jarPath = FileUtilities.getJarFolder(this).getPath();
        return jarPath.substring(0, jarPath.lastIndexOf(appName) + appName.length());
    }

    private String getMacAppFolder() {
        String appName = "DVA.app";
        String jarPath = FileUtilities.getJarFolder(this).getPath();
        return jarPath.substring(0, jarPath.lastIndexOf(appName));
    }

    private static long getRealLastModified(URLStat stat) {
        return stat.Headers != null && stat.Headers.containsKey("x-ms-meta-" + WAzureUpdater.PersistedLastModifiedTimestamp)
            ? Long.parseLong(stat.Headers.get("x-ms-meta-" + WAzureUpdater.PersistedLastModifiedTimestamp).get(0))
            : stat.LastModified.getTime();
    }
}