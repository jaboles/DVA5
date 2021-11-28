package jb.plasma;
import java.util.List;

// Class used to store settings on the Indicators page for saving/loading
public class IndicatorSettings
{
    private List<DepartureData> departureData;
    private List<String> renderers;
    private String gtfsStation;
    private String gtfsPlatform;
    private boolean useSchedule;
    private boolean playAnnouncements;
    private String announcementTimes;
    private String announcementVoice;
    private boolean coalesceStationSequences;

    public IndicatorSettings(boolean useSchedule, List<String> renderers, boolean playAnnouncements,
                             String announcementTimes, String announcementVoice, boolean coalesceStationSequences,
                             List<DepartureData> departureData, String gtfsStation, String gtfsPlatform)
    {
        this.useSchedule = useSchedule;
        this.departureData = departureData;
        this.playAnnouncements = playAnnouncements;
        this.announcementTimes = announcementTimes;
        this.announcementVoice = announcementVoice;
        this.coalesceStationSequences = coalesceStationSequences;
        this.renderers = renderers;
        this.gtfsStation = gtfsStation;
        this.gtfsPlatform = gtfsPlatform;
    }

    public List<DepartureData> getDepartureData() { return departureData; }

    public boolean useSchedule() { return useSchedule; }

    public boolean playAnnouncements() { return playAnnouncements; }

    public String announcementTimes() { return announcementTimes; }

    public String announcementVoice() { return announcementVoice; }
    
    public boolean coalesceStationSequences() { return coalesceStationSequences; }

    public List<String> getRenderers() { return renderers; }

    public String getGtfsStation() { return gtfsStation; }

    public String getGtfsPlatform() { return gtfsPlatform; }
}
