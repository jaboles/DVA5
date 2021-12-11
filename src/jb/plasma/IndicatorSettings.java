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
    private final boolean useSchedule;
    private final boolean playAnnouncements;
    private final String announcementTimes;
    private final String announcementVoice;
    private final boolean coalesceStationSequences;

    public IndicatorSettings(boolean useSchedule,
                             List<String> renderers,
                             boolean playAnnouncements,
                             String announcementTimes,
                             String announcementVoice,
                             boolean coalesceStationSequences,
                             List<DepartureData> departureData,
                             String gtfsStation,
                             boolean filterPlatform,
                             String gtfsPlatform,
                             boolean filterRoute,
                             String gtfsRoute)
    {
        this.useSchedule = useSchedule;
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
    }

    public List<DepartureData> getDepartureData() { return departureData; }

    public boolean useSchedule() { return useSchedule; }

    public boolean playAnnouncements() { return playAnnouncements; }

    public String announcementTimes() { return announcementTimes; }

    public String announcementVoice() { return announcementVoice; }
    
    public boolean coalesceStationSequences() { return coalesceStationSequences; }

    public List<String> getRenderers() { return renderers; }

    public String getGtfsStation() { return gtfsStation; }

    public boolean filterPlatform() { return filterPlatform; }

    public String getGtfsPlatform() { return gtfsPlatform; }

    public boolean filterRoute() { return filterRoute; }

    public String getGtfsRoute() { return gtfsRoute; }
}
