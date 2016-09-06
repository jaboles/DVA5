package jb.plasma.renderers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.List;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;

public class CityrailV3Secondary extends CityrailV3
{
    protected Color Line1Color1;
    protected Color Line1Color2;
    protected Color Line1TextColor;
    protected Color Line2Color1;
    protected Color Line2Color2;
    protected Color Line2TextColor;

    public CityrailV3Secondary() {
    }

    public Dimension getAspectRatio() { return LANDSCAPE_1610; }
    public String toString() { return "CityRail V3 Dual-screen Secondary (Landscape 16:10)"; }

    public void dimensionsChanged()
    {
        super.dimensionsChanged();
        nextTrainBackground = new Polygon();
        nextTrainBackground.addPoint(0, 0);
        nextTrainBackground.addPoint(round(0.22 * width), 0);
        nextTrainBackground.addPoint(round(0.18 * width), round(0.1 * width));
        nextTrainBackground.addPoint(0, round(0.1 * width));
    }

    public void dataChanged(List<DepartureData> data)
    {
        super.dataChanged(data);
        if (data.size() > 1)
        {
            DepartureData d1 = data.get(1);
            CityrailLine line = CityrailLine.get(d1.Line);
            Line1Color1 = d1.Color1Override != null ? d1.Color1Override : (line != null? line.Color1 : Color.black);
            Line1Color2 = d1.Color2Override != null ? d1.Color2Override : (line != null? line.Color2 : Color.black);
            Line1TextColor = d1.TextColorOverride != null ? d1.TextColorOverride : (line != null? line.TextColor : Color.black);
            if (data.size() > 2)
            {
                DepartureData d2 = data.get(2);
                line = CityrailLine.get(d2.Line);
                Line2Color1 = d2.Color1Override != null ? d2.Color1Override : (line != null? line.Color1 : Color.black);
                Line2Color2 = d2.Color2Override != null ? d2.Color2Override : (line != null? line.Color2 : Color.black);
                Line2TextColor = d2.TextColorOverride != null ? d2.TextColorOverride : (line != null? line.TextColor : Color.black);
            }
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        DepartureData d1 = null, d2 = null;
        if (DepartureData.size() > 1)
        {
            d1 = DepartureData.get(1);
            if (DepartureData.size() > 2)
            {
                d2 = DepartureData.get(2);
            }
        }
        
        g.setColor(Color.black);
        g.fillPolygon(nextTrainBackground);
        drawString("Following", 0.01, 0.06, TextWhite, NextTrainFont);
        drawString("Trains", 0.01, 0.13, TextWhite, NextTrainFont);
        drawStringR(TimeFormat.format(timeNow), 0.98, 0.105, TextWhite, DestinationFont);
        drawString("Time now:", 0.5, 0.105, TextWhite, Cars2Font);
        drawString("Departs", 0.07, 0.27, TextWhite, HeaderFont);
        drawString("Destination", 0.25, 0.27, TextWhite, HeaderFont);

        fillRect(0.05, 0.3, 1, 0.64, MiddleColor);
        fillRect(0.05, 0.66, 1, 1, MiddleColor);

        // 2nd departure
        if (d1 != null) DrawDeparture(d1, 0.3);

        // 3rd departure
        if (d2 != null) DrawDeparture(d2, 0.66);
    }

    private void DrawDeparture(DepartureData d, double y)
    {
        drawStringR(getDueOut(d.DueOut).getValue1(), 0.14, y + 0.12, TextBlue, DestinationFont);
        drawString("mins", 0.15, y + 0.12, TextBlue, MicroFontBold);
        drawString(d.Destination, 0.25, y + 0.12, TextBlue, DestinationFont);
        if (d.Line != null)
        {
            fillRect(0.05, y + 0.21, 1, y + 0.29, Line2Color1);
            drawString(d.Line, 0.25, y + 0.272, Line2TextColor, NextTrainFont);
        }
        drawString(d.Type.split(" "), 0.8, y + 0.08, 0.067, Color.gray, TypeNextFont);
    }
}