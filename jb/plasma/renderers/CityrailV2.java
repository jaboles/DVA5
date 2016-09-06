package jb.plasma.renderers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.List;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;

public class CityrailV2 extends Cityrail
{
    protected static Color MiddleColor = new Color(0, 30, 120);
    protected static Color BackgroundColor = new Color(0, 0, 0);
    protected static Color TextYellow = Color.yellow;
    protected static Color TextWhite = Color.white;

    protected Font DueOutFont;
    protected Font NextTrainFont;
    protected double stationListSeparation;

    protected boolean isConcourse;

    public CityrailV2(boolean isConcourse) {
        this.isConcourse = isConcourse;

        stationListInc = 0.036 / PlasmaPanel.FPS;
        stationListSeparation = 0.052;
        stationListPosInitial = 0.2 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio() { return PORTRAIT_1610; }
    public String toString() { return "CityRail V2 " + (isConcourse? "Concourse" : "Platform") + " (Portrait 16:10)"; }

    public void dimensionsChanged()
    {
        TimeNowFont = new Font("Arial", Font.BOLD, (int)(height * 0.024));
        TimeFont = new Font("Arial", Font.BOLD, (int)(height * 0.036));
        DepartureTimeFont = new Font("Arial", Font.BOLD, (int)(height * 0.04));
        DestinationFont = new Font("Arial", Font.BOLD, (int)(height * 0.10));
        Destination2Font = DepartureTimeFont;
        MainFont = new Font("Arial", Font.BOLD, (int)(height * 0.044));
        DueOutFont = new Font("Arial", Font.BOLD, (int)(height * 0.05));
        SmallFont = new Font("Arial", Font.BOLD, (int)(height * 0.0238));
        NextTrainFont = Destination2Font;
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        List<DepartureData> data = DepartureData;
        
        // Backgrounds
        fillRect(0, 0, 1, 1, BackgroundColor);
        fillRect(0, 0, 1, 0.8, MiddleColor);
        Polygon timeNowBackground = new Polygon();
        timeNowBackground.addPoint(round(0.5 * width), 0);
        timeNowBackground.addPoint(width, 0);
        timeNowBackground.addPoint(width, round(0.09 * width));
        timeNowBackground.addPoint(round(0.55 * width), round(0.09 * width));
        g.setColor(BackgroundColor);
        g.fillPolygon(timeNowBackground);

        // Static text
        drawString("Next train", 0.02, 0.04, TextWhite, SmallFont);
        drawString("Time now", 0.55, 0.025, TextWhite, TimeNowFont);
        drawStringR(TimeFormat.format(timeNow), 0.98, 0.04, TextWhite, TimeFont);
        drawLine(0.25, 0.2, 0.25, 0.8, TextWhite);
        drawString("Following Trains", 0.02, 0.83, TextWhite, SmallFont);

        // Next train
        DepartureData d0 = null;
        if (data.size() >= 1)
        {
            d0 = data.get(0);
            drawString(d0.Destination, 0.02, 0.13, TextYellow, DestinationFont);
            drawString(d0.Destination2, 0.02, 0.18, TextYellow, Destination2Font);
            drawString(d0.Type, 0.02, 0.24, TextWhite, SmallFont);
            drawStringC("Departs", 0.125, 0.34, TextWhite, SmallFont);
            drawStringC(getDueOut(d0.DueOut).getValue1(), 0.125, 0.39, TextWhite, DueOutFont);
            drawStringC("mins", 0.125, 0.415, TextWhite, SmallFont);

            drawStringC(Integer.toString(d0.Cars), 0.125, 0.68, TextWhite, MainFont);
            drawStringC("cars", 0.125, 0.705, TextWhite, SmallFont);
            drawRect(0.078, 0.64, 0.175, 0.71, TextWhite);
        }

        if (data.size() >= 2)
        {
            DepartureData d1 = data.get(1);
            drawStringR(getDueOut(d1.DueOut).getValue1(), 0.077, 0.88, TextYellow, NextTrainFont);
            drawString("mins",  0.09, 0.88, TextWhite, SmallFont);
            drawString(d1.Destination, 0.21, 0.88, TextYellow, MainFont);
            drawString(d1.Type, 0.21, 0.91, TextWhite, SmallFont);
        }
        if (data.size() >= 3)
        {
            DepartureData d2 = data.get(1);
            drawStringR(getDueOut(d2.DueOut).getValue1(), 0.077, 0.95, TextYellow, NextTrainFont);
            drawString("mins",  0.09, 0.95, TextWhite, SmallFont);
            drawString(d2.Destination, 0.21, 0.95, TextYellow, NextTrainFont);
            drawString(d2.Type, 0.21, 0.98, TextWhite, SmallFont);
        }

        if (d0 != null)
        {
            boolean shouldScroll = d0.Stops.length > 11;
            g.setClip(round(0.25 * width), round(0.21 * height), round((1 - 0.25) * width), round((0.8 - 0.21) * height));
            String[] stationList = d0.Stops;
            for (int i = 0; i < stationList.length; i++) {
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], 0.27, yAbs, TextWhite, MainFont);
                if (shouldScroll) {
                    yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                    drawString(stationList[i], 0.27, yAbs, TextWhite, MainFont);
                }
            }
            g.setClip(0, 0, width, height);

            if (shouldScroll) {
                stationListPos -= stationListInc;
                if (stationListPos < (-1 * (stationList.length + 5) * stationListSeparation)) {
                    stationListPos += (stationList.length + 5) * stationListSeparation;
                }
            }
        }
    }
}