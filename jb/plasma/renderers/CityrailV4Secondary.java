package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Secondary extends CityrailV4
{
    public BufferedImage Line1Logo;
    public BufferedImage Line2Logo;

    public String toString()
    {
        return "CityRail V4 Dual-screen Secondary (Landscape 16:10)";
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
        if (d1 != null) DrawDeparture(d1, 0.3);

        // 3rd departure
        if (d2 != null) DrawDeparture(d2, 0.6);
    }

    private void DrawDeparture(DepartureData d, double y)
    {
        if (Line1Logo != null)
        {
            drawImageSquare(Line1Logo, 0.025, y, 0.15);
        }
        drawString(d.Destination, 0.4, y, TextColor, DestinationFont);
    }
}
