package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV5Primary extends CityrailV5Landscape
{
    private BufferedImage LineLogo;
    private CityrailLine Line;

    public CityrailV5Primary() {
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
        return "CityRail V5 Dual-screen Primary (Landscape 16:10)";
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
        int logoWidth = round(height * 0.2);
        if (Line != null) LineLogo = TryReloadLineLogo(Line, new Dimension(logoWidth, logoWidth));
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        DepartureData d0 = null;
            if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
        }

        drawString("Next service", LeftMargin, 0.08, HeaderTextColor, HeaderFont);
        drawStringR(TimeFormat.format(timeNow), 0.98, 0.08, HeaderTextColor, HeaderFont);
        drawString("Time now", 0.65, 0.08, HeaderTextColor, HeaderTimeNowFont);
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

            drawStringR(d0.Platform, RightMargin, 0.28, TextColor, PlatformDepartsFont);
            if (d0.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d0.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                if (h > 0 || m > 0) {
                    String dueOutString = m + " min";
                    if (h > 0) {
                        dueOutString = h + " hr " + dueOutString;
                    }
                    drawStringR(dueOutString, RightMargin, 0.95, TextColor, PlatformDepartsFont);
                }
            }

            double textboxWidth = (double)Math.max(g.getFontMetrics(TextBoxFont).stringWidth(d0.Cars + " cars"),
                    g.getFontMetrics(TextBoxFont).stringWidth(d0.Type != null ? d0.Type : "")) / width + 0.02;
            drawMiniTextBox(0.97 - textboxWidth, 0.39, textboxWidth, d0.Cars + " cars");
            if (d0.Type != null && d0.Type.length() > 0) {
                drawMiniTextBox(0.97 - textboxWidth, 0.46, textboxWidth, d0.Type);
            }

            // Scrolling list
            boolean shouldScroll = d0.Stops.length > 6;
            g.setClip(round(LeftMargin * width), round(0.35 * height), round(0.55 * width), height - round(0.35 * height));
            fillRect(LeftMargin, 0.35, 0.6, 1, Color.white);
            String[] stationList = d0.Stops;
            for (int i = 0; i < stationList.length; i++) {
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], LeftMargin, yAbs, TextColor, MainFont);

                if (d0.StopCarRanges != null && i < d0.StopCarRanges.length && d0.StopCarRanges[i] != null)  {
                    int carRangeYAbs = yAbs - round(stationListSeparation * 0.6 * height);
                    double carRangeOffset = (double)g.getFontMetrics(MainFont).stringWidth(stationList[i]) / width;
                    drawCarRangeTextBox(g, LeftMargin + carRangeOffset + 0.02, carRangeYAbs, d0.StopCarRanges[i]);
                }

                // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
                if (shouldScroll) {
                    yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                    drawString(stationList[i], LeftMargin, yAbs, TextColor, MainFont);

                    if (d0.StopCarRanges != null && i < d0.StopCarRanges.length && d0.StopCarRanges[i] != null)  {
                        int carRangeYAbs = yAbs - round(stationListSeparation * 0.6 * height);
                        double carRangeOffset = (double)g.getFontMetrics(MainFont).stringWidth(stationList[i]) / width;
                        drawCarRangeTextBox(g, LeftMargin + carRangeOffset + 0.02, carRangeYAbs, d0.StopCarRanges[i]);
                    }
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