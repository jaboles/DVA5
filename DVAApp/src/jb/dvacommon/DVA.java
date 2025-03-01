package jb.dvacommon;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.swing.UIManager;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.*;
import jb.common.*;
import jb.common.jna.windows.GDI32Ex;
import jb.common.jna.windows.Shell32Ex;
import jb.common.jna.windows.User32Ex;
import jb.common.sound.Player;
import jb.common.ui.MacOSUtilities;
import jb.dva.DVAManager;
import jb.dva.SoundLibraryManager;
import jb.dvacommon.ui.ProgressWindow;
import jb.dva.Script;
import jb.dvacommon.ui.DVAShell;
import jb.dvacommon.ui.LicenceWindow;
import jb.dvacommon.ui.LoadWindow;
import jb.plasma.gtfs.GtfsGenerator;
import jb.plasma.gtfs.GtfsTimetable;
import jb.plasma.gtfs.GtfsTimetableTranslator;
import jb.plasma.ui.PlasmaUI;
import jb.plasma.ui.PlasmaWindow;
import jb.plasma.ui.ScreenSaverSettingsDialog;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DVA {
    private static final Logger logger = LogManager.getLogger(DVA.class);
    public static String VersionString;
    public static final String CopyrightMessage = "Copyright © Jonathan Boles 1999-2024";

    private DVAShell mainWindow;
    private final SoundLibraryManager soundLibraryManager;
    private final DVAManager dvaManager;

    public DVA() throws Exception {
        try {
            Properties props = new Properties();
            props.load(LoadWindow.class.getResourceAsStream("/version.txt"));
            VersionString = props.getProperty("version");
            Utilities.compareVersion(VersionString, "0.0.0");
        } catch (NullPointerException | NumberFormatException e) {
            VersionString = "0.0.0";
        }

        logger.info("DVA: {}, Java: {} {}", VersionString, System.getProperty("java.version"), System.getProperty("os.arch"));
        logger.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("Temp is: {}", getTemp());
        logger.info("FFmpeg.log: {}ffmpeg.log", new File(getTemp(), "FFmpeg.log").getAbsolutePath());
        Player.emptyCache(getTemp());

        soundLibraryManager = new SoundLibraryManager(getTemp(), VersionString, Settings.specialSoundsEnabled());
        dvaManager = new DVAManager(getTemp(), soundLibraryManager);
    }

    public void runApp(boolean showMainWindow, boolean showLoadingProgress) throws Exception {
        if (Settings.soundJarsNotDownloaded())
        {
            fetchSoundJars();
        }
        showLicenceIfNotRead();

        final LoadWindow lw = new LoadWindow();
        if (showLoadingProgress) {
            boolean fade = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
            lw.show(false, showMainWindow, fade);
        }

        logger.info("Loading sound libraries");
        soundLibraryManager.loadAllSoundLibraries(showLoadingProgress
                ? name -> lw.setText("Loading sound libraries... " + name)
                : null);

        if (soundLibraryManager.getSoundLibraries().size() <= 1)
        {
            fetchSoundJars();

            soundLibraryManager.loadAllSoundLibraries(showLoadingProgress
                    ? name -> lw.setText("Loading sound libraries... " + name)
                    : null);
        }

        logger.info("Fetching GTFS timetable");
        if (showLoadingProgress) lw.setText("Fetching GTFS timetable... ");
        GtfsGenerator.initialize(new File(getTemp(), "GtfsTimetable").toPath());
        GtfsGenerator.getInstance().download(false);

        logger.info("Reading timetable data");
        if (showLoadingProgress) lw.setText("Reading timetable data... ");
        GtfsTimetable tt;
        try {
            tt = GtfsGenerator.getInstance().read();
        } catch (NoSuchFileException e) {
            if (showLoadingProgress) lw.setText("Detected incomplete timetable data, re-downloading... ");
            GtfsGenerator.getInstance().download(true);
            if (showLoadingProgress) lw.setText("Reading timetable data... ");
            tt = GtfsGenerator.getInstance().read();
        }

        int steps = GtfsTimetable.getAnalysisStepCount();
        if (showLoadingProgress) lw.setText("Reading timetable data... indexing stops");
        for (int i = 0; i < steps; i++)  {
            String stepDescription = tt.analyse(i);
            if (showLoadingProgress) lw.setText("Analysing timetable... " + stepDescription);
        }
        GtfsTimetableTranslator.initialize(tt, getTemp());

        if (showMainWindow) {
            logger.info("Loading main window");
            if (showLoadingProgress) {
                lw.setText("Loading...");
            }
            mainWindow = new DVAShell(dvaManager, soundLibraryManager.getSoundLibraries(), getTemp());
        }

        if (showLoadingProgress) {
            lw.setText("");
            lw.dispose();
        }

        if (showMainWindow) {
            mainWindow.setVisible(true);
        }
    }

    private void screensaver() throws Exception {
        runApp(false, false);
        new PlasmaUI(PlasmaUI.Mode.SCREENSAVER, dvaManager, getTemp())
                .showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER, null);
    }

    private void screensaverPreview(long windowHandle) {
        WinDef.HWND rvhwnd;
        int rv;
        boolean rvb;

        // HWnd of the screensaver preview mini-window rooted in the screensaver settings dialog box
        WinDef.HWND phwnd = new WinDef.HWND(Pointer.createConstant(windowHandle));
        logger.info("Preview parent window handle: {}", phwnd);

        // Get dimensions of the Windows window to install the plasma window into
        WinDef.RECT parentRectNative = new WinDef.RECT();
        rvb = User32Ex.INSTANCE.GetClientRect(phwnd, parentRectNative);
        logger.info("GetClientRect returned {}", rvb);
        Rectangle parentRect = parentRectNative.toRectangle();

        // Scale dimensions by the DPI setting
        // HAX!!!1 http://stackoverflow.com/questions/7003316/windows-display-setting-at-150-still-shows-96-dpi
        WinDef.HDC dc = User32.INSTANCE.GetDC(new WinDef.HWND(Pointer.NULL));
        int virtualWidth = GDI32.INSTANCE.GetDeviceCaps(dc, GDI32Ex.HORZRES);
        int physicalWidth = GDI32.INSTANCE.GetDeviceCaps(dc, GDI32Ex.DESKTOPHORZRES);
        User32.INSTANCE.ReleaseDC(new WinDef.HWND(Pointer.NULL), dc);
        double scaleFactor = (double)physicalWidth / (double)virtualWidth;
        parentRect.setSize((int)(parentRect.getWidth() * scaleFactor), (int)(parentRect.getHeight() * scaleFactor));

        // Create the plasma window
        Window w = new PlasmaUI(PlasmaUI.Mode.SCREENSAVER_PREVIEW, null, getTemp())
                .showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER_PREVIEW_MINI_WINDOW, parentRect.getSize()).get(0);
        WinDef.HWND hwnd = new WinDef.HWND(Native.getWindowPointer(w));
        logger.info("Window handle: {}", hwnd);

        // Shove the plasma window into the screensaver settings dialog box
        rvhwnd = User32.INSTANCE.SetParent(hwnd, phwnd);
        logger.info("SetParent returned {}", rvhwnd);
        rv = User32.INSTANCE.SetWindowLong(hwnd, User32.GWL_STYLE, User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE) | User32.WS_CHILD);
        logger.info("SetWindowLong returned {}", rv);
        rvb = User32.INSTANCE.SetWindowPos(hwnd, phwnd, 0, 0, (int)parentRect.getWidth(), (int)parentRect.getHeight(), User32.SWP_NOZORDER | User32Ex.SWP_NOACTIVATE);
        logger.info("SetWindowPos returned {}", rvb);
    }

    private void screensaverSettings() throws Exception {
        runApp(false, true);
        new ScreenSaverSettingsDialog(dvaManager, getTemp()).setVisible(true);
    }

    private void play(Script announcement) {
        try {
            soundLibraryManager.loadSoundLibraryWithFallback(announcement.getVoice());
            dvaManager.verify(announcement);
            logger.info("Playing: '{}'", dvaManager.getCanonicalScript(announcement));
            Player p = dvaManager.play(null, announcement, null, null);
            p.start();
            new Thread(() -> {
                try {
                    p.join();
                    logger.info("Success.");
                    System.exit(0);
                } catch (InterruptedException ignored) {
                }
            }).start();
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    private void export(Script announcement, String filename) {
        try {
            logger.info("Loading sound library '{}'", announcement.getVoice());
            soundLibraryManager.loadSoundLibraryWithFallback(announcement.getVoice());
            dvaManager.verify(announcement);
            if (!Paths.get(filename).isAbsolute())
            {
                filename = Paths.get(System.getProperty("user.dir")).resolve(filename).toString();
            }
            File file = new File(filename);
            if (file.exists()) file.delete();
            logger.info("Exporting to '{}': '{}'", filename, dvaManager.getCanonicalScript(announcement));
            try {
                dvaManager.export(announcement, filename);
            } catch (Exception e) {
                ExceptionReporter.reportException(e);
            }
            logger.info("Success.");
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    // This is where it all starts.
    public static void main(String[] args) {
        ExceptionReporter.ApplicationName = "DVA";
        ExceptionReporter.ApplicationVersion = DVA.VersionString;

        if (args.length > 0 && args[0].equalsIgnoreCase("/clearsettings")) {
            // Delete settings
            Settings.deleteAll();
            System.exit(0);
        }

        if (!getTemp().exists() && !getTemp().mkdirs()) {logger.warn("Failed to mkdir {}", getTemp());}

        // Apple UI stuff
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DVA");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("sun.java2d.metal", "true");

        // Set TLS version to 1.2 only
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        // Set AUMI on Windows 7, to fix two separate DVA taskbar icons appearing during
        // launch (DVA.exe and java.exe)
        boolean isWindows = OSDetection.isWindows();
        if (isWindows && VersionComparator.Instance.compare(System.getProperty("os.version"), "6.1") >= 0) {
            Shell32Ex.INSTANCE.SetCurrentProcessExplicitAppUserModelID(new WString("jb.DVA"));
        }

        /*try {
		 // High-DPI scaling
		 int pixelPerInch=java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		 javax.swing.JOptionPane.showMessageDialog(null, "pixelPerInch: "+pixelPerInch);

		 for (Iterator i = UIManager.getLookAndFeelDefaults().keySet().iterator(); i.hasNext();) {
		 String key = (String) i.next();
		 System.out.println(key);
		 if(key.endsWith(".font")) {
		 Font font = UIManager.getFont(key);
		 Font biggerFont = font.deriveFont((pixelPerInch / 72f) * font.getSize2D());
		 // change ui default to bigger font
		 UIManager.put(key,biggerFont);
		 }
		 }
		 } catch (Exception e) {
		 // wtf?? Impossible situation
		 }*/

        try {
            // Set native look and feel
            if (OSDetection.isMac()) {
                String laf = Settings.getLookAndFeelName();
                if (laf.equals("auto") ||
                        (laf.equals("dark") && MacOSUtilities.isDarkTheme())) {
                    System.setProperty("apple.awt.application.appearance", "system");
                }
            }
            UIManager.setLookAndFeel(DVAShell.getLookAndFeelClassName(Settings.getLookAndFeelName()));
        } catch (Exception e) {
            // wtf?? Impossible situation
        }

        logger.info("argc: {}", args.length);

        try {
            // Run the program in different ways depending on command line switches
            if (args.length > 0 && args[0].equalsIgnoreCase("/x"))
            {
                // Sound download during Windows installer -- exit
                fetchSoundJars();
            }
            else if (args.length >= 3 && args[0].equalsIgnoreCase("/play"))
            {
                String soundLibrary = args[1];
                String announcementText = args[2];
                new DVA().play(new Script(soundLibrary, announcementText));
            }
            else if (args.length >= 4 && args[0].equalsIgnoreCase("/export"))
            {
                String filename = args[1];
                String soundLibrary = args[2];
                String announcementText = args[3];
                new DVA().export(new Script(soundLibrary, announcementText), filename);
            }
            else if (args.length > 0 && args[0].equalsIgnoreCase("/s"))
            {
                // Windows screen saver mode
                new DVA().screensaver();
            }
            else if (args.length > 0 && (args[0].equalsIgnoreCase("/c") || args[0].startsWith("/c:") || args[0].startsWith("/C:")))
            {
                // Run the plasma UI in screen saver setting mode, outside of the regular
                // application.
                new DVA().screensaverSettings();
            }
            else if (OSDetection.isWindows() && args.length > 0 && args[0].toLowerCase().startsWith("/p") && args.length >= 2)
            {
                // Windows screen saver preview mode
                long windowHandle = Long.parseLong(args[1]);
                new DVA().screensaverPreview(windowHandle);
            } else {
                // Regular application
                new DVA().runApp(true, true);
            }
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    private static void showLicenceIfNotRead()
    {
        // Show the licence if it hasn't been displayed for this version.
        if (!Settings.isLicenceRead())
        {
            LicenceWindow licenceWindow = new LicenceWindow();
            licenceWindow.showFirstTime();
            if (!licenceWindow.accepted())
            {
                System.exit(0);
            }

            Settings.setLicenceRead();
        }
    }

    private static void fetchSoundJars()
    {
        ProgressWindow pw = new ProgressWindow("Download Progress", "Updating sound libraries...");
        pw.setProgressText("Checking for updated sound libraries");
        ProgressAdapter pa = new ProgressAdapter(pw);
        pw.show();
        try {
            new CloudSoundJarFetcher(
                new URL(new URL("https://dvaupdate.blob.core.windows.net/"), WAzureUpdater.SoundJarsContainerName + "/"),
                new URL(new URL("https://dvaupdate.blob.core.windows.net/"), WAzureUpdater.MetadataContainerName + "/" + WAzureUpdater.SoundJarsList))
            .doFetch(pa)
            .join();
        } catch (MalformedURLException | InterruptedException ignored) {
        }
    }

    public static File getTemp()
    {
        return new File(System.getProperty("java.io.tmpdir"), "DVA");
    }
}