package jb.common;

import jb.common.ui.HttpPostExceptionSink;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ExceptionReporter
{
    public static String ApplicationName;
    public static String ApplicationVersion;

    // Report application exceptions and crashes
    public static void reportException(final Exception e) {
    
        // Print out exception to stderr
        System.err.println(e.toString());
        e.printStackTrace(System.err);
    
        // Report application exception
        Thread t = new Thread()
        {
            public void run()
            {
                try {
                    // Convert exception data to string
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    pw.println("Application: " + ApplicationName);
                    pw.println("Version: " + ApplicationVersion);
                    e.printStackTrace(pw);
                    // Normalise line endings
                    String message = sw.toString().replace("\r\n", "\n").replace("\n", "\r\n");

                    new HttpPostExceptionSink().store(message);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    System.err.println("Could not report application exception. Original exception follows");
                    System.err.println(e.toString());
                    e.printStackTrace(System.err);
                }
            }
        };
        t.start();
    }
}
