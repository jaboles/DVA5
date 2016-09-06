package jb.common;

import java.io.InputStream;
import java.util.logging.LogManager;

public class LoggingConfiguratorFromJar
{
    public LoggingConfiguratorFromJar()
    {
        try (InputStream is = getClass().getResourceAsStream("/logging.properties"))
        {
            final LogManager logManager = LogManager.getLogManager();
            logManager.readConfiguration(is);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
