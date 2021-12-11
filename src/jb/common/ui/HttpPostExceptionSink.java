package jb.common.ui;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpPostExceptionSink implements IExceptionSink
{
    public void store(String message) throws MalformedURLException, IOException
    {
        URL url = new URL("http://jonathanboles.com/dva_reporterror.php");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);

        // Send exception data
        byte[] postData = message.getBytes();
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

        try (OutputStream os = conn.getOutputStream())
        {
            os.write(postData);
            os.flush();

            // Read response
            try (InputStream is = conn.getInputStream())
            {
                new BufferedReader(new InputStreamReader(is)).readLine();
                int statusCode = ((HttpURLConnection)conn).getResponseCode();
                System.err.println("Reported application exception: " + Integer.toString(statusCode));
            }
        }
    }
}
