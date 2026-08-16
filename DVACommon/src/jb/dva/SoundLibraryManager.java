package jb.dva;

import jb.common.FileUtilities;
import jb.common.OSDetection;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SoundLibraryManager {
    private Map<String, SoundLibrary> soundLibraryMap = new LinkedHashMap<>();
    private final File temp;
    private final String dvaVersion;

    // Set fallback libraries for incomplete sound libraries
    private static final Map<String, String> FALLBACK_LIBRARIES = Arrays.stream(new String[][] {
            { "dTrog remix", "Sydney-Male" },
            { "AnnouncementRail", "Sydney-Female" },
            { "Sydney-Male (replaced low-quality sounds)", "Sydney-Male" },
            { "Sydney-Female (replaced low-quality sounds)", "Sydney-Female" },
    }).collect(Collectors.toMap(v -> v[0], v -> v[1]));

    public SoundLibraryManager(File temp, String dvaVersion) throws Exception {
        this.temp = temp;
        this.dvaVersion = dvaVersion;
    }

    public void loadAllSoundLibraries(Consumer<String> progress) throws Exception {
        populateSoundLibraries();

        for (SoundLibrary library : soundLibraryMap.values()) {
            if (FALLBACK_LIBRARIES.containsKey(library.getName())) {
                library.addFallback(soundLibraryMap.get(FALLBACK_LIBRARIES.get(library.getName())));
            }
        }

        for (final Map.Entry<String, SoundLibrary> entry : soundLibraryMap.entrySet()) {
            if (progress != null)
                progress.accept(entry.getValue().getName());

            loadSoundLibrary(entry.getKey());
        }

        for (SoundLibrary library : soundLibraryMap.values()) {
            if (FALLBACK_LIBRARIES.containsKey(library.getName())) {
                library.addFallback(soundLibraryMap.get(FALLBACK_LIBRARIES.get(library.getName())));
            }
        }

        soundLibraryMap.put("All", new SoundLibrary("All", new LinkedList<>(soundLibraryMap.values()), SoundLibraryManager.class.getResource("/all.png")));
    }

    // Find folders and jars next to the application and load them as sound libraries.
    private void populateSoundLibraries() {
        File f = getSoundJarsFolder();
        if (f.exists() && f.isDirectory()) {
            File[] soundDirs = f.listFiles();
            if (soundDirs != null) {
                for (File soundDir : soundDirs) {
                    String path = soundDir.getPath();
                    String name;
                    if (soundDir.isDirectory() && !path.toLowerCase().endsWith(".app")) {
                        name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                        if (name.equals("AnnouncementRail") || name.equals("dTrog remix")) continue;
                        getOrCreateSoundLibrary(name).addFile(soundDir);
                    } else if (path.toLowerCase().endsWith(".jar")) {
                        name = path.substring(path.lastIndexOf(File.separatorChar) + 1, path.length() - 4);
                        if (name.equals("AnnouncementRail") || name.equals("dTrog remix")) continue;
                        getOrCreateSoundLibrary(name).addFile(soundDir);
                    }
                }
            }
        }
    }

    private SoundLibrary getOrCreateSoundLibrary(String name) {
        if (soundLibraryMap.containsKey(name)) {
            return soundLibraryMap.get(name);
        } else {
            SoundLibrary sl = new SoundLibrary(name);
            soundLibraryMap.put(name, sl);
            return sl;
        }
    }

    public void loadSoundLibraryWithFallback(String name) throws Exception {
        loadSoundLibrary(name);
        String fallbackLibraryName = FALLBACK_LIBRARIES.get(name);
        loadSoundLibrary(fallbackLibraryName);

        for (String key : soundLibraryMap.keySet()) {
            SoundLibrary library = soundLibraryMap.get(key);
            if (FALLBACK_LIBRARIES.containsKey(library.getName())) {
                library.addFallback(soundLibraryMap.get(FALLBACK_LIBRARIES.get(library.getName())));
            }
        }
    }

    private void loadSoundLibrary(String name) throws Exception {
        SoundLibrary library = soundLibraryMap.get(name);
        try {
            if (library != null)
            {
                library.populate();
                soundLibraryMap.put(name, library);
            }
        } catch (Exception ignored) {
        }
    }

    public SoundLibrary getSoundLibrary(String key) {
        return soundLibraryMap.get(key);
    }

    public Map<String, SoundLibrary> getSoundLibraries() { return soundLibraryMap; }

    public static File getSoundJarsFolder()
    {
        if (OSDetection.isWindows() || OSDetection.isMac())
        {
            return new File(FileUtilities.getUserApplicationDataFolder(), "DVA");
        }
        else
        {
            return new File(FileUtilities.getUserApplicationDataFolder(), ".dva");
        }
    }
}
