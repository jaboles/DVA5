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
import javax.swing.table.TableColumnModel;

import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.sound.Player;
import jb.common.ui.*;
import jb.dva.DVAManager;
import jb.dva.Script;
import jb.dvacommon.Settings;
import jb.dvacommon.ui.ProgressWindow;
import jb.dvacommon.ui.ThemedFlatSVGIcon;
import jb.plasma.*;
import jb.plasma.announcers.*;
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
    @SuppressWarnings("UnusedDeclaration") private JButton updateIndicatorsButton;
    @SuppressWarnings("UnusedDeclaration") private JButton promoteDeparturesButton;

    // Recurring section
    @SuppressWarnings("UnusedDeclaration") private DeparturePanel recurringDeparturePanel;
    @SuppressWarnings("UnusedDeclaration") private JRadioButton recurringIntervalRadioButton;
    @SuppressWarnings("UnusedDeclaration") private XVBox recurringDeparture;
    @SuppressWarnings("UnusedDeclaration") private JSpinner recurringIntervalValue;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox recurringEndCheckbox;
    @SuppressWarnings("UnusedDeclaration") private JTextField recurringEndValue;
    @SuppressWarnings("UnusedDeclaration") private JButton playStopButton2;
    @SuppressWarnings("UnusedDeclaration") private JButton updateIndicatorsButton2;
    @SuppressWarnings("UnusedDeclaration") private JRadioButton recurringTimesRadioButton;
    @SuppressWarnings("UnusedDeclaration") private JTextField recurringTimesValue;

    // Timetables section
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsInfo;
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsDownloadTimestamp;
    @SuppressWarnings("UnusedDeclaration") private JLabel gtfsExpiryTime;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<Stop> gtfsStation;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox filterPlatform;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<Stop> gtfsPlatform;
    @SuppressWarnings("UnusedDeclaration") private JCheckBox filterRoute;
    @SuppressWarnings("UnusedDeclaration") private JBComboBox<String> gtfsRoute;
    @SuppressWarnings("UnusedDeclaration") private JTable gtfsTable;

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

            ButtonGroup recurringRadioButtons = new ButtonGroup();
            recurringRadioButtons.add(recurringIntervalRadioButton);
            recurringRadioButtons.add(recurringTimesRadioButton);

            // Instantiate the available renderers and announcers
            Drawer[] renderers = new Drawer[] {
                new CityrailV5Portrait(CityrailV4and5.Orange),
                new CityrailV5Primary(CityrailV4and5.Orange, null), new CityrailV5Secondary(CityrailV4and5.Orange),
                new CityrailV4Portrait(false, CityrailV4and5.Orange),
                new CityrailV4Portrait(true, CityrailV4and5.Orange),
                new CityrailV4Primary(CityrailV4and5.Orange), new CityrailV4Secondary(CityrailV4and5.Orange),
                new CityrailV3Primary(), new CityrailV3Secondary(),
                new CityrailV2(false), new CityrailV1Portrait(true, true), new CityrailV1Landscape(true, false),
                new CityrailV1Portrait(false, true), new CityrailV1Landscape(false, false),
                new OlympicParkLED(),
                new CityrailV5Primary(CityrailLine.busesBlue, "Sydney Bus Museum") };

            Announcer[] announcers = new Announcer[] {
                new CityrailStandard("Sydney-Male", true),
                new CityrailStandard("Sydney-Female", false),
                new NswCountry("Sydney-Male", true),
                new NswCountry("Sydney-Female", false),
                new SydneyBusMuseum()
            };

            // Populate the comboboxes with them, the second combobox has the
            // additional option of 'None'.
            rendererComboBoxes = new ArrayList<>();
            for (int i = 0; i < 3; i++)
            {
                JComboBox<Drawer> cb = new JComboBox<>();
                cb.addItem(new NullDrawer());
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

            playAnnouncementVoiceCombobox.setModel(new DefaultComboBoxModel<>(announcers));
            playAnnouncementVoiceCombobox.setSelectedIndex(0);

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

            recurringDeparturePanel = new DeparturePanel("All Trains:",
                    dva,
                    dva != null ? (playAnnouncementVoiceCombobox.getSelectedItemTyped()).getSoundLibrary() : null);
            recurringDeparture.add(recurringDeparturePanel.getPanel(), 0);

            ActionListener recurringEndCheckboxChanged = e -> {
                recurringEndValue.setEnabled(recurringEndCheckbox.isSelected() && recurringIntervalRadioButton.isSelected());
            };
            recurringEndCheckboxChanged.actionPerformed(null);
            recurringEndCheckbox.addActionListener(recurringEndCheckboxChanged);

            ActionListener recurringRadioButtonChanged = e -> {
                recurringIntervalValue.setEnabled(recurringIntervalRadioButton.isSelected());
                recurringEndCheckbox.setEnabled(recurringIntervalRadioButton.isSelected());
                recurringEndValue.setEnabled(recurringEndCheckbox.isSelected() && recurringIntervalRadioButton.isSelected());
                recurringTimesValue.setEnabled(recurringTimesRadioButton.isSelected());
            };
            recurringRadioButtonChanged.actionPerformed(null);
            recurringIntervalRadioButton.addActionListener(recurringRadioButtonChanged);
            recurringTimesRadioButton.addActionListener(recurringRadioButtonChanged);

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
            startButtonsPanel.setVisible(mode == Mode.REGULAR);
            previewButtonPanel.setVisible(mode == Mode.SCREENSAVER);

            // Load the indicators from user preferences. Defaults are in the Settings class.
            setSettings(Settings.getIndicator(settingsKey));

            playAnnouncementVoiceCombobox.addActionListener(e -> {
                for (DeparturePanel departurePanel : departurePanels) {
                    departurePanel.setScriptVoice((playAnnouncementVoiceCombobox.getSelectedItemTyped()).getSoundLibrary());
                }
                recurringDeparturePanel.setScriptVoice((playAnnouncementVoiceCombobox.getSelectedItemTyped()).getSoundLibrary());
            });

            gtfsTable.setModel(new GtfsTableModel(new ArrayList<GtfsDepartureData>()));
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
        } else if (tabbedPane.getSelectedIndex() == 1) {
            try {
                LocalDateTime start = recurringDeparturePanel.getData().DueOut;
                LocalDateTime end;
                if (recurringEndCheckbox.isSelected()) {
                    String[] values = recurringEndValue.getText().split(":");
                    int h = Integer.parseInt(values[0]);
                    int m = Integer.parseInt(values[1]);
                    end = LocalDateTime.now()
                            .withHour(h)
                            .withMinute(m)
                            .withSecond(40);
                } else {
                    end = start.plusYears(1);
                }

                dd = new LinkedList<>();
                if (recurringIntervalRadioButton.isSelected())
                {
                    for (LocalDateTime t = start; t.compareTo(end) <= 0; t = t.plusMinutes((Integer)recurringIntervalValue.getValue()))
                    {
                        DepartureData d = recurringDeparturePanel.getData();
                        d.DueOut = t;
                        dd.add(d);
                    }
                }
                else if (recurringTimesRadioButton.isSelected())
                {
                    String[] timeStrs = recurringTimesValue.getText().split(",");
                    LocalDateTime now = LocalDateTime.now();
                    for (String timeStr : timeStrs)
                    {
                        int h = Integer.parseInt(timeStr.trim().split(":")[0]);
                        int m = Integer.parseInt(timeStr.trim().split(":")[1]);
                        LocalDateTime t = now.withHour(h).withMinute(m);
                        if (t.isAfter(now))
                        {
                            DepartureData d = recurringDeparturePanel.getData();
                            d.DueOut = t;
                            dd.add(d);
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,
                        "NumberFormatException: " + ex.getMessage() + ". Check entered departure times are valid.");
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null,
                        "IndexOutOfBoundsException: " + ex.getMessage() + ". Check entered departure times are valid.");
            }
            return dd;
        } else {
            return new ArrayList<>(getTimetableDepartureData());
        }
    }

    private List<GtfsDepartureData> getTimetableDepartureData()
    {
        List<GtfsDepartureData> result = null;
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
    public List<PlasmaWindow> showIndicatorBoard(PlasmaWindow.Mode mode, Dimension size)
    {
        stopSession();

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
        List<PlasmaWindow> windows = new LinkedList<>();
        for (int i = 0; i < maxScreens; i++) {
            JComboBox<Drawer> rendererComboBox = rendererComboBoxes.get(i);
            if (rendererComboBox.getSelectedItem() instanceof NullDrawer)
                continue;

            Drawer d;
            try {
                d = (Drawer) (rendererComboBox.getItemAt(rendererComboBox.getSelectedIndex())).clone();
                drawers.add(d);
                d.dataChanged(departureData);
                PlasmaPanel p = new PlasmaPanel(d, true);

                ArrayList<GraphicsDevice> graphicsDevices = new ArrayList<>();
                graphicsDevices.add(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
                for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    if (gd != GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()) {
                        graphicsDevices.add(gd);
                    }
                }

                GraphicsDevice gd = mode.IsFullScreen ? graphicsDevices.get(i) : null;
                Dimension aspectRatio = Drawer.convertAspectRatio(d.getAspectRatio(), gd != null ? gd.getDisplayMode() : null);
                PlasmaWindow w = new PlasmaWindow(this, mode, gd, d.toString(), size, aspectRatio,
                        new ProportionalPanel(aspectRatio, p, Color.black));
                w.paint(w.getGraphics());
                w.setVisible(true);
                windows.add(w);

                if (mode.IsFullScreen && i > 0 && rendererComboBoxes.get(0).getSelectedItem() instanceof NullDrawer) {
                    // Full screen mode with main window still visible - add operator preview window
                    d = (Drawer) (rendererComboBox.getItemAt(rendererComboBox.getSelectedIndex())).clone();
                    drawers.add(d);
                    d.dataChanged(departureData);
                    p = new PlasmaPanel(d, true);
                    int width = 300;
                    int height = 200;
                    if (aspectRatio.getWidth() > aspectRatio.getHeight()) {
                        height = (int)(width * aspectRatio.getHeight() / aspectRatio.getWidth());
                    } else {
                        width = (int)(height * aspectRatio.getWidth() / aspectRatio.getHeight());
                    }
                    w = new PlasmaWindow(this, PlasmaWindow.Mode.FULLSCREEN_PREVIEW, null, "Preview - " + d, new Dimension(width, height), aspectRatio,
                            new ProportionalPanel(aspectRatio, p, Color.black));
                    w.paint(w.getGraphics());
                    w.setVisible(true);
                    w.pack();
                    windows.add(w);
                }
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

            announce = this::announce;
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
        if (playAnnouncementCheckbox.isSelected() && departureData != null && departureData.size() > 0)
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
                playStopButton2.setAction(stopAction);
                announceAction.setEnabled(false);

                new Thread(() -> {
                    try {
                        p.join();

                        stopAction.setEnabled(false);
                        announceAction.setEnabled(true);
                        playStopButton.setAction(announceAction);
                        playStopButton2.setAction(announceAction);
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

        return new IndicatorSettings(tabbedPane.getSelectedIndex(),
                renderers,
                playAnnouncementCheckbox.isSelected(),
                dva != null ? playAnnouncementTimes.getText() : null,
                dva != null ? (playAnnouncementVoiceCombobox.getSelectedItemTyped()).getName() : null,
                coalesceStationSequencesCheckbox.isSelected(),
                data,
                recurringDeparturePanel.getData(),
                recurringIntervalRadioButton.isSelected(),
                (Integer)recurringIntervalValue.getValue(),
                recurringTimesRadioButton.isSelected(),
                recurringTimesValue.getText(),
                recurringEndCheckbox.isSelected(),
                recurringEndValue.getText(),
                gtfsStation.getSelectedItemTyped().toString(),
                filterPlatform.isSelected(),
                gtfsPlatform.getSelectedItemTyped().toString(),
                filterRoute.isSelected(),
                gtfsRoute.getSelectedItemTyped());
    }

    // Set the indicator settings in the panels from an IndicatorSettings object
    public void setSettings(IndicatorSettings settings)
    {
        tabbedPane.setSelectedIndex(settings.getTabIndex());

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

        var recurringDepartureData = settings.getRecurringDepartureData();
        // Next departure set to multiple of interval minutes
        LocalDateTime now = LocalDateTime.now();
        recurringDepartureData.DueOut = now.withMinute(0).plusMinutes((((long)now.getMinute() / settings.getRecurringInterval() + 1) * settings.getRecurringInterval()));
        recurringDeparturePanel.setData(recurringDepartureData);
        recurringIntervalRadioButton.setSelected(settings.getRecurringIntervalSelected());
        recurringIntervalValue.setValue(settings.getRecurringInterval());
        recurringTimesRadioButton.setSelected(settings.getRecurringTimesSelected());
        recurringTimesValue.setText(settings.getRecurringTimes());
        recurringEndCheckbox.setSelected(settings.getRecurringEndSelected());
        recurringEndValue.setText(settings.getRecurringEnd());

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

    @SuppressWarnings("UnusedDeclaration") public Action updateIndicatorsAction = new AbstractAction("Refresh Indicators", new ThemedFlatSVGIcon("refresh")) {
        public void actionPerformed(ActionEvent e)
        {
            departureData = getDepartureData();
            if (session != null) {
                session.dataChanged(departureData);
            }
        }
    };

    public final Action stopAction = new AbstractAction("Stop", new ThemedFlatSVGIcon("stop")) {
        public void actionPerformed(ActionEvent e) {
            stopAction.setEnabled(false);
            announceAction.setEnabled(true);
            playStopButton.setAction(announceAction);
            playStopButton2.setAction(announceAction);
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
                departureData = new ArrayList<>(getTimetableDepartureData());
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

    private static final int[] GtfsColumnWidths = {110, 60, 50, 100, 200, 50, 100, 530, 100};
    @SuppressWarnings("UnusedDeclaration") public Action previewGtfsAction = new AbstractAction("Examine GTFS Data", new ThemedFlatSVGIcon("find")) {
        public void actionPerformed(ActionEvent e)
        {
            List<GtfsDepartureData> dd = getTimetableDepartureData();
            gtfsTable.setModel(new GtfsTableModel(dd));
            GtfsTableCellRenderer renderer = new GtfsTableCellRenderer(dd);
            TableColumnModel tcm = gtfsTable.getColumnModel();
            for (int i = 0; i < tcm.getColumnCount(); i++) {
                tcm.getColumn(i).setPreferredWidth(GtfsColumnWidths[i]);
                tcm.getColumn(i).setCellRenderer(renderer);
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