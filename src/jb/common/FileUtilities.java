package jb.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileUtilities
{
    final static Logger logger = LogManager.getLogger(FileUtilities.class);

    public static void fastChannelCopy(final ReadableByteChannel source, final WritableByteChannel destination) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (source.read(buffer) != -1) {
          // prepare the buffer to be drained
          buffer.flip();
          // write to the channel, may block
          destination.write(buffer);
          // If partial transfer, shift remainder down
          // If buffer is empty, same as doing clear()
          buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
          destination.write(buffer);
        }
      }
    
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        if(destinationFile.exists() || destinationFile.createNewFile()) {
            try (
                    FileInputStream fis = new FileInputStream(sourceFile);
                    FileOutputStream fos = new FileOutputStream(destinationFile)
            ) {
                fastChannelCopy(fis.getChannel(), fos.getChannel());
            }
        }
    }
    
    public static void copyStream(InputStream sourceStream, File destinationFile) throws IOException
    {
        try (
            FileOutputStream fos = new FileOutputStream(destinationFile)
        ) {
            fastChannelCopy(Channels.newChannel(sourceStream), fos.getChannel());
        }
    }
    
    public static String readFromUrl(URL url) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
        String s;
        while ((s = r.readLine()) != null)
        {
            sb.append(s);
            sb.append("\n");
        }
        logger.debug("Read string of length {} from {}", sb.length(), url);
        return sb.toString();
    }

    public static String postToUrl(URL url, Map<String,String> params) throws Exception
    {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String s;
        while ((s = r.readLine()) != null)
        {
            sb.append(s);
            sb.append("\n");
        }
        logger.debug("Read string of length {} from {}", sb.length(), url);
        return sb.toString();
    }

    public static List<String> readLinesFromUrl(URL url) throws IOException
    {
        List<String> lines = new LinkedList<>();
        BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
        String s;
        while ((s = r.readLine()) != null)
        {
            lines.add(s);
        }
        logger.debug("Read {} lines from {}", lines.size(), url);
        return lines;
    }

    public static URLStat statUrl(URL url) throws IOException, URISyntaxException
    {
        if (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https")) {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("HEAD");
            int status = conn.getResponseCode();
            if (status >= 200 && status <= 299) {
                long length = Long.parseLong(conn.getHeaderField("Content-Length"));
                Date lastModified = new Date(conn.getHeaderFieldDate("Last-Modified", 0));
                logger.debug("stat {} success: code {}, Content-Length {}, Last-Modified {}", url, status, length, lastModified);
                return new URLStat(length, lastModified, conn.getHeaderFields());
            } else {
                logger.debug("stat {} failed: code {}", url, status);
            }
        } else if (url.getProtocol().equalsIgnoreCase("file")) {
            File f = new File(url.toURI());
            return new URLStat(f.length(), new Date(f.lastModified()), null);
        }
        return null;
    }

    public static LinkedList<String> readAllLines(File f) throws IOException
    {
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                lines.add(line);
            }
        }
        return lines;
    }
    
    public static String readAllText(String path) throws IOException
    {
        return readAllText(new File(path));
    }
    
    public static String readAllText(File f) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public static File getJarFolder(Class<?> c) {
        try {
            return new File(c.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (URISyntaxException e) { return null; }
    }

    public static File getJarFolder(Object runtimeObj) {
        return getJarFolder(runtimeObj.getClass());
    }

    public static File getApplicationDataFolder()
    {
        if (OSDetection.isMac())
        {
            return new File("/Library/Application Support");
        }
        else if (OSDetection.isWindows())
        {
            return new File(new File(System.getenv("ALLUSERSPROFILE")), "Application Data");
        }
        else
        {
            return new File("/");
        }
    }

    public static File getUserApplicationDataFolder()
    {
        if (OSDetection.isMac())
        {
            return new File(new File(System.getProperty("user.home"), "Library"), "Application Support");
        }
        else if (OSDetection.isWindows())
        {
            if (System.getenv("LOCALAPPDATA") != null)
            {
                return new File(System.getenv("LOCALAPPDATA"));
            }
            else
            {
                return new File(System.getenv("HOMEPATH"));
            }
        }
        else if (OSDetection.isUnix())
        {
            return new File(System.getProperty("user.home"));
        }
        else
        {
            return new File("/");
        }
    }
}
