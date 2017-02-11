package jb.plasma;
import jb.plasma.data.DepartureData;

import java.util.List;

// Class used to store settings on the Indicators page for saving/loading
public class IndicatorSettings
{
    private List<DepartureData> departureData;
    private List<String> renderers;
    private String line;
    private String direction;
    private String station;
    private int platform;
    private int cars;
    private boolean useSchedule;
    private boolean playAnnouncements;
    private String announcementTimes;
    private String announcementVoice;
    private boolean coalesceStationSequences;

    public IndicatorSettings(boolean useSchedule, List<String> renderers, boolean playAnnouncements, String announcementTimes, String announcementVoice, boolean coalesceStationSequences, List<DepartureData> departureData, String line, String direction, String station, int platform, int cars)
    {
        this.useSchedule = useSchedule;
        this.departureData = departureData;
        this.playAnnouncements = playAnnouncements;
        this.announcementTimes = announcementTimes;
        this.announcementVoice = announcementVoice;
        this.coalesceStationSequences = coalesceStationSequences;
        this.renderers = renderers;
        this.line = line;
        this.direction = direction;
        this.station = station;
        this.platform = platform;
        this.cars = cars;
    }

    public List<DepartureData> getDepartureData() { return departureData; }

    public boolean useSchedule() { return useSchedule; }

    public boolean playAnnouncements() { return playAnnouncements; }

    public String announcementTimes() { return announcementTimes; }

    public String announcementVoice() { return announcementVoice; }
    
    public boolean coalesceStationSequences() { return coalesceStationSequences; }

    public List<String> getRenderers() { return renderers; }

    public String getLine() { return line; }

    public String getDirection() { return direction; }

    public String getStation() { return station; }

    public int getPlatform() { return platform; }

    public int getCars() { return cars; }
}
