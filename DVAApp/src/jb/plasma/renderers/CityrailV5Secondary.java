package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV5Secondary extends CityrailV5Landscape
{
    private BufferedImage Line1Logo;
    private BufferedImage Line2Logo;
    private CityrailLine Line1;
    private CityrailLine Line2;

    public Dimension getAspectRatio()
    {
        return LANDSCAPE_1610;
    }

    public String toString()
    {
        return "CityRail V5 Dual-screen Secondary (Landscape 16:10)";
    }

    public void dataChanged(java.util.List<DepartureData> data)
    {
        super.dataChanged(data);
        if (data.size() > 1)
        {
            DepartureData d1 = data.get(1);
            Line1 = CityrailLine.get(d1.Line);
            int logoSize = round(height * 0.2);
            Line1Logo = TryReloadLineLogo(Line1, new Dimension(logoSize, logoSize));
            if (data.size() > 2)
            {
                DepartureData d2 = data.get(2);
                Line2 = CityrailLine.get(d2.Line);
                Line2Logo = TryReloadLineLogo(Line2, new Dimension(logoSize, logoSize));
            }
            else {
                Line2Logo = null;
                Line2 = null;
            }
        }
        else {
            Line1Logo = null;
            Line2Logo = null;
            Line1 = null;
            Line2 = null;
        }
    }

    public void dimensionsChanged() {
        super.dimensionsChanged();
        int logoSize = round(height * 0.2);
        if (Line1 != null) Line1Logo = TryReloadLineLogo(Line1, new Dimension(logoSize, logoSize));
        if (Line2 != null) Line2Logo = TryReloadLineLogo(Line2, new Dimension(logoSize, logoSize));
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
        DepartureData d1 = null, d2 = null;
        if (DepartureData.size() > 1) {
            d1 = DepartureData.get(1);
            if (DepartureData.size() > 2) {
                d2 = DepartureData.get(2);
            }
        }

        drawString("Following services", 0.025, 0.08, HeaderTextColor, HeaderFont);
        drawStringR(TimeFormat.format(timeNow), 0.98, 0.08, HeaderTextColor, HeaderFont);
        drawString("Time now", 0.65, 0.08, HeaderTextColor, HeaderTimeNowFont);

        // 2nd departure
        DrawDeparture(d1, g, 0.12, Line1Logo);

        drawLine(LeftMargin, 0.55, RightMargin, 0.55, TextColor);

        // 3rd departure
        DrawDeparture(d2, g, 0.57, Line2Logo);
    }

    private void DrawDeparture(DepartureData d, Graphics g, double y, BufferedImage lineLogo)
    {
        String dueOutString;
        drawStringR("Platform", RightMargin, y + 0.05, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin, y + 0.27, TextColor, PlatformDepartsLabelFont);

        if (d != null) {
            double destinationLeft = LeftMargin;
            if (lineLogo != null) {
                drawImage(lineLogo, LeftMargin, y);
                destinationLeft = 0.16;
            }
            drawString(d.Destination, destinationLeft, y + 0.13, TextColor, DestinationFont);
            if (d.Destination2 != null) {
                drawString(d.Destination2, destinationLeft + 0.005, y + 0.19, TextColor, Destination2Font);
            }

            drawStringR(d.Platform, RightMargin, y + 0.17, TextColor, PlatformDepartsFontSmall);
            if (d.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                if (h > 0 || m > 0) {
                    dueOutString = m + " min";
                    if (h > 0) {
                        dueOutString = h + " hr " + dueOutString;
                    }
                    drawStringR(dueOutString, RightMargin, y + 0.38, TextColor, PlatformDepartsFontSmall);
                }
            }
            double x = drawMiniTextBox(g, LeftMargin, y + 0.24, d.Cars + " cars");
            if (d.Type != null && d.Type.length() > 0) {
                drawMiniTextBox(g, LeftMargin + x + 0.01, y + 0.24, d.Type);
            }
        }
    }
}