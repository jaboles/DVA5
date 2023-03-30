package jb.dva;

import jb.common.FileUtilities;
import jb.common.OSDetection;
import jb.common.ObjectCache;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SoundLibraryManager {
    private static final String CacheUniverse = "4";
    private Map<String, SoundLibrary> soundLibraryMap = new LinkedHashMap<>();
    private ObjectCache<SoundLibrary> soundLibraryCache = null;
    private final File temp;
    private final boolean specialSoundsEnabled;
    private final String dvaVersion;

    // 'Special' sounds which are only shown after enabling the option
    private static final Set<String> SPECIAL_SOUNDS = Arrays.stream(new String[] {
            "dTrog remix",
            "AnnouncementRail",
    }).collect(Collectors.toSet());

    // Set fallback libraries for incomplete sound libraries
    private static final Map<String, String> FALLBACK_LIBRARIES = Arrays.stream(new String[][] {
            { "dTrog remix", "Sydney-Male" },
            { "AnnouncementRail", "Sydney-Female" },
            { "Sydney-Male (replaced low-quality sounds)", "Sydney-Male" },
            { "Sydney-Female (replaced low-quality sounds)", "Sydney-Female" },
    }).collect(Collectors.toMap(v -> v[0], v -> v[1]));

    public SoundLibraryManager(File temp, String dvaVersion, boolean specialSoundsEnabled) throws Exception {
        this.temp = temp;
        this.dvaVersion = dvaVersion;
        this.specialSoundsEnabled = specialSoundsEnabled;
    }

    public void loadAllSoundLibraries(Consumer<String> progress) throws Exception {
        final ObjectCache<Map<String,SoundLibrary>> mc = new ObjectCache<>(temp, "soundlibrarymap_" + CacheUniverse + "_" + dvaVersion);
        soundLibraryCache = new ObjectCache<>(temp, "soundlibrary_" + CacheUniverse + "_" + dvaVersion);

        populateSoundLibraries(specialSoundsEnabled);

        // Map cache is keyed to size of the map so that if new libraries are added or removed the cache is refreshed.
        soundLibraryMap = mc.load(SoundLibraryManager.class, Integer.toString(soundLibraryMap.size()), () -> {
            soundLibraryCache.emptyCache();
            return soundLibraryMap;
        });

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
    private void populateSoundLibraries(boolean specialSoundsEnabled) {
        File f = getSoundJarsFolder();
        if (f.exists() && f.isDirectory()) {
            File[] soundDirs = f.listFiles();
            if (soundDirs != null) {
                for (File soundDir : soundDirs) {
                    String path = soundDir.getPath();
                    String name;
                    if (soundDir.isDirectory() && !path.toLowerCase().endsWith(".app")) {
                        name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                        if (!SPECIAL_SOUNDS.contains(name) || specialSoundsEnabled) {
                            getOrCreateSoundLibrary(name).addFile(soundDir);
                        }
                    } else if (path.toLowerCase().endsWith(".jar")) {
                        name = path.substring(path.lastIndexOf(File.separatorChar) + 1, path.length() - 4);
                        if (!SPECIAL_SOUNDS.contains(name) || specialSoundsEnabled) {
                            getOrCreateSoundLibrary(name).addFile(soundDir);
                        }
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
        SoundLibrary library = soundLibraryCache.load(SoundLibraryManager.class, name, () -> {
            SoundLibrary l = soundLibraryMap.get(name);
            try {
                l.populate();
            } catch (Exception ignored) {
            }
            return l;
        });

        if (library != null) {
            soundLibraryMap.put(name, library);
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
