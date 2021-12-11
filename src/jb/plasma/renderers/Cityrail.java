package jb.plasma.renderers;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.imageio.ImageIO;
import org.javatuples.Pair;
import jb.common.ExceptionReporter;
import jb.plasma.DepartureData;
import jb.plasma.Drawer;
import jb.plasma.ui.PlasmaPanel;

// Common ancestor for all CityRail renderers. Has stuff that is common to all of them, such as the
// way times are formatted, fonts for common text items, and parameters for scrolling behaviour.
public abstract class Cityrail extends Drawer
{
    protected Font TimeNowFont = null;
    protected Font TimeFont = null;
    protected Font DepartureTimeFont = null;
    protected Font DestinationFont = null;
    protected Font Destination2Font = null;
    protected Font MainFont = null;
    protected Font SmallFont = null;
    protected Font TypeSmallFont = null;

    protected static DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    protected static DateTimeFormatter DueOutFormat = DateTimeFormatter.ofPattern("HH:mm");

    protected double stationListInc = -0.1 / PlasmaPanel.FPS;
    protected double stationListPosInitial;
    protected double stationListPos;
    protected double stationListSeparation;

    protected Image airportIcon;
    
    protected LocalDateTime timeNow;

    public Cityrail()
    {
        try
        {
            airportIcon = ImageIO.read(Cityrail.class.getResourceAsStream("/dva_icon.png"));
        }
        catch (IOException ex)
        {
            ExceptionReporter.reportException(ex);
        }
    }
    
    public void paint(Graphics g)
    {
        super.paint(g);
        timeNow = LocalDateTime.now();
    }

    protected Pair<Integer,Integer> getDueOut(LocalDateTime dueOut)
    {
        int mins = (int)ChronoUnit.MINUTES.between(timeNow, dueOut);
        return new Pair<>((mins / 60), mins % 60);
    }
    
    protected boolean isAirportTrain(DepartureData d)
    {
        //String[] stops = d.Stops;
        return true;
    }

    public void dataChanged(List<DepartureData> newData)
    {
        super.dataChanged(newData);
        stationListPos = stationListPosInitial;
    }

    public String[] splitString(String s) { return s.split(" "); }

    public void scrollStationList(Graphics g, DepartureData d, Color c, Font f, double x, double clipx1, double clipy1, double clipx2, double clipy2)
    {
        // Scrolling list
        boolean shouldScroll = d.Stops.length > 6;
        g.setClip(round(clipx1 * width), round(clipy1 * height), round(clipx2 * width), round(clipy2 * height));
        String[] stationList = d.Stops;
        for (int i = 0; i < stationList.length; i++) {
            double y = stationListPos + (i * stationListSeparation);
            int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
            drawString(stationList[i], x, yAbs, c, f);

            // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
            if (shouldScroll) {
                y = stationListPos + ((i + stationList.length + 5) * stationListSeparation);
                yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                drawString(stationList[i], x, yAbs, c, f);
            }
        }
        g.setClip(0, 0, width, height);

        if (shouldScroll) {
            stationListPos -= (stationListInc * realFPSAdjustment);
            if (stationListPos < (-1 * (stationList.length + 5) * stationListSeparation)) {
                stationListPos += (stationList.length + 5) * stationListSeparation;
            }
        }
    }
}