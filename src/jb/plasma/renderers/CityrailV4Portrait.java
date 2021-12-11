package jb.plasma.renderers;

import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Portrait extends CityrailV4
{
    public BufferedImage LineLogo;
    private Font NextPlatformDepartsLabelFont;
    private Font NextDestinationFont;
    private Font NextDestination2Font;
    private Font MiniTextBoxFont;
    private final boolean isConcourse;
    private double top = 0;

    public CityrailV4Portrait(boolean isConcourse) {
        this.isConcourse = isConcourse;
        if (!isConcourse) {
            this.top = 0.06;
        }

        stationListInc = 0.04 / PlasmaPanel.FPS;
        stationListSeparation = 0.056;
        stationListPosInitial = top + 0.14 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio()
    {
        return PORTRAIT_1610;
    }

    public String toString()
    {
        return "CityRail V4 Single-screen " + (isConcourse? "Concourse" : "Platform") + " (Portrait 16:10)";
    }

    public void dimensionsChanged()
    {
        HeaderFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.042));
        HeaderTimeNowFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.025));
        TimeFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.035));
        DestinationFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.074));
        Destination2Font = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.03));
        PlatformDepartsLabelFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.025));
        PlatformDepartsFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.065));
        PlatformDepartsFontSmall = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        MainFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.043));
        TextBoxFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.021));

        NextPlatformDepartsLabelFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.022));
        NextDestinationFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.026));
        NextDestination2Font = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.019));
        MiniTextBoxFont = RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.017));
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
        if (!isConcourse) {
            fillRect(0, 0, 1, top, HeaderBackgroundColor);
            drawString("Service", LeftMargin, 0.045, HeaderTextColor, HeaderFont);
            // TODO: time now
        }

        DepartureData d0 = null;
        DepartureData d1 = null;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
            if (DepartureData.size() > 1) {
                d1 = DepartureData.get(1);
            }
        }

        drawStringR("Platform", RightMargin, top + 0.16, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin,  0.77, TextColor, PlatformDepartsLabelFont);

        String dueOutString;
        if (d0 != null) {
            double departureLeft = LeftMargin;
            if (LineLogo != null) {
                drawImageSquare(LineLogo, LeftMargin, top + 0.01, 0.11);
                departureLeft = 0.22;
            }
            drawString(d0.Destination, departureLeft, top + 0.08, TextColor, DestinationFont);
            if (d0.Destination2 != null) {
                drawString(d0.Destination2, departureLeft + 0.006, top + 0.11, TextColor, Destination2Font);
            }

            drawStringR(d0.Platform, RightMargin, top + 0.22, OrangeTextColor, PlatformDepartsFont);
            if (d0.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d0.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                if (h > 0 || m > 0) {
                    dueOutString = m + " min";
                    if (h > 0) {
                        dueOutString = h + " hr " + dueOutString;
                    }
                    drawStringR(dueOutString, RightMargin, 0.83, OrangeTextColor, PlatformDepartsFont);
                }
            }
            drawMiniTextBox(0.74, 0.32, d0.Cars + " carriages");
            if (d0.Type != null && !d0.Type.equals("")) {
                drawMiniTextBox(LeftMargin, 0.8, d0.Type);
            }

            // Scrolling list
            boolean shouldScroll = d0.Stops.length > 6;
            g.setClip(round(LeftMargin * width), round((top + 0.14) * height), round(0.7 * width), round((0.64 - top) * height));
            fillRect(LeftMargin, top + 0.14, 0.7, top + 0.74, Color.white);
            String[] stationList = d0.Stops;
            for (int i = 0; i < stationList.length; i++) {
                int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
                drawString(stationList[i], LeftMargin, yAbs, TextColor, MainFont);

                // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
                if (shouldScroll) {
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

        drawLine(LeftMargin, top + 0.13, RightMargin, top + 0.13, TextColor);
        drawLine(LeftMargin, 0.85, RightMargin, 0.85, TextColor);

        drawString("Next Service", LeftMargin, 0.88, TextColor, NextPlatformDepartsLabelFont);
        drawStringC("Platform", 0.7, 0.88, TextColor, NextPlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin, 0.88, TextColor, NextPlatformDepartsLabelFont);
        if (d1 != null) {
            drawString(d1.Destination, LeftMargin, 0.93, TextColor, NextDestinationFont);
            drawStringC(d1.Platform, 0.7, 0.93, TextColor, NextDestinationFont);
            if (d1.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d1.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                dueOutString = m + " min";
                if (h > 0) {
                    dueOutString = h + " hr " + dueOutString;
                }
                drawStringR(dueOutString, RightMargin, 0.93, TextColor, NextDestinationFont);
            }
            if (d1.Destination2 != null) {
                drawString(d1.Destination2, LeftMargin, 0.97, TextColor, NextDestination2Font);
            }
            if (d1.Type != null && !d1.Type.equals("")) {
                drawMiniMiniTextBox(0.3, 0.947, d1.Type);
            }
        }
    }

    protected void drawMiniTextBox(double x, double y, String s)
    {
        double h = 0.036;
        double w = 0.24;
        fillRect(x, y, x + w, y + h, TextColor);
        drawString(s, x + 0.01, y + h - 0.012, Color.white, TextBoxFont);
    }

    protected void drawMiniMiniTextBox(double x, double y, String s)
    {
        double h = 0.03;
        double w = 0.2;
        fillRect(x, y, x + w, y + h, TextColor);
        drawString(s, x + 0.01, y + h - 0.01, Color.white, MiniTextBoxFont);
    }
}