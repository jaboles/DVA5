package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Primary extends CityrailV4
{
    public BufferedImage LineLogo;

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

        drawString("Next service", 0.025, 0.08, HeaderTextColor, HeaderFont);

        if (LineLogo != null) {
            drawImageSquare(LineLogo, 0.025, 0.12, 0.15);
        }
        drawString(d0.Destination, 0.4, 0.12, TextColor, DestinationFont);
    }
}