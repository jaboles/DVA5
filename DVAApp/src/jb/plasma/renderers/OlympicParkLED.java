package jb.plasma.renderers;

import jb.dvacommon.ui.Resources;
import jb.plasma.Drawer;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OlympicParkLED extends Drawer {
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HoursFormat = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter MinutesFormat = DateTimeFormatter.ofPattern("mm");
    private Font SmallFont;
    private Font LargeFont;
    private int scrollCount = 0;
    private int lastSecond = 0;

    public String toString() { return "Olympic Park LED"; }
    public Dimension getAspectRatio() { return LANDSCAPE_1610; }

    public void dimensionsChanged() {
        SmallFont = Resources.LedBoard7.deriveFont(Font.PLAIN, (int)(height * 0.08));
        LargeFont = Resources.AdvancedLedBoard7.deriveFont(Font.PLAIN, (int)(height * 0.12));
        lastSecond = LocalDateTime.now().getSecond();
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);

        jb.plasma.DepartureData d0 = null, d1 = null;
        if (DepartureData.size() > 0)
        {
            d0 = DepartureData.get(0);
            if (DepartureData.size() > 1)
            {
                d1 = DepartureData.get(1);
            }
        }

        fillRect(0, 0, 1, 1, Color.black);
        if (d0 != null) {
            drawString(TimeFormat.format(d0.DueOut) + " NEXT TRAIN", 0.01, 0.09, Color.orange, SmallFont);
            drawString(d0.Destination.toUpperCase(), 0.01, 0.21, Color.orange, LargeFont);
        }
        drawString("STOPPING AT", 0.01, 0.30, Color.orange, SmallFont);
        for (int i = 0; i < 2; i++) {
            int stopIndex = d0.Stops.length > 2 ? (scrollCount + i) % (d0.Stops.length + 2) : i;
            if (stopIndex < d0.Stops.length) {
                drawString(d0.Stops[stopIndex].Name.toUpperCase(), 0.01, 0.39 + (i * 0.09), Color.orange, SmallFont);
            }
        }

        if (d1 != null) {
            drawString(TimeFormat.format(d1.DueOut) + " FOLLOWING TRAIN", 0.01, 0.63, Color.orange, SmallFont);
            drawString(d1.Destination.toUpperCase(), 0.01, 0.76, Color.orange, LargeFont);
        }

        LocalDateTime timeNow = LocalDateTime.now();
        drawString(HoursFormat.format(timeNow), 0.01, 0.97, Color.orange, LargeFont);
        if (timeNow.getSecond() % 2 == 0)
            drawString(":", 0.107, 0.97, Color.orange, LargeFont);
        drawString(MinutesFormat.format(timeNow), 0.135, 0.97, Color.orange, LargeFont);
        if (timeNow.getSecond() != lastSecond) {
            scrollCount++;
            lastSecond = timeNow.getSecond();
        }
    }
}
