package jb.dvacommon;

import jb.dva.SoundLibraryManager;

import java.net.URI;
import java.net.URL;

public class CloudSoundJarFetcher extends BaseUpdater
{
    public CloudSoundJarFetcher(URL baseUrl) {
        super(baseUrl);
    }

    public Thread doFetch(ProgressAdapter pw) {
        final Thread t = new Thread(() -> {
            try {
                if (downloadFolder(baseUrl, SoundLibraryManager.getSoundJarsFolder(), pw) >= 0) {
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
