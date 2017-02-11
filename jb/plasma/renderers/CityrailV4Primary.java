package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.data.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Primary extends CityrailV4
{
    public BufferedImage LineLogo;
    private String dueOutString;

    public CityrailV4Primary() {
        stationListInc = 0.0528 / PlasmaPanel.FPS;
        stationListSeparation = 0.10;
        stationListPosInitial = 0.35 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public String toString()
    {
        return "CityRail V4 Dual-screen Primary (Landscape 16:10)";
    }

    public void dataChanged(java.util.List<DepartureData> data)
    {
        super.dataChanged(data);
        // Set the line logo from the departuredata
        if (data.size() > 0)
        {
            DepartureData d = data.get(0);
            CityrailLine line = CityrailLine.get(d.Line);
            LineLogo = TryLoadLineLogo(line);
        }
        else { LineLogo = null; }
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        DepartureData d0 = null;
            if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
        }

        drawString("Next service", LeftMargin, 0.08, HeaderTextColor, HeaderFont);
        drawStringR("Platform", RightMargin, 0.16, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin, 0.8, TextColor, PlatformDepartsLabelFont);

        if (d0 != null) {
            double departureLeft = LeftMargin;
            if (LineLogo != null) {
                drawImageSquare(LineLogo, LeftMargin, 0.12, 0.2);
                departureLeft = 0.16;
            }
            drawString(d0.Destination, departureLeft, 0.25, TextColor, DestinationFont);
            if (d0.Destination2 != null) {
                drawString(d0.Destination2, departureLeft + 0.005, 0.31, TextColor, Destination2Font);
            }

            drawStringR(d0.Platform, RightMargin, 0.28, OrangeTextColor, PlatformDepartsFont);
            Pair<Integer, Integer> dueOut = getDueOut(d0.DueOut);
            int h = dueOut.getValue0();
            int m = dueOut.getValue1();
            dueOutString = Integer.toString(m) + " min";
            if (h > 0) {
                dueOutString = Integer.toString(h) + " hr " + dueOutString;
            }
            drawStringR(dueOutString, RightMargin, 0.95, OrangeTextColor, PlatformDepartsFont);
            drawMiniTextBox(0.77, 0.39, Integer.toString(d0.Cars) + " carriages");
            drawMiniTextBox(0.77, 0.46, d0.Type);

            // Scrolling list
            boolean shouldScroll = d0.Stops.length > 6;
            g.setClip(round(LeftMargin * width), round(0.35 * height), round(0.55 * width), height - round(0.35 * height));
            fillRect(LeftMargin, 0.35, 0.6, 1, Color.white);
            String[] stationList = d0.Stops;
            for (int i = 0; i < stationList.length; i++) {
                double y = stationListPos + (i * stationListSeparation);
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], LeftMargin, yAbs, TextColor, MainFont);

                // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
                if (shouldScroll) {
                    y = stationListPos + ((i + stationList.length + 5) * stationListSeparation);
                    yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                    drawString(stationList[i], LeftMargin, yAbs, TextColor, MainFont);
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

        drawLine(LeftMargin, 0.35, RightMargin, 0.35, TextColor);

    }
}