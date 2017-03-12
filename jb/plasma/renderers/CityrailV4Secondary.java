package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.data.DepartureData;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Secondary extends CityrailV4Landscape
{
    public BufferedImage Line1Logo;
    public BufferedImage Line2Logo;

    public Dimension getAspectRatio()
    {
        return LANDSCAPE_1610;
    }

    public String toString()
    {
        return "CityRail V4 Dual-screen Platform Secondary (Landscape 16:10)";
    }

    public void dataChanged(java.util.List<DepartureData> data)
    {
        super.dataChanged(data);
        if (data.size() > 1)
        {
            DepartureData d1 = data.get(1);
            CityrailLine line = CityrailLine.get(d1.Line);
            Line1Logo = TryLoadLineLogo(line);
            if (data.size() > 2)
            {
                DepartureData d2 = data.get(2);
                line = CityrailLine.get(d2.Line);
                Line2Logo = TryLoadLineLogo(line);
            }
            else { Line2Logo = null; }
        }
        else { Line1Logo = null; Line2Logo = null; }
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        DepartureData d1 = null, d2 = null;
        if (DepartureData.size() > 1) {
            d1 = DepartureData.get(1);
            if (DepartureData.size() > 2) {
                d2 = DepartureData.get(2);
            }
        }

        drawString("Following services", 0.025, 0.08, HeaderTextColor, HeaderFont);
        drawStringR(TimeFormat.format(timeNow), 0.98, 0.08, HeaderTextColor, HeaderFont);
        drawString("Time now:", 0.6, 0.08, HeaderTextColor, HeaderTimeNowFont);

        // 2nd departure
        DrawDeparture(d1, 0.12, Line1Logo);

        drawLine(LeftMargin, 0.55, RightMargin, 0.55, TextColor);

        // 3rd departure
        DrawDeparture(d2, 0.57, Line2Logo);
    }

    private void DrawDeparture(DepartureData d, double y, BufferedImage lineLogo)
    {
        String dueOutString;
        drawStringR("Platform", RightMargin, y + 0.05, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin, y + 0.27, TextColor, PlatformDepartsLabelFont);

        if (d != null) {
            double destinationLeft = LeftMargin;
            if (lineLogo != null) {
                drawImageSquare(lineLogo, LeftMargin, y, 0.2);
                destinationLeft = 0.16;
            }
            drawString(d.Destination, destinationLeft, y + 0.13, TextColor, DestinationFont);
            if (d.Destination2 != null) {
                drawString(d.Destination2, destinationLeft + 0.005, y + 0.19, TextColor, Destination2Font);
            }

            drawStringR(d.Platform, RightMargin, y + 0.17, OrangeTextColor, PlatformDepartsFontSmall);
            Pair<Integer, Integer> dueOut = getDueOut(d.DueOut);
            int h = dueOut.getValue0();
            int m = dueOut.getValue1();
            dueOutString = Integer.toString(m) + " min";
            if (h > 0) {
                dueOutString = Integer.toString(h) + " hr " + dueOutString;
            }
            drawStringR(dueOutString, RightMargin, y + 0.38, OrangeTextColor, PlatformDepartsFontSmall);
            drawMiniTextBox(LeftMargin, y + 0.24, Integer.toString(d.Cars) + " carriages");
            drawMiniTextBox(0.25, y + 0.24, d.Type);
        }
    }
}
