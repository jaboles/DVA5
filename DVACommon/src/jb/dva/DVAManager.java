package jb.dva;

import jb.common.ExceptionReporter;
import jb.common.sound.LevelMeterPanel;
import jb.common.sound.MediaConcatenatorFfmpeg;
import jb.common.sound.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DVAManager {
    private final static Logger logger = LogManager.getLogger(DVAManager.class);

    private Player player;
    private final File temp;
    private final SoundLibraryManager soundLibraryManager;

    public DVAManager(File temp, SoundLibraryManager soundLibraryManager) {
        this.temp = temp;
        this.soundLibraryManager = soundLibraryManager;
    }

    // Check that the script correctly and fully translates into a playable announcement, given the available
    // sound files.
    public Pair<Integer, List<URL>> verify(Script script) {
        List<URL> verifiedUrlList = null;
        try {
            verifiedUrlList = script.getTranslatedUrlList(soundLibraryManager.getSoundLibrary(script.getVoice()));
        } catch (Exception ex) {
            try {
                return new Pair<>(Integer.parseInt(ex.toString().substring(ex.toString().lastIndexOf(' ')+1)), null);
            } catch (Exception ex2) {
                ExceptionReporter.reportException(ex);
                ExceptionReporter.reportException(ex2);
            }
        }
        return new Pair<>(-1, verifiedUrlList);
    }

    public String getCanonicalScript(Script script) {
        try {
            return script.getCanonicalScript(soundLibraryManager.getSoundLibrary(script.getVoice()));
        } catch (Exception ex) {
            ExceptionReporter.reportException(ex);
        }
        return null;
    }

    public Player play(LevelMeterPanel levelMeterPanel, Script s, Runnable longConcatCallback, Runnable afterConcatCallback) {
        try {
            ArrayList<URL> al = s.getTranslatedUrlList(soundLibraryManager.getSoundLibrary(s.getVoice()));
            player = new Player(al, longConcatCallback, afterConcatCallback, levelMeterPanel, temp);

            return player;
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
        return null;
    }

    public Player play(LevelMeterPanel levelMeterPanel, Script s) {
        return play(levelMeterPanel, s, null, null);
    }

    public void stop() {
        player.stopPlaying();
    }

    public void export(Script script, String targetFile) throws Exception
    {
        // Convert to list of URLs to the wads
        ArrayList<URL> al = script.getTranslatedUrlList(soundLibraryManager.getSoundLibrary(script.getVoice()));
        File parent = (new File(targetFile)).getParentFile();
        if (parent != null && !parent.mkdirs()) logger.warn("Failed to create dir {}", parent);
        MediaConcatenatorFfmpeg.concat(al, targetFile, temp);
    }
}
