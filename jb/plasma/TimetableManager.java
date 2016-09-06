package jb.plasma;

import jb.common.ObjectCache;
import jb.common.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.CodeSource;
import java.net.URL;
import javax.swing.DefaultComboBoxModel;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import jb.dvacommon.DVA;

public class TimetableManager extends DefaultComboBoxModel<Timetable>
{
    final static Logger logger = LoggerFactory.getLogger(TimetableTranslator.class);
    private static TimetableManager instance = new TimetableManager();

    private TimetableManager()
    {
    }

    public static TimetableManager getInstance()
    {
        return instance;
    }

    public static void initialize() throws Exception
    {
        getInstance().initializeInternal();
    }

    private void initializeInternal() throws Exception
    {
        CodeSource src = TimetableManager.class.getProtectionDomain().getCodeSource();
        URL jar = src.getLocation();
        ZipInputStream zip = new ZipInputStream(jar.openStream());
        while(true) {
            ZipEntry e = zip.getNextEntry();
            if (e == null)
                break;
            String name = e.getName();
            if (name.toLowerCase().endsWith(".tt")) {
                loadFrom(Integer.toString((DVA.VersionString + name).hashCode()),
                    TimetableManager.class.getResourceAsStream("/" + name));
            }
        }

        File ttDir = getTimetablesDir();
        if (ttDir.exists() && ttDir.isDirectory() && ttDir.listFiles() != null) {
            for (File f : ttDir.listFiles()) {
                loadFrom(Integer.toString((DVA.VersionString + f.getPath()).hashCode()),
                        new BufferedInputStream(new FileInputStream(f)));
            }
        }
    }

    private void loadFrom(String cacheId, final InputStream is) throws Exception
    {
        ObjectCache<Timetable> c = new ObjectCache<>("dvatmp", "timetable2");
        final Timetable tt = c.load(TimetableTranslator.class, cacheId, () -> {
            Timetable t = null;
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is, 102400))) {
                t = (Timetable) ois.readObject();
            }
            new TimetableTranslator(t).cleanUpTimetable();
            return t;
        });

        logger.info("Loaded timetable: {} lines, {} directions, {} trains", tt.getLineCount(), tt.getDirectionCount(),
                tt.getTrainCount());

        addElement(tt);
    }

    public void deleteTimetable(Timetable tt)
    {
        File ttFile = new File(getTimetablesDir(), tt.getName() + ".tt");
        if (ttFile.exists()) {
            this.removeElement(tt);
            ttFile.delete();
        }
    }

    public static File getTimetablesDir() {
        return new File(DVA.getApplicationDataFolder(), "Timetables");
    }
}
