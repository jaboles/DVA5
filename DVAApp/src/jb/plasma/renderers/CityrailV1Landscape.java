package jb.plasma.renderers;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.time.LocalDateTime;

import jb.dvacommon.ui.Resources;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;

public class CityrailV1Landscape extends CityrailV1
{
    public CityrailV1Landscape(boolean isv11, boolean isConcourse)
    {
        super(isv11, isConcourse);

        TopOffset = 0.1;
        BottomOffset = 0.8;
        LeftOffset = 0.2;
        RightOffset = 0.8;

        stationListInc = 0.045 / PlasmaPanel.FPS;
        stationListSeparation = 0.07;
        stationListPosInitial = 0.29 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public int getAspectRatio() { return LANDSCAPE_43; }
    public String toString() { return "CityRail " + (isv11? "V1.1" : "V1") + " " + (isConcourse? "Concourse" : "Platform") + " (Landscape 4:3)"; }

    public void dimensionsChanged()
    {
        // Update the fonts if the dimensions changed.
        TimeNowFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TimeFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.07));
        DepartureTimeFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.098));
        DestinationFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.12));
        Destination2Font = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.05));
        MainFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.066));
        SmallFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.05));
        TypeSmallFont = Resources.ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.035));
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);

        DepartureData d0 = null, d1 = null, d2 = null;
        if (DepartureData.size() > 0)
        {
            d0 = DepartureData.get(0);
            if (DepartureData.size() > 1)
            {
                d1 = DepartureData.get(1);
                if (DepartureData.size() > 2)
                {
                    d2 = DepartureData.get(2);
                }
            }
        }

        // Background and colour blocks
        fillRect(0, 0, 1, 1, BackgroundColor);
        fillRect(0, TopOffset, LeftOffset, BottomOffset, SideColor);
        fillRect(RightOffset, TopOffset, 1, BottomOffset, SideColor);
        fillRect(LeftOffset, TopOffset, RightOffset, BottomOffset, MiddleColor);

        // Time now
        drawString("Time now:", 0.57, 0.08, TextWhite, TimeNowFont);
        drawString(TimeFormat.format(timeNow), 0.77, 0.08, TextWhite, TimeFont);

        // Platform
        if (isConcourse)
        {
            drawStringC("Platform", 0.9, 0.18, TextWhite, TimeNowFont);
        }
        // Due out (x) mins
        drawStringC("Due Out",  0.1, 0.67, TextWhite, SmallFont);
        drawString("mins",  0.097, 0.77, TextWhite, SmallFont);

        if (isConcourse)
        {
            drawString("Platform", 0.825, 0.85, TextWhite, TimeNowFont);
        }
        // V1 uses 'Next trains', V1.1 uses 'Following trains'
        if (isv11) {
            drawString("Following trains", 0.35, 0.85, TextWhite, TimeNowFont);
        } else {
            drawString("Next trains", 0.4, 0.85, TextWhite, TimeNowFont);
        }
        drawString("Due Out",  0.031, 0.85, TextWhite, TimeNowFont);

        // 1st departure
        if (d0 != null)
        {
            if (isConcourse) {
                drawStringC(Integer.toString(d0.Platform), 0.9, 0.25, TextWhite, MainFont);
            } else if (d0.Cars > 0) {
                drawStringC(d0.Cars + " car", 0.9, 0.18, TextWhite, MainFont);
                drawStringC("train", 0.9, 0.245, TextWhite, MainFont);
            }
            drawString(d0.Destination,  0.21, 0.2, TextYellow, DestinationFont);
            if (isv11) {
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, 0.21, 0.265, TextYellow, Destination2Font);
                }
                if (d0.Type != null && d0.Type.length() > 0)
                    drawString(d0.Type, 0.02, 0.265, TextWhite, TypeSmallFont);
            } else {
                if (d0.Type != null && d0.Type.length() > 0)
                    drawString(d0.Type, 0.21, 0.265, TextWhite, MainFont);
            }
            LocalDateTime dueOut = d0.DueOut;
            if (dueOut != null) {
                drawString(dueOut.format(DueOutFormat), 0.012, 0.2, TextWhite, DepartureTimeFont);
                int m = getDueOut(d0.DueOut).getValue1();
                if (m > 0) {
                    drawStringR(m, 0.089, 0.77, TextWhite, DepartureTimeFont);
                }
            }
        }

        // 2nd departure
        if (d1 != null) {
            if (d1.DueOut != null) {
                int m = getDueOut(d1.DueOut).getValue1();
                if (m > 0) {
                    drawStringR(m, 0.077, 0.91, TextYellow, MainFont);
                    drawString("mins", 0.09, 0.91, TextWhite, SmallFont);
                }
            }
            drawString(d1.Destination, 0.21, 0.91, TextYellow, MainFont);
            if (d1.Type != null && d1.Type.length() > 0)
                drawString(d1.Type, 0.6, 0.91, TextWhite, SmallFont);
            if (isConcourse)
                drawString(d1.Platform, 0.9, 0.91, TextYellow, MainFont);
        }

        // 3rd departure
        if (d2 != null)
        {
            if (d2.DueOut != null) {
                int m = getDueOut(d2.DueOut).getValue1();
                if (m > 0) {
                    drawStringR(m, 0.077, 0.98, TextYellow, MainFont);
                    drawString("mins", 0.09, 0.98, TextWhite, SmallFont);
                }
            }
            drawString(d2.Destination, 0.21, 0.98, TextYellow, MainFont);
            if (d2.Type != null && d2.Type.length() > 0)
                drawString(d2.Type, 0.6, 0.98, TextWhite, SmallFont);
            if (isConcourse)
                drawString(d2.Platform, 0.9, 0.98, TextYellow, MainFont);
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g);

        DepartureData d0;
        if (DepartureData.size() > 0)
            d0 = DepartureData.get(0);
        else return;

        boolean shouldScroll = d0.Stops.length > 7;
        g.setClip(round(0.21 * width), round(0.29 * height), round((1 - 0.21 - 0.21) * width), round((BottomOffset - 0.29) * height));
        for (int i = 0; i < d0.Stops.length; i++) {
            int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
            drawString(d0.Stops[i].Name, 0.25, yAbs, TextWhite, MainFont);
            if (shouldScroll) {
                yAbs = round(stationListPos * height) + round((i + d0.Stops.length + 5) * stationListSeparation * height);
                drawString(d0.Stops[i].Name, 0.25, yAbs, TextWhite, MainFont);
            }
        }
        g.setClip(0, 0, width, height);

        if (shouldScroll) {
            stationListPos -= stationListInc;
            if (stationListPos < (-1 * (d0.Stops.length + 5) * stationListSeparation)) {
                stationListPos += (d0.Stops.length + 5) * stationListSeparation;
            }
        }
    }
}