package jb.plasma.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.sound.Player;
import jb.common.ui.*;
import jb.dva.DVAManager;
import jb.dva.Script;
import jb.dva.SoundLibrary;
import jb.dvacommon.Settings;
import jb.dvacommon.ui.ProgressWindow;
import jb.dvacommon.ui.ThemedFlatSVGIcon;
import jb.plasma.*;
import jb.plasma.announcers.CityrailStandard;
import jb.plasma.announcers.NswCountry;
import jb.plasma.gtfs.GtfsGenerator;
import jb.plasma.gtfs.GtfsTimetableTranslator;
import jb.plasma.gtfs.Stop;
import jb.plasma.renderers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.swixml.SwingEngine;
import org.swixml.XHBox;
import org.swixml.XVBox;

import static com.formdev.flatlaf.FlatClientProperties.*;

// The main CityRail Indicators application. It is usually hosted inside the DVA application, but
// can exist outside of it, which is used by the screen saver mode.
public class PlasmaUI
{
    public static class Mode
    {
        public static final int REGULAR = 0;
        public static final int SCREENSAVER = 1;
        public static final int SCREENSAVER_PREVIEW = 2;
    }

    private static final String[] departurePanelTitles = new String[] { "Next Train:", "2nd Train:", "3rd Train:" };
    final static Logger logger = LogManager.getLogger(PlasmaUI.class);

    private final DVAManager dva;
    private final String settingsKey;
    private PlasmaSession session;
    private Player player;
    private List<DepartureData> departureData = new LinkedList<>();
    private GtfsTimetableTranslator timetableTranslator;
    private final File temp;

    private Container panel;
    @SuppressWarnings("UnusedDeclaration") private JTabbedPane tabbedPane;

    // Settings section
    @SuppressWarnings("UnusedDeclaration") private XVBox rendererComboboxesPanel;
    @SuppressWarnings("UnusedDeclaration") private List<JComboBox<Drawer>> rendererComboBoxes;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox playAnnouncementCheckbox;
    @SuppressWarnings("UnusedDeclaration") private JTextField playAnnouncementTimes;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<Announcer> playAnnouncementVoiceCombobox;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox coalesceStationSequencesCheckbox;
    @SuppressWarnings("UnusedDeclaration") private JPanel startButtonsPanel;
    @SuppressWarnings("UnusedDeclaration") private JPanel previewButtonPanel;

    // Manual section
    private final DeparturePanel[] departurePanels = new DeparturePanel[3];
    @SuppressWarnings("UnusedDeclaration") private XVBox departuresList;
    @SuppressWarnings("UnusedDeclaration") private JButton playStopButton;
    @SuppressWarnings("UnusedDeclaration") private JButton promoteDeparturesButton;

    // Timetables section
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsInfo;
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsDownloadTimestamp;
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsExpiryTime;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<Stop> gtfsStation;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox filterPlatform;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<Stop> gtfsPlatform;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox filterRoute;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<String> gtfsRoute;

    public PlasmaUI(int mode, DVAManager dvaManager, File temp) {
        this.dva = dvaManager;
        this.settingsKey = (mode == Mode.SCREENSAVER || mode == Mode.SCREENSAVER_PREVIEW) ? "screenSaver" : "remembered";
        this.temp = temp;

        SwingEngine renderer = new SwingEngine(this);
        renderer.getTaglib().registerTag("colorcombobox", ColorComboBox.class);
        renderer.getTaglib().registerTag("jbcombobox", JBComboBox.class);
        renderer.getTaglib().registerTag("filler", Filler.class);
        try {
            panel = renderer.render(PlasmaUI.class.getResource("/jb/plasma/ui/resources/ui.xml"));
            TextIcon ti = new TextIcon(promoteDeparturesButton, "     <<< Shift Departures Upwards <<<    ");
            ti.setFont(new JLabel().getFont());
            RotatedIcon ri = new RotatedIcon(ti, RotatedIcon.Rotate.DOWN);
            promoteDeparturesButton.setIcon(ri);
            tabbedPane.putClientProperty(TABBED_PANE_TAB_AREA_ALIGNMENT, TABBED_PANE_ALIGN_CENTER);

            // Instantiate the available renderers and announcers
            Drawer[] renderers = new Drawer[] {
                new CityrailV5Portrait(),
                new CityrailV5Primary(), new CityrailV5Secondary(),
                new CityrailV4Portrait(false),
                new CityrailV4Portrait(true),
                new CityrailV4Primary(), new CityrailV4Secondary(),
                new CityrailV3Primary(), new CityrailV3Secondary(),
                new CityrailV2(false), new CityrailV1Portrait(true, true), new CityrailV1Landscape(true, false),
                new CityrailV1Portrait(false, true), new CityrailV1Landscape(false, false),
                new OlympicParkLED() };

            Announcer[] announcers = new Announcer[] {
                new CityrailStandard("Sydney-Male", true),
                new CityrailStandard("Sydney-Female", false),
                new NswCountry("Sydney-Male", true),
                new NswCountry("Sydney-Female", false)
            };

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
                cb.setRenderer(new PlasmaRendererListCellRenderer());
                rendererComboBoxes.add(cb);
                XHBox hb = new XHBox();
                hb.add(new JLabel("Monitor " + (i + 1) + " Renderer:"));
                hb.add(cb);
                if (i > 0) rendererComboboxesPanel.add(Box.createVerticalStrut(2));
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
                    dva != null ? (playAnnouncementVoiceCombobox.getSelectedItemTyped()).getSoundLibrary() : null);

                if (i >= 1) {
                    JSeparator separator = new JSeparator();
                    separator.setBorder(new EmptyBorder(0, 0, 0, 0));
                    departuresList.add(separator);
                }
                departuresList.add(departurePanels[i].getPanel());
            }

            timetableTranslator = GtfsTimetableTranslator.getInstance();
            gtfsInfo.setText("TfNSW GTFS timetable");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            gtfsDownloadTimestamp.setText("Downloaded: " + timetableTranslator.downloadTimestamp().format(fmt));
            gtfsExpiryTime.setText("Expiry & next download: " + timetableTranslator.expiryTime().format(fmt));

            gtfsStation.replaceItems(timetableTranslator.getStations());

            // When the station is changed, update the list of platforms and routes
            ActionListener gtfsStationChanged = e -> {
                Stop station = gtfsStation.getSelectedItemTyped();

                if (station != null) {
                    gtfsPlatform.replaceItems(timetableTranslator.getPlatformsForStation(station));
                    gtfsRoute.replaceItems(timetableTranslator.getRoutesForStation(station));
                }
            };
            gtfsStationChanged.actionPerformed(null);
            gtfsStation.addActionListener(gtfsStationChanged);

            // Initially select the first line, and show/hide the
            // window/fullscreen/preview buttons depending
            // on whether running in regular mode or screen saver settings mode
            tabbedPane.setSelectedIndex(0);
            startButtonsPanel.setVisible(mode == Mode.REGULAR);
            previewButtonPanel.setVisible(mode == Mode.SCREENSAVER);

            // Load the indicators from user preferences. Defaults are in the Settings class.
            setSettings(Settings.getIndicator(settingsKey));

            playAnnouncementVoiceCombobox.addActionListener(e -> {
                for (DeparturePanel departurePanel : departurePanels) {
                    departurePanel.setScriptVoice((playAnnouncementVoiceCombobox.getSelectedItemTyped()).getSoundLibrary());
                }
            });
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    public Container getPanel()
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
                for (DeparturePanel departurePanel : departurePanels) {
                    dd.add(departurePanel.getData());
                }
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null,
                        "IndexOutOfBoundsException, check entered departure times are valid.");
            }
            return dd;
        } else {
            return getTimetableDepartureData();
        }
    }

    private List<DepartureData> getTimetableDepartureData()
    {
        List<DepartureData> result = null;
        ProgressWindow pw = new ProgressWindow("Progress", "Scanning timetable data");
        try {
            pw.setProgressBarMaximum(100);
            pw.setProgressText("Filtering timepoint data to location " + gtfsStation.getSelectedItemTyped().Name);
            pw.show();
            pw.repaint();

            result = timetableTranslator.getDepartureDataForStation(
                    gtfsStation.getSelectedItemTyped(),
                    filterPlatform.isSelected() ? gtfsPlatform.getSelectedItemTyped() : null,
                    filterRoute.isSelected()    ? gtfsRoute.getSelectedItemTyped()    : null,
                    0,
                    progress -> {
                        pw.setValue((int)(progress.getValue0() * 100));
                        pw.setProgressText(progress.getValue1());
                        pw.repaint();
                    })
                    .collect(Collectors.toList());

            pw.dispose();
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        } finally {
            pw.dispose();
        }
        return result;
    }

    // Show the indicator board, specifying whether to run in full screen,
    // whether to close the
    // indicator windows on an event (e.g. screen saver 'preview', and whether
    // to terminate the application fully
    // on an event (e.g. when running as an actual screen saver).
    public List<Window> showIndicatorBoard(PlasmaWindow.Mode mode, Dimension size)
    {
        departureData = getDepartureData();
        if (departureData == null) return null;

        if (departureData.size() > 0)
        {
            departureData.get(0).logDetails();
        }

        for (DepartureData d : departureData)
        {
            if (d != null && d.CustomAnnouncementPath != null && !d.CustomAnnouncementPath.isEmpty()) {
                if (!(new File(d.CustomAnnouncementPath).exists())) {
                    JOptionPane.showMessageDialog(null, "File '" + d.CustomAnnouncementPath + "' not found.");
                    return null;
                }
            }
        }

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
        List<Window> windows = new LinkedList<>();
        for (int i = 0; i < maxScreens; i++) {
            JComboBox<Drawer> rendererComboBox = rendererComboBoxes.get(i);
            if (rendererComboBox.getSelectedItem() instanceof NullDrawer)
                continue;

            Drawer d;
            try {
                d = (Drawer) (rendererComboBox.getItemAt(rendererComboBox.getSelectedIndex())).clone();
                drawers.add(d);
                d.dataChanged(departureData);
                PlasmaPanel p = new PlasmaPanel(d);
                PlasmaWindow w = new PlasmaWindow(this, mode, i, d.toString(), size, d.getAspectRatio(), new ProportionalPanel(d
                        .getAspectRatio(), p, Color.black));
                w.paint(w.getGraphics());
                w.setVisible(true);
                windows.add(w);
            } catch (CloneNotSupportedException e) {
                ExceptionReporter.reportException(e);
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
                    player = new Player(al, null, temp);
                    player.start();
                } catch (Exception e) {
                    ExceptionReporter.reportException(e);
                }
            }
            else if (dva != null)
            {
                logger.info("Playing auto generated announcement");
                Announcer announcer = playAnnouncementVoiceCombobox.getSelectedItemTyped();
                long minsToDeparture = ChronoUnit.MINUTES.between(LocalDateTime.now(), departureData.get(0).DueOut);
                if (announcer instanceof CityrailStandard)
                {
                    ((CityrailStandard)announcer).setCoalesceStationSequences(coalesceStationSequencesCheckbox.isSelected());
                }
                Script announcement = new Script(announcer.getSoundLibrary(), announcer.createAnnouncementText(departureData.get(0), (int)minsToDeparture));
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

                new Thread(() -> {
                    try {
                        p.join();

                        stopAction.setEnabled(false);
                        announceAction.setEnabled(true);
                        playStopButton.setAction(announceAction);
                    } catch (InterruptedException ignored) {
                    }
                }).start();
                player.start();
            }
        }
    }

    // Get the indicator settings entered into the panels as an
    // IndicatorSettings object
    public IndicatorSettings getSettings()
    {
        List<String> renderers = new LinkedList<>();
        for (JComboBox<Drawer> r : rendererComboBoxes) {
            if (r.getSelectedItem() != null)
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
                dva != null ? (playAnnouncementVoiceCombobox.getSelectedItemTyped()).getName() : null,
                coalesceStationSequencesCheckbox.isSelected(),
                data,
                gtfsStation.getSelectedItemTyped().toString(),
                filterPlatform.isSelected(),
                gtfsPlatform.getSelectedItemTyped().toString(),
                filterRoute.isSelected(),
                gtfsRoute.getSelectedItemTyped());
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

        for (int i = 0; i < gtfsStation.getItemCount(); i++)
            if (gtfsStation.getItemAt(i).toString().equals(settings.getGtfsStation())) {
                gtfsStation.setSelectedIndex(i);
                break;
            }
        filterPlatform.setSelected(settings.filterPlatform());
        gtfsPlatform.setEnabled(filterPlatform.isSelected());
        for (int i = 0; i < gtfsPlatform.getItemCount(); i++)
            if (gtfsPlatform.getItemAt(i).toString().equals(settings.getGtfsPlatform())) {
                gtfsPlatform.setSelectedIndex(i);
                break;
            }
        filterRoute.setSelected(settings.filterRoute());
        gtfsRoute.setEnabled(filterRoute.isSelected());
        for (int i = 0; i < gtfsRoute.getItemCount(); i++)
            if (gtfsRoute.getItemAt(i).equals(settings.getGtfsRoute())) {
                gtfsRoute.setSelectedIndex(i);
                break;
            }
    }

    // Show the indicator in windowed mode
    public final Action windowAction = new AbstractAction("Open in Window", new ThemedFlatSVGIcon("showwindow")) {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.WINDOWED, null);
        }
    };

    // Show the indicator in fullscreen mode
    public final Action fullScreenAction = new AbstractAction("Open in Full Screen", new ThemedFlatSVGIcon("show")) {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.FULLSCREEN, null);
        }
    };

    // Show the indicator in screen saver preview mode
    @SuppressWarnings("unused")
    public Action previewAction = new AbstractAction("Preview") {
        public void actionPerformed(ActionEvent e)
        {
            showIndicatorBoard(PlasmaWindow.Mode.SCREENSAVER_PREVIEW, null);
        }
    };

    @SuppressWarnings("unused")
    public Action clearDownloadedGtfsAction = new AbstractAction("Clear") {
        public void actionPerformed(ActionEvent e) {
            try {
                GtfsGenerator.getInstance().delete();
                JOptionPane.showMessageDialog(panel, "Data deleted. It will be downloaded again at next restart.");
            } catch (Exception ex) {
                ExceptionReporter.reportException(ex);
            }
        }
    };

    @SuppressWarnings("unused")
    public Action filterPlatformAction = new AbstractAction("Filter") {
        public void actionPerformed(ActionEvent e) { gtfsPlatform.setEnabled(filterPlatform.isSelected()); }
    };

    @SuppressWarnings("unused")
    public Action filterRouteAction = new AbstractAction("Filter") {
        public void actionPerformed(ActionEvent e) { gtfsRoute.setEnabled(filterRoute.isSelected()); }
    };

    // Play an announcement from the indicator
    @SuppressWarnings("unused")
    public Action announceAction = new AbstractAction("Play", new ThemedFlatSVGIcon("play")) {
        public void actionPerformed(ActionEvent e)
        {
            departureData = getDepartureData();
            for (DepartureData d : departureData)
            {
                if (d != null && d.CustomAnnouncementPath != null && !d.CustomAnnouncementPath.isEmpty()) {
                    if (!(new File(d.CustomAnnouncementPath).exists())) {
                        JOptionPane.showMessageDialog(null, "File '" + d.CustomAnnouncementPath + "' not found.");
                        return;
                    }
                }
            }

            announce();
        }
    };

    public final Action stopAction = new AbstractAction("Stop", new ThemedFlatSVGIcon("stop")) {
        public void actionPerformed(ActionEvent e) {
            stopAction.setEnabled(false);
            announceAction.setEnabled(true);
            playStopButton.setAction(announceAction);
            player.stopPlaying();
        }
    };

    @SuppressWarnings("unused")
    public final Action promoteDepartures = new AbstractAction("Shift Departures Upwards") {
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
                departurePanels[departurePanels.length - 1].setData(new NullDepartureData());
            }
        }
    };

    @SuppressWarnings("unused")
    public final Action loadManualFromTimetable = new AbstractAction("<< Transfer to Manual Page") {
        public void actionPerformed(ActionEvent e)
        {
            try {
                departureData = getTimetableDepartureData();
                for (int i = 0; i < departurePanels.length; i++) {
                    if (departureData != null && departureData.size() > i) {
                        departurePanels[i].setData(departureData.get(i));
                    }
                }
                tabbedPane.setSelectedIndex(0);
            } catch (Exception ex) {
                ExceptionReporter.reportException(ex);
            }
        }
    };

    public final Action editSubstitutionsAction = new AbstractAction("Edit Substitution List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("substitutions.txt");
        }
    };

    public final Action editViasAction = new AbstractAction("Edit \"Via\" List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("vias.txt");
        }
    };

    public final Action editAllStationsTosAction = new AbstractAction("Edit \"All Stations To\" List") {
        public void actionPerformed(ActionEvent e) {
            launchTextEditor("allStationsTos.txt");
        }
    };

    private void launchTextEditor(String filename)
    {
        try
        {
            File f = new File(FileUtilities.getJarFolder(PlasmaUI.class), filename);
            if (SwingEngine.isMacOSX())
            {
                new ProcessBuilder("open", "-a", "TextEdit", f.getPath()).start();
            }
            else
            {
                new ProcessBuilder("notepad", f.getPath()).start();

            }
        }
        catch (IOException e) {
            ExceptionReporter.reportException(e);
        }
    }
}