package jb.dvacommon;

import jb.dva.SoundLibraryManager;

import java.net.URL;

public class CloudSoundJarFetcher extends BaseUpdater
{
    private final URL artifactListUrl;

    public CloudSoundJarFetcher(URL baseUrl, URL artifactListUrl) {
        super(baseUrl);
        this.artifactListUrl = artifactListUrl;
    }

    public Thread doFetch(ProgressAdapter pw) {
        final Thread t = new Thread(() -> {
            try {
                if (downloadIncrementalJarUpdates(baseUrl, artifactListUrl, SoundLibraryManager.getSoundJarsFolder(), pw) >= 0) {
                    Settings.setSoundJarsDownloaded();
                }
            } finally {
                pw.dispose();
            }
        });
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
    public URL getBaseUrl(String version)
    {
        return null;
    }
}
