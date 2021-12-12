package jb.common.sound;

import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.OSDetection;
import jb.dvacommon.ProgressAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MediaConcatenatorFfmpeg
{
    private final static Logger logger = LogManager.getLogger(MediaConcatenatorFfmpeg.class);

    public static void concat(List<URL> urlList, String outputFile, ProgressAdapter pa) {
        if (pa != null) {
            pa.show();
            pa.updateProgress(0, 0, "Processing...", null);
        }

        logger.info("Concatenating {} items", urlList.size());

        try
        {
            File tempDir = getTemp();
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            File ffmpeg = new File(FileUtilities.getJarFolder(MediaConcatenatorFfmpeg.class),
                    OSDetection.isWindows() ? "ffmpeg" : "ffmpeg.exe");
            List<String> ffmpegCmd = new LinkedList<>();
            ffmpegCmd.add(ffmpeg.getAbsolutePath());
            StringBuilder ffmpegCmdFilterComplex = new StringBuilder();

            Iterator<URL> it = urlList.iterator();
            for (int i = 0; it.hasNext(); i++)
            {
                URL u = it.next();
                logger.debug("Concatenating: {}", u);
                if (pa != null) pa.updateProgress(0, 0, "Processing...", u.toString());

                File tempFile = new File(tempDir, Integer.toString(i));
                Files.copy(u.openStream(), Paths.get(tempFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
                ffmpegCmd.add("-i");
                ffmpegCmd.add(Integer.toString(i));
                ffmpegCmdFilterComplex.append("[").append(i).append(":0]");
            }

            ffmpegCmd.add("-filter_complex");
            ffmpegCmdFilterComplex.append("concat=n=");
            ffmpegCmdFilterComplex.append(urlList.size());
            ffmpegCmdFilterComplex.append(":v=0:a=1[out]");
            ffmpegCmd.add(ffmpegCmdFilterComplex.toString());
            ffmpegCmd.add("-map");
            ffmpegCmd.add("[out]");
            ffmpegCmd.add(outputFile);

            logger.info("Running ffmpeg: {}", String.join(" ", ffmpegCmd));
            Process p = new ProcessBuilder(ffmpegCmd)
                    .directory(tempDir)
                    .start();
            p.waitFor();

            BufferedReader processReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = processReader.readLine()) != null)
                logger.debug(line);
            processReader.close();
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
        if (pa != null) pa.dispose();
    }

    private static File getTemp()
    {
        return new File(System.getProperty("java.io.tmpdir") + File.separator + "dvatmp" + File.separator + "c");
    }
}
