package jb.plasma.renderers;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.time.LocalDateTime;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;

public class CityrailV1Portrait extends CityrailV1
{
    public CityrailV1Portrait(boolean isv11, boolean isConcourse)
    {
        super(isv11, isConcourse);

        TopOffset = 0.0;
        BottomOffset = 0.87;
        LeftOffset = 0.2;
        RightOffset = 0.8;

        stationListInc = 0.036 / PlasmaPanel.FPS;
        stationListSeparation = 0.04;
        stationListPosInitial = 0.12 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio() { return PORTRAIT_1610; }
    public String toString() { return "CityRail " + (isv11? "V1.1" : "V1") + " " + (isConcourse? "Concourse" : "Platform") + " (Portrait 16:10)"; }

    // Update the fonts if the dimensions changed.
    public void dimensionsChanged()
    {
        TimeNowFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.024));
        TimeFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.036));
        DepartureTimeFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.04));
        DestinationFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.05));
        Destination2Font = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.02));
        MainFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.036));
        SmallFont = ArialBold.deriveFont(Font.PLAIN, (int)(height * 0.025));
    }

    public void paint(Graphics g)
    {
        super.paint(g);
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
        
        fillRect(0, 0, 1, 1, BackgroundColor);
        fillRect(0, TopOffset, LeftOffset, BottomOffset, SideColor);
        fillRect(RightOffset, TopOffset, 1, BottomOffset, SideColor);
        fillRect(LeftOffset, TopOffset, RightOffset, BottomOffset, MiddleColor);

        if (isConcourse)
            drawString("Platform", 0.825, 0.04, TextWhite, TimeNowFont);
        drawString("Due Out",  0.031, 0.78, TextWhite, SmallFont);
        drawString("mins",  0.097, 0.84, TextWhite, SmallFont);

        if (isConcourse)
            drawString("Platform", 0.825, 0.9, TextWhite, TimeNowFont);
        if (isv11) {
            drawString("Following trains", 0.35, 0.9, TextWhite, TimeNowFont);
        } else {
            drawString("Next trains", 0.4, 0.9, TextWhite, TimeNowFont);
        }
        drawString("Due Out",  0.031, 0.9, TextWhite, TimeNowFont);

        // 1st departure
        if (d0 != null)
        {
            if (isConcourse) {
                drawString(Integer.toString(d0.Platform), 0.88, 0.09, TextWhite, MainFont);
            } else if (d0.Cars > 0) {
                drawString(d0.Cars + " car", 0.84, 0.06, TextWhite, MainFont);
                drawString("train", 0.85, 0.12, TextWhite, MainFont);
            }
            drawString(d0.Destination,  0.21, 0.048, TextYellow, DestinationFont);
            if (isv11) {
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, 0.21, 0.13, TextYellow, Destination2Font);
                }
                drawString(d0.Type, 0.02, 0.09, TextWhite, TypeSmallFont);
            } else {
                drawString(d0.Type, 0.21, 0.09, TextWhite, MainFont);
            }
            LocalDateTime dueOut = d0.DueOut;
            if (dueOut != null) {
                drawString(DueOutFormat.format(dueOut), 0.012, 0.048, TextWhite, DepartureTimeFont);
                int m = getDueOut(dueOut).getValue1();
                if (m > 0) {
                    drawStringR(Integer.toString(m), 0.089, 0.84, TextWhite, DepartureTimeFont);
                }
            }
        }

        // 2nd departure
        if (d1 != null)
        {
            if (d1.DueOut != null) {
                int m = getDueOut(d1.DueOut).getValue1();
                if (m > 0) {
                    drawStringR(m, 0.077, 0.94, TextYellow, MainFont);
                    drawString("mins", 0.09, 0.94, TextWhite, SmallFont);
                }
            }
            drawString(d1.Destination, 0.21, 0.94, TextYellow, MainFont);
            drawString(d1.Type, 0.6, 0.94, TextWhite, SmallFont);
            drawString(d1.Platform, 0.9, 0.94, TextYellow, MainFont);
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
            drawString(d2.Type, 0.6, 0.98, TextWhite, SmallFont);
            drawString(d2.Platform, 0.9, 0.98, TextYellow, MainFont);
        }

        // Scrolling list
        if (d0 != null)
        {
            boolean shouldScroll = d0.Stops.length > 15;
            g.setClip(round(0.21 * width), round(0.12 * height), round((1 - 0.21 - 0.21) * width), round((BottomOffset - 0.12) * height));
            String[] stationList = d0.Stops;
            for (int i = 0; i < stationList.length; i++) {
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], 0.25, yAbs, TextWhite, MainFont);
                if (shouldScroll) {
                    yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                    drawString(stationList[i], 0.25, yAbs, TextWhite, MainFont);
                }
            }
            g.setClip(0, 0, width, height);

            if (d0.Stops.length > 17) {
                stationListPos -= stationListInc;
                if (stationListPos < (-1 * (stationList.length + 5) * stationListSeparation)) {
                    stationListPos += (stationList.length + 5) * stationListSeparation;
                }
            }
        }
    }
}