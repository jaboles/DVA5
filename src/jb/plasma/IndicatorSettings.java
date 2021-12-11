package jb.plasma;
import java.util.List;

// Class used to store settings on the Indicators page for saving/loading
public class IndicatorSettings
{
    private List<DepartureData> departureData;
    private List<String> renderers;
    private String gtfsStation;
    private boolean filterPlatform;
    private String gtfsPlatform;
    private boolean filterRoute;
    private String gtfsRoute;
    private boolean useSchedule;
    private boolean playAnnouncements;
    private String announcementTimes;
    private String announcementVoice;
    private boolean coalesceStationSequences;

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
