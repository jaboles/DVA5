package jb.plasma;
import java.util.List;

// Class used to store settings on the Indicators page for saving/loading
public class IndicatorSettings
{
    private final List<DepartureData> departureData;
    private final List<String> renderers;
    private final String gtfsStation;
    private final boolean filterPlatform;
    private final String gtfsPlatform;
    private final boolean filterRoute;
    private final String gtfsRoute;
    private final int tabIndex;
    private final boolean playAnnouncements;
    private final String announcementTimes;
    private final String announcementVoice;
    private final boolean coalesceStationSequences;
    private final DepartureData recurringDepartureData;
    private final boolean recurringIntervalSelected;
    private final int recurringInterval;
    private final boolean recurringTimesSelected;
    private final String recurringTimes;
    private final boolean recurringEndSelected;
    private final String recurringEnd;

    public IndicatorSettings(int tabIndex,
                             List<String> renderers,
                             boolean playAnnouncements,
                             String announcementTimes,
                             String announcementVoice,
                             boolean coalesceStationSequences,
                             List<DepartureData> departureData,
                             DepartureData recurringDepartureData,
                             boolean recurringIntervalSelected,
                             int recurringInterval,
                             boolean recurringTimesSelected,
                             String recurringTimes,
                             boolean recurringEndSelected,
                             String recurringEnd,
                             String gtfsStation,
                             boolean filterPlatform,
                             String gtfsPlatform,
                             boolean filterRoute,
                             String gtfsRoute)
    {
        this.tabIndex = tabIndex;
        this.departureData = departureData;
        this.playAnnouncements = playAnnouncements;
        this.announcementTimes = announcementTimes;
        this.announcementVoice = announcementVoice;
        this.coalesceStationSequences = coalesceStationSequences;
        this.renderers = renderers;
        this.gtfsStation = gtfsStation;
        this.filterPlatform = filterPlatform;
        this.gtfsPlatform = gtfsPlatform;
        this.filterRoute = filterRoute;
        this.gtfsRoute = gtfsRoute;
        this.recurringDepartureData = recurringDepartureData;
        this.recurringIntervalSelected = recurringIntervalSelected;
        this.recurringInterval = recurringInterval;
        this.recurringTimesSelected = recurringTimesSelected;
        this.recurringTimes = recurringTimes;
        this.recurringEndSelected = recurringEndSelected;
        this.recurringEnd = recurringEnd;
    }

    public List<DepartureData> getDepartureData() { return departureData; }

    public int getTabIndex() { return tabIndex; }

    public boolean playAnnouncements() { return playAnnouncements; }

    public String announcementTimes() { return announcementTimes; }

    public String announcementVoice() { return announcementVoice; }

    public boolean coalesceStationSequences() { return coalesceStationSequences; }

    public List<String> getRenderers() { return renderers; }

    public DepartureData getRecurringDepartureData() { return recurringDepartureData; }

    public boolean getRecurringIntervalSelected() { return recurringEndSelected; }

    public int getRecurringInterval() { return recurringInterval; }

    public boolean getRecurringTimesSelected() { return recurringTimesSelected; }

    public String getRecurringTimes() { return recurringTimes; }

    public boolean getRecurringEndSelected() { return recurringEndSelected; }

    public String getRecurringEnd() { return recurringEnd; }

    public String getGtfsStation() { return gtfsStation; }

    public boolean filterPlatform() { return filterPlatform; }

    public String getGtfsPlatform() { return gtfsPlatform; }

    public boolean filterRoute() { return filterRoute; }

    public String getGtfsRoute() { return gtfsRoute; }
}
