package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Primary extends CityrailV4Landscape
{
    private BufferedImage LineLogo;
    private CityrailLine Line;

    public CityrailV4Primary(Color headerBackgroundColor) {
        super(headerBackgroundColor);
        stationListInc = 0.0528 / PlasmaPanel.FPS;
        stationListSeparation = 0.10;
        stationListPosInitial = 0.35 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio()
    {
        return LANDSCAPE_1610;
    }

    public String toString()
    {
        return "CityRail V4 Dual-screen Platform Primary (Landscape 16:10)";
    }

    public void dataChanged(java.util.List<DepartureData> data)
    {
        super.dataChanged(data);
        // Set the line logo from the departuredata
        if (data.size() > 0)
        {
            DepartureData d = data.get(0);
            Line = CityrailLine.get(d.Line);
            int logoSize = round(height * 0.2);
            LineLogo = TryReloadLineLogo(Line, new Dimension(logoSize, logoSize));
        }
        else {
            LineLogo = null;
            Line = null;
        }
    }

    public void dimensionsChanged() {
        super.dimensionsChanged();
        int logoSize = round(height * 0.2);
        if (Line != null) LineLogo = TryReloadLineLogo(Line, new Dimension(logoSize, logoSize));
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
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
                drawImage(LineLogo, LeftMargin, 0.12);
                departureLeft = 0.16;
            }
            drawString(d0.Destination, departureLeft, 0.25, TextColor, DestinationFont);
            if (d0.Destination2 != null) {
                drawString(d0.Destination2, departureLeft + 0.005, 0.31, TextColor, Destination2Font);
            }

            drawStringR(d0.Platform, RightMargin, 0.28, OrangeTextColor, PlatformDepartsFont);
            if (d0.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d0.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                if (h > 0 || m > 0) {
                    String dueOutString = m + " min";
                    if (h > 0) {
                        dueOutString = h + " hr " + dueOutString;
                    }
                    drawStringR(dueOutString, RightMargin, 0.95, OrangeTextColor, PlatformDepartsFont);
                }
            }
            drawMiniTextBox(0.77, 0.39, d0.Cars + " carriages");
            if (d0.Type != null && d0.Type.length() > 0) {
                drawMiniTextBox(0.77, 0.46, d0.Type);
            }
        }

        drawLine(LeftMargin, 0.35, RightMargin, 0.35, TextColor);
    }

    public void paint(Graphics g) {
        super.paint(g);

        DepartureData d0;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
        } else return;

        // Scrolling list
        boolean shouldScroll = d0.Stops.length > 6;
        g.setClip(round(LeftMargin * width), round(0.355 * height), round(0.55 * width), height - round(0.355 * height));
        fillRect(LeftMargin, 0.355, 0.6, 1, Color.white);
        for (int i = 0; i < d0.Stops.length; i++) {
            int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
            int yCutoff = round((1.05 + stationListSeparation) * height);
            if (yAbs > yCutoff)
                continue;
            drawString(d0.Stops[i].Name, LeftMargin, yAbs, TextColor, MainFont);

            // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
            if (shouldScroll) {
                yAbs = round(stationListPos * height) + round((i + d0.Stops.length + 5) * stationListSeparation * height);
                if (yAbs <= yCutoff)
                    drawString(d0.Stops[i].Name, LeftMargin, yAbs, TextColor, MainFont);
            }
        }
        g.setClip(0, 0, width, height);

        if (shouldScroll) {
            stationListPos -= (stationListInc * realFPSAdjustment);
            if (stationListPos < (-1 * (d0.Stops.length + 5) * stationListSeparation)) {
                stationListPos += (d0.Stops.length + 5) * stationListSeparation;
            }
        }
    }
}