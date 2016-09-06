package jb.plasma.renderers;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.List;
import org.javatuples.Pair;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;

public class CityrailV3Primary extends CityrailV3
{
    protected Color LineColor1;
    protected Color LineColor2;
    protected Color LineTextColor;
    protected Stroke stroke;

    public CityrailV3Primary() {

        stationListInc = 0.0528 / PlasmaPanel.FPS;
        stationListSeparation = 0.11;
        stationListPosInitial = 0.225 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio() { return LANDSCAPE_1610; }
    public String toString() { return "CityRail V3 Dual-screen Primary (Landscape 16:10)"; }

    public void dimensionsChanged()
    {
        super.dimensionsChanged();
        nextTrainBackground = new Polygon();
        nextTrainBackground.addPoint(0, 0);
        nextTrainBackground.addPoint(round(0.15 * width), 0);
        nextTrainBackground.addPoint(round(0.11 * width), round(0.1 * width));
        nextTrainBackground.addPoint(0, round(0.1 * width));
        stroke = new BasicStroke(round(0.02 * height));        
    }
    
    public void dataChanged(List<DepartureData> data)
    {
        super.dataChanged(data);
        // Set the line colours from the departuredata
        if (data.size() > 0)
        {
            DepartureData d = data.get(0);
            CityrailLine line = CityrailLine.get(d.Line);
            LineColor1 = d.Color1Override != null ? d.Color1Override : (line != null? line.Color1 : Color.black);
            LineColor2 = d.Color2Override != null ? d.Color2Override : (line != null? line.Color2 : Color.black);
            LineTextColor = d.TextColorOverride != null ? d.TextColorOverride : (line != null? line.TextColor : Color.black);
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        DepartureData d0 = null;
        if (DepartureData.size() > 0)
        {
            d0 = DepartureData.get(0);
        }

        //if (paintInfrequent)
        {
            // Background color blocks
            fillRect(0, 0, 1, 0.22, MiddleColor);
            fillRect(0.05, 0.225, 0.95, 1, MiddleColor);

            if (d0 != null && d0.Line != null) {
                fillRect(0, 0.12, 1, 0.2, LineColor1);
                drawString(d0.Line, 0.15, 0.182, LineTextColor, NextTrainFont);
            }

            // 'This train'
            g.setColor(Color.black);
            g.fillPolygon(nextTrainBackground);
            drawString("This", 0.01, 0.06, TextWhite, NextTrainFont);
            drawString("Train", 0.01, 0.13, TextWhite, NextTrainFont);
        }
        
        if (d0 != null) {
            //if (paintInfrequent)
            {
                // 'in X mins to Destination'
                drawString("in", 0.15, 0.105, TextBlue, MicroFont);
                Pair<Integer,Integer> dueOut = getDueOut(d0.DueOut);
                double hoursWidth = 0;
                int dueOutHours = dueOut.getValue0();
                int dueOutMins = dueOut.getValue1();
                if (dueOutHours > 0)
                {
                    hoursWidth = 0.07;  
                    if (dueOutHours >= 10)
                    {
                        hoursWidth += 0.03;
                    }
                    drawStringR(dueOutHours, 0.16 + hoursWidth, 0.105, TextBlue, MainFont);
                    drawString("hr", 0.162 + hoursWidth, 0.105, TextBlue, MicroFontBold);
                }
                drawStringR(dueOutMins, 0.28 + hoursWidth, 0.105, TextBlue, MainFont);
                drawString("mins", 0.285 + hoursWidth, 0.105, TextBlue, MicroFontBold);
                drawString("to", 0.36 + hoursWidth, 0.105, TextBlue, MicroFont);
                drawString(d0.Destination, 0.41 + hoursWidth, 0.105, TextBlue, MainFont);

                drawStringC(d0.Type.split(" "), 0.125, 0.4, 0.08, TextBlue, TypeFont);

                // Cars count
                fillRect(0.06, 0.8, 0.17, 0.9, TextBlue);
                drawString(Integer.toString(d0.Cars), 0.065, 0.888, TextWhite, CarsFont);
                drawString("cars", 0.106, 0.888, TextWhite, Cars2Font);
            }
            
            // Scrolling list
            boolean shouldScroll = d0.Stops.length > 6;
            g.setClip(round(0.05 * width), round(0.225 * height), round((1 - 0.05) * width), height - round(0.225 * height));
            fillRect(0.22, 0.225, 0.95, 1, Color.white);
            String[] stationList = d0.Stops;
            double stopGraphicOffset = 0.35;
            for (int i = 0; i < stationList.length; i++) {
                double y = stationListPos + (i * stationListSeparation);
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], 0.25, yAbs, TextBlue, MainFont);

                // Draw the coloured line graphic to the left of the station name
                ((Graphics2D)g).setStroke(stroke);
                g.setColor(LineColor2);
                yAbs -= round(height * stopGraphicOffset * stationListSeparation);
                g.drawLine(round(width * 0.225), yAbs, round(width * 0.231), yAbs);
                g.setColor(LineColor1);
                g.drawLine(round(width * 0.22), round(height * (y - stopGraphicOffset * stationListSeparation - stationListSeparation)), round(width * 0.22), yAbs);

                // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
                if (shouldScroll) {
                    y = stationListPos + ((i + stationList.length + 5) * stationListSeparation);
                    yAbs = round(stationListPos * height) + round((i + stationList.length + 5) * stationListSeparation * height);
                    drawString(stationList[i], 0.25, yAbs, TextBlue, MainFont);

                    ((Graphics2D)g).setStroke(stroke);
                    g.setColor(LineColor2);
                    yAbs -= round(height * stopGraphicOffset * stationListSeparation);
                    g.drawLine(round(width * 0.225), yAbs, round(width * 0.231), yAbs);
                    g.setColor(LineColor1);
                    g.drawLine(round(width * 0.22), round(height * (y - stopGraphicOffset * stationListSeparation - stationListSeparation)), round(width * 0.22), yAbs);
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
    }
}