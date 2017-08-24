package jb.plasma.ui;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import jb.common.ExceptionReporter;
import jb.common.sound.Player;
import jb.common.ui.ColorComboBox;
import jb.common.ui.JBComboBox;
import jb.common.ui.ProportionalPanel;
import jb.common.ui.RotatedIcon;
import jb.common.ui.TextIcon;
import jb.dva.Script;
import jb.dvacommon.DVA;
import jb.dvacommon.ProgressAdapter;
import jb.common.ui.ProgressWindow;
import jb.dvacommon.Settings;
import jb.plasma.Announcer;
import jb.plasma.DepartureData;
import jb.plasma.Drawer;
import jb.plasma.Generator;
import jb.plasma.IndicatorSettings;
import jb.plasma.NullDrawer;
import jb.plasma.PlasmaSession;
import jb.plasma.Timetable;
import jb.plasma.TimetableManager;
import jb.plasma.TimetableTranslator;
import jb.plasma.announcers.CityrailStandard;
import jb.plasma.announcers.NswCountry;
import jb.plasma.renderers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swixml.SwingEngine;
import org.swixml.XHBox;
import org.swixml.XVBox;

// The main CityRail Indicators application. It is usually hosted inside the DVA application, but
// can exist outside of it, which is used by the screen saver mode.
public class PlasmaUI
{
    public class Mode
    {
        public static final int REGULAR = 0;
        public static final int SCREENSAVER = 1;
        public static final int SCREENSAVER_PREVIEW = 2;
    }
    
    private static String[] departurePanelTitles = new String[] { "Next Train:", "2nd Train:", "3rd Train:" };
    final static Logger logger = LoggerFactory.getLogger(PlasmaUI.class);
    private DVA dva;
    private String settingsKey;
    private PlasmaSession session;
    private TimetableManager timetableManager;

    private JPanel panel;
    public JTabbedPane tabbedPane;

    public JButton promoteDeparturesButton;
    public XVBox departuresList;
    private DeparturePanel[] departurePanels = new DeparturePanel[3];
    public JButton playStopButton;
    private List<DepartureData> departureData = new LinkedList<>();

    public JBComboBox<Timetable> timetable;
    public JBComboBox<String> scheduleLine;
    public JBComboBox<String> scheduleDirection;
    public JBComboBox<String> scheduleStation;
    public JSpinner carsValue;
    public JSpinner platformValue;

    public JCheckBox playAnnouncementCheckbox;
    public JTextField playAnnouncementTimes;
    public JComboBox<Announcer> playAnnouncementVoiceCombobox;
    public JCheckBox coalesceStationSequencesCheckbox;
    public JPanel startButtonsPanel;
    public JPanel previewButtonPanel;
    public XVBox rendererComboboxesPanel;
    private List<JComboBox<Drawer>> rendererComboBoxes;
    
    private Player player;

    public PlasmaUI(int mode, DVA dva) {
        this.dva = dva;
        this.settingsKey = (mode == Mode.SCREENSAVER || mode == Mode.SCREENSAVER_PREVIEW) ? "screenSaver" : "remembered";
        Drawer.initializeFonts();

        SwingEngine renderer = new SwingEngine(this);
        renderer.getTaglib().registerTag("colorcombobox", ColorComboBox.class);
        renderer.getTaglib().registerTag("jbcombobox", JBComboBox.class);
        try {
            panel = (JPanel) renderer.render(PlasmaUI.class.getResource("/jb/plasma/ui/resources/ui.xml"));
            TextIcon ti = new TextIcon(promoteDeparturesButton, "     <<< Shift Departures Upwards <<<    ");
            ti.setFont(new JLabel().getFont());
            RotatedIcon ri = new RotatedIcon(ti, RotatedIcon.Rotate.DOWN);
            promoteDeparturesButton.setIcon(ri);

            // Instantiate the available renderers and announcers
            Drawer[] renderers = new Drawer[] {
                    new CityrailV4Portrait(false),
                    new CityrailV4Portrait(true),
                new CityrailV4Primary(), new CityrailV4Secondary(),
                new CityrailV3Primary(), new CityrailV3Secondary(),
                new CityrailV2(false), new CityrailV1Portrait(true, true), new CityrailV1Landscape(true, false),
                new CityrailV1Portrait(false, true), new CityrailV1Landscape(false, false) };

            Announcer[] announcers = null;
            if (dva != null)
            {
                announcers = new Announcer[] { new CityrailStandard(dva.getSoundLibrary("Sydney-Male"), true),
                    new CityrailStandard(dva.getSoundLibrary("Sydney-Female"), false),
                    new NswCountry(dva.getSoundLibrary("Sydney-Male"), true),
                    new NswCountry(dva.getSoundLibrary("Sydney-Female"), false) };
            }            

            // Populate the comboboxes with them, the second combobox has the
            // additional option of 'None'.
            rendererComboBoxes = new ArrayList<>();
            for (int i = 0; i < 3; i++)
            {
                JComboBox<Drawer> cb = new JComboBox<>();
                if (i > 0) {
                    cb.addItem(new NullDrawer());
                }
                for (Drawer d : renderers) {
                    cb.addItem(d);
                }
                rendererComboBoxes.add(cb);
                XHBox hb = new XHBox();
                hb.add(new JLabel("Monitor " + (i + 1) + " Renderer:"));
                hb.add(cb);
                rendererComboboxesPanel.add(hb);
            }
            if (announcers != null)
            {
                playAnnouncementVoiceCombobox.setModel(new DefaultComboBoxModel<>(announcers));
                playAnnouncementVoiceCombobox.setSelectedIndex(0);
            }

            // Add the DeparturePanels, currently 3 for use with Cityrail
            // indicators.
            for (int i = 0; i < departurePanels.length; i++) {
                departurePanels[i] = new DeparturePanel(departurePanelTitles[i],
                    dva,
                    dva != null ? ((Announcer) playAnnouncementVoiceCombobox.getSelectedItem()).getSoundLibrary().getName() : null);

                if (i >= 1) {
                    JSeparator separator = new JSeparator();
                    separator.setBorder(new EmptyBorder(0, 0, 0, 0));
                    departuresList.add(separator);
                }
                departuresList.add(departurePanels[i]);
            }

            // Populate the timetables combobox
            timetableManager = TimetableManager.getInstance();
            timetable.setModel(timetableManager);

            // When the timetable is changed, update the list of lines
            timetable.addActionListener(e -> {
                TimetableTranslator tt = getActiveTimetable();

                scheduleLine.replaceItems(tt.getLines());
            });

            // When the line is changed, update the list of directions and
            // stations
            scheduleLine.addActionListener(e -> {
                TimetableTranslator tt = getActiveTimetable();

                String line = (String) scheduleLine.getSelectedItem();
                if (line != null) {
                    scheduleDirection.replaceItems(tt.getDirectionsForLine(line));
                }
            });

            // When the direction is changed, update the list of stations
            scheduleDirection.addActionListener(e -> {
                TimetableTranslator tt = getActiveTimetable();

                String line = (String) scheduleLine.getSelectedItem();
                String direction = (String) scheduleDirection.getSelectedItem();
                if (line != null && direction != null) {
                    scheduleStation.replaceItems(tt.getStationsForLineAndDirection(line, direction));
                }
            });

            // Initially select the first line, and show/hide the
            // window/fullscreen/preview buttons depending
            // on whether running in regular mode or screen saver settings mode
            timetable.setSelectedIndex(0);
            tabbedPane.setSelectedIndex(0);
            startButtonsPanel.setVisible(mode == Mode.REGULAR);
            previewButtonPanel.setVisible(mode == Mode.SCREENSAVER);

            // Load the indicators from user preferences. Defaults are in the Settings class.
            setSettings(Settings.getIndicator(settingsKey));

            playAnnouncementVoiceCombobox.addActionListener(e -> {
                for (DeparturePanel departurePanel : departurePanels) {
                    departurePanel.setScriptVoice(((Announcer) playAnnouncementVoiceCombobox.getSelectedItem()).getSoundLibrary().getName());
                }
            });
        } catch (Exception e) {
            jb.common.ExceptionReporter.reportException(e);
        }
    }

    // Run the indicator in screensaver mode
    public static List<PlasmaWindow> screenSaver(boolean preview, Dimension previewSize)
    {
        if (preview)
        {
            final PlasmaUI ui = new PlasmaUI(PlasmaUI.Mode.SCREENSAVER_PREVIEW, null);
            return ui.showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER_PREVIEW_MINI_WINDOW, previewSize);
        }
        else
        {
            final PlasmaUI ui = new PlasmaUI(PlasmaUI.Mode.SCREENSAVER, new DVA(false, false));
            return ui.showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER, null);
        }
    }
    
    public static Calendar inXMinutes(Calendar cal, int minutes)
    {
        Calendar c = (Calendar) cal.clone();
        c.add(Calendar.MINUTE, minutes);
        return c;
    }

    public JPanel getPanel()
    {
        return panel;
    }

    public void setTabIndex(int i)
    {
        tabbedPane.setSelectedIndex(i);
    }

    private List<DepartureData> getDepartureData()
    {
        // If 'manual', get the departure data from what's been entered in the
        // panels.
        // Otherwise ('auto'), use the timetable.
        List<DepartureData> dd = null;
        if (tabbedPane.getSelectedIndex() == 0) {
            try {
                dd = new LinkedList<>();
                for (int i = 0; i < departurePanels.length; i++) {
                    if (i >= dd.size()) {
                        dd.add(new DepartureData());
                    }
                    dd.set(i, departurePanels[i].getData());
                }
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null,
                        "IndexOutOfBoundsException, check entered departure times are valid.");
            }
        } else {
            try {
                dd = getActiveTimetable().getDepartureDataForStation(
                        (String) scheduleLine.getSelectedItem(), (String) scheduleDirection.getSelectedItem(),
                        (String) scheduleStation.getSelectedItem(), Calendar.getInstance(), TimetableTranslator.AtOrAfter.AFTER,
                        (Integer) platformValue.getValue(), (Integer) carsValue.getValue(), 0, true);
            } catch (Exception e) {
                jb.common.ExceptionReporter.reportException(e);
            }
        }
        return dd;
    }
    
    // Show the indicator board, specifying whether to run in full screen,
    // whether to close the
    // indicator windows on an event (e.g. screen saver 'preview', and whether
    // to terminate the application fully
    // on an event (e.g. when running as an actual screen saver).
    public List<PlasmaWindow> showIndicatorBoard(PlasmaWindow.Mode mode, Dimension size)
    {
        departureData = getDepartureData();
        if (departureData == null) return null;

        // Get all graphics devices (screens) and if set to run in full screen,
        // ensure there are enough.
        int maxScreens;
        if (mode.LimitToOneScreen) {
            maxScreens = 1;
        } else if (mode.IsFullScreen) {
            GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (int i = 0; i < rendererComboBoxes.size(); i++) {
                if (!(rendererComboBoxes.get(i).getSelectedItem() instanceof NullDrawer) && i >= screens.length) {
                    JOptionPane.showMessageDialog(null, "Insufficient number of monitors detected.");
                    return null;
                }
            }
            maxScreens = Math.min(screens.length, rendererComboBoxes.size());
        } else {
            maxScreens = rendererComboBoxes.size();
        }

        // Save indicator settings for next time
        Settings.setIndicator(settingsKey, getSettings());

        // Create a window for each renderer selected for use
        List<Drawer> drawers = new LinkedList<>();
        List<PlasmaWindow> windows = new LinkedList<>();
        for (int i = 0; i < maxScreens; i++) {
            JComboBox<Drawer> rendererComboBox = rendererComboBoxes.get(i);
            if (rendererComboBox.getSelectedItem() instanceof NullDrawer)
                continue;

            Drawer d;
            try {
                d = (Drawer) (rendererComboBox.getItemAt(rendererComboBox.getSelectedIndex())).clone();
                drawers.add(d);
                PlasmaPanel p = new PlasmaPanel(d, departureData);
                PlasmaWindow w = new PlasmaWindow(this, mode, i, d.toString(), size, d.getAspectRatio(), new ProportionalPanel(d
                        .getAspectRatio(), p));
                w.paint(w.getGraphics());
                w.setVisible(true);
                windows.add(w);
            } catch (CloneNotSupportedException e) {
                jb.common.ExceptionReporter.reportException(e);
            }
        }

        // Create a session to manage the de-queuing of the list of data when
        // departures pass, and to play
        // annoucements at the specified intervals.
        Runnable announce = null;
        int[] announcementTimes = null;
        if (dva != null)
        {
            String[] announcementTimesStrings = playAnnouncementTimes.getText().split(",");
            announcementTimes = new int[announcementTimesStrings.length];
            try {
                for (int i = 0; i < announcementTimesStrings.length; i++) {
                    announcementTimes[i] = Integer.parseInt(announcementTimesStrings[i].trim());
                }
            } catch (NumberFormatException e) {
                announcementTimes = null;
            }
            
            if (playAnnouncementCheckbox.isSelected())
            {
                announce = this::announce;
            }
        }
        session = new PlasmaSession(windows, announce, departureData, drawers, announcementTimes);
        return windows;
    }

    public void stopSession()
    {
        if (session != null) {
            session.stop();
            session = null;
        }
    }
    
    public void announce()
    {
        if (departureData != null && departureData.size() > 0)
        {
            Player player = null;
            if (departureData.get(0).CustomAnnouncementPath != null)
            {
                try {
                    ArrayList<URL> al = new ArrayList<>();
                    al.add(new File(departureData.get(0).CustomAnnouncementPath).toURI().toURL());
                    player = new Player(al, null);
                    player.start();
                } catch (Exception e) {
                    ExceptionReporter.reportException(e);
                }
            }
            else if (dva != null)
            {
                logger.info("Playing auto generated announcement");
                Announcer announcer = (Announcer) playAnnouncementVoiceCombobox.getSelectedItem();
                long millisToDeparture = departureData.get(0).DueOut.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                long minsToDeparture = TimeUnit.MILLISECONDS.toMinutes(millisToDeparture);
                if (announcer instanceof CityrailStandard)
                {
                    ((CityrailStandard)announcer).setCoalesceStationSequences(coalesceStationSequencesCheckbox.isSelected());
                }
                Script announcement = announcer.createAnnouncement(departureData.get(0), (int)minsToDeparture);
                logger.debug("Generated script: {}", announcement.getScript());
                player = dva.play(null, announcement);
            }
                        
            if (player != null)
            {
                final Player p = player;
                this.player = player;

                stopAction.setEnabled(true);
                playStopButton.setAction(stopAction);
                announceAction.setEnabled(false);
                
                new Thread() {
                    public void run() {
                        try {
                            p.join();
    
                            stopAction.setEnabled(false);
                            announceAction.setEnabled(true);
                            playStopButton.setAction(announceAction);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }.start();
                player.start();
            }
        }        
    }

    private TimetableTranslator getActiveTimetable()
    {
        return new TimetableTranslator((Timetable)timetableManager.getSelectedItem());
    }

    // Get the indicator settings entered into the panels as an
    // IndicatorSettings object
    public IndicatorSettings getSettings()
    {
        List<String> renderers = new LinkedList<>();
        for (JComboBox<Drawer> r : rendererComboBoxes) {
            renderers.add(r.getSelectedItem().toString());
        }
        List<DepartureData> data = new LinkedList<>();
        for (DeparturePanel dp : departurePanels) {
            data.add(dp.getData());
        }

        return new IndicatorSettings(tabbedPane.getSelectedIndex() == 1,
                renderers,
                playAnnouncementCheckbox.isSelected(),
                dva != null ? playAnnouncementTimes.getText() : null,
                dva != null ? ((Announcer) playAnnouncementVoiceCombobox.getSelectedItem()).getName() : null,
                coalesceStationSequencesCheckbox.isSelected(),
                data,
                scheduleLine.getSelectedItem().toString(),
                scheduleDirection.getSelectedItem().toString(),
                scheduleStation.getSelectedItem().toString(),
                (Integer) platformValue.getValue(),
                (Integer) carsValue.getValue());
    }

    // Set the indicator settings in the panels from an IndicatorSettings object
    public void setSettings(IndicatorSettings settings)
    {
        tabbedPane.setSelectedIndex(settings.useSchedule() ? 1 : 0);

        playAnnouncementCheckbox.setSelected(settings.playAnnouncements());
        playAnnouncementTimes.setText(settings.announcementTimes());
        for (int j = 0; j < playAnnouncementVoiceCombobox.getItemCount(); j++) {
            if (playAnnouncementVoiceCombobox.getItemAt(j).toString().equals(settings.announcementVoice())) {
                playAnnouncementVoiceCombobox.setSelectedIndex(j);
            }
        }
        coalesceStationSequencesCheckbox.setSelected(settings.coalesceStationSequences());

        for (int i = 0; i < rendererComboBoxes.size(); i++) {
            JComboBox<Drawer> rendererComboBox = rendererComboBoxes.get(i);
            if (settings.getRenderers().size() > 0 && i < settings.getRenderers().size()) {
                for (int j = 0; j < rendererComboBox.getItemCount(); j++) {
                    if (rendererComboBox.getItemAt(j).toString().equals(settings.getRenderers().get(i))) {
                        rendererComboBox.setSelectedIndex(j);
                    }
                }
            }
        }

        for (int i = 0; i < departurePanels.length; i++) {
            departurePanels[i].setData(settings.getDepartureData().get(i));
        }

        for (int i = 0; i < scheduleLine.getItemCount(); i++)
            if (scheduleLine.getItemAt(i).equals(settings.getLine())) {
                scheduleLine.setSelectedIndex(i);
                break;
            }
        for (int i = 0; i < scheduleDirection.getItemCount(); i++)
            if (scheduleDirection.getItemAt(i).equals(settings.getDirection())) {
                scheduleDirection.setSelectedIndex(i);
                break;
            }
        for (int i = 0; i < scheduleStation.getItemCount(); i++)
            if (scheduleStation.getItemAt(i).equals(settings.getStation())) {
                scheduleStation.setSelectedIndex(i);
                break;
            }
        platformValue.setValue(settings.getPlatform());
        carsValue.setValue(settings.getCars());
    }

    // Show the indicator in windowed mode
    @SuppressWarnings("serial")
    public Action windowAction = new AbstractAction("Open in Window") {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.WINDOWED, null);
        }
    };

    // Show the indicator in fullscreen mode
    @SuppressWarnings("serial")
    public Action fullScreenAction = new AbstractAction("Open in Full Screen") {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.FULLSCREEN, null);
        }
    };

    // Show the indicator in screen saver preview mode
    @SuppressWarnings("serial")
    public Action previewAction = new AbstractAction("Preview") {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER_PREVIEW, null);
        }
    };
    
    // Play an announcement from the indicator
    @SuppressWarnings("serial")
    public Action announceAction = new AbstractAction("Play", new ImageIcon(PlasmaUI.class.getResource("/toolbarButtonGraphics/media/Play24.gif"))) {
        public void actionPerformed(ActionEvent e)
        {
            departureData = getDepartureData();
            announce();
        }
    };
    
    @SuppressWarnings("serial")
    public Action stopAction = new AbstractAction("Stop", new ImageIcon(PlasmaUI.class.getResource("/toolbarButtonGraphics/media/Stop24.gif"))) {
        public void actionPerformed(ActionEvent e) {
            stopAction.setEnabled(false);
            announceAction.setEnabled(true);
            playStopButton.setAction(announceAction);
            player.stopPlaying();
        }
    };

    @SuppressWarnings("serial")
    public Action promoteDepartures = new AbstractAction("Shift Departures Upwards") {
        public void actionPerformed(ActionEvent e)
        {
            if (session != null) {
                session.trainDeparted();
            } else if (departureData != null && departureData.size() > 0) {
                departureData.remove(0);
            }
            for (int i = 0; i < departurePanels.length - 1; i++) {
                departurePanels[i].setData(departurePanels[i + 1].getData());
            }
            if (departureData != null && departureData.size() > 2) {
                departurePanels[departurePanels.length - 1].setData(departureData.get(2));
            } else {
                departurePanels[departurePanels.length - 1].setData(new DepartureData());
            }
        }
    };

    @SuppressWarnings("serial")
    public Action loadManualFromTimetable = new AbstractAction("<< Transfer to Manual Page") {
        public void actionPerformed(ActionEvent e)
        {
            try {
                departureData = getActiveTimetable().getDepartureDataForStation(
                        (String) scheduleLine.getSelectedItem(), (String) scheduleDirection.getSelectedItem(),
                        (String) scheduleStation.getSelectedItem(), Calendar.getInstance(), TimetableTranslator.AtOrAfter.AFTER,
                        (Integer) platformValue.getValue(), (Integer) carsValue.getValue(), 0, true);
                for (int i = 0; i < departurePanels.length; i++) {
                    if (departureData.size() > i) {
                        departurePanels[i].setData(departureData.get(i));
                    }
                }
                tabbedPane.setSelectedIndex(0);
            } catch (Exception ex) {
                jb.common.ExceptionReporter.reportException(ex);
            }
        }
    };
    
    @SuppressWarnings("serial")
    public Action editSubstitutionsAction = new AbstractAction("Edit Substitution List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("substitutions.txt");
        }
    };

    @SuppressWarnings("serial")
    public Action editViasAction = new AbstractAction("Edit \"Via\" List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("vias.txt");
        }
    };

    @SuppressWarnings("serial")
    public Action editAllStationsTosAction = new AbstractAction("Edit \"All Stations To\" List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("allStationsTos.txt");
        }
    };

    public Action downloadTimetableAction = new AbstractAction("Download new timetable") {
        public void actionPerformed(ActionEvent e) {
            Timetable activeTimetable = (Timetable)timetableManager.getSelectedItem();
            String newName = activeTimetable.getName();
            if (newName.indexOf('(') >= 0) {
                newName = newName.substring(newName.indexOf('('));
            }
            newName += new SimpleDateFormat(" (MMM yyyy)").format(Calendar.getInstance().getTime());
            List<String> existingNames = new LinkedList<>();
            for (int i = 0; i < timetableManager.getSize(); i++) {
                existingNames.add(timetableManager.getElementAt(i).getName());
            }
            DownloadTimetableDialog d = new DownloadTimetableDialog(newName, activeTimetable.type, existingNames);
            if (d.accepted()) {
                if (TimetableManager.getTimetablesDir().exists() || TimetableManager.getTimetablesDir().mkdirs())
                {
                    File tf = new File(TimetableManager.getTimetablesDir(), d.timetableName() + ".tt");
                    ProgressAdapter pa = new ProgressAdapter(new ProgressWindow("Download timetable", "Downloading timetable..."), false);
                    pa.show();
                    File parentDir = new File(tf.getParent());
                    if (parentDir.exists() || parentDir.mkdirs()) {
                        String ttName = tf.getName().replaceFirst("[.][^.]+$", "");
                        Generator g = new Generator(System.out, System.err, tf.getPath(), ttName, activeTimetable.type, true, pa);
                        final Thread t = new Thread() {
                            public void run() {
                                try {
                                    Timetable tt = g.run();
                                    if (tt != null) {
                                        timetableManager.addElement(tt);
                                        timetableManager.setSelectedItem(tt);
                                    }
                                } catch (Exception e) {
                                    ExceptionReporter.reportException(e);
                                } finally {
                                    pa.dispose();
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        }
    };

    public Action deleteTimetableAction = new AbstractAction("Delete timetable") {
        public void actionPerformed(ActionEvent e) {
            Timetable activeTimetable = (Timetable)timetableManager.getSelectedItem();
            timetableManager.deleteTimetable(activeTimetable);
        }
    };

    private void launchTextEditor(String filename)
    {
        try
        {
            File jarFolder = new File(PlasmaUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            File f = new File(jarFolder, filename);
            if (SwingEngine.isMacOSX())
            {
                new ProcessBuilder("open", "-a", "TextEdit", f.getPath()).start();
            }
            else                
            {
                new ProcessBuilder("notepad", f.getPath()).start();
                
            }
        }
        catch (URISyntaxException | IOException e) {
            ExceptionReporter.reportException(e);
        }
    }
}