package jb.dvacommon;

import java.net.MalformedURLException;
import java.net.URL;
import jb.common.FileUtilities;

public class CloudSoundJarFetcher extends BaseUpdater
{
    private URL artifactListUrl;
    
    public CloudSoundJarFetcher(URL baseUrl, URL artifactListUrl) {
        super(baseUrl);
        this.artifactListUrl = artifactListUrl;
    }
    
    public Thread doFetch(ProgressAdapter pw) {
        final Thread t = new Thread() {
            public void run()
            {
                try {
                    if (downloadIncrementalJarUpdates(baseUrl, artifactListUrl, DVA.getSoundJarsFolder(), pw) >= 0) {
                        Settings.setSoundJarsDownloaded();
                    }
                } finally {
                    pw.dispose();
                }
            }
        };
        pw.enableCancel(t);
        t.start();
        return t;
    }

    @Override
    public String getLatestVersion()
    {
        return null;
    }

    @Override
    public URL getBaseUrl(String version) throws MalformedURLException
    {
        return null;
    }
}
