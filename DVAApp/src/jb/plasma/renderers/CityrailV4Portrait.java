package jb.plasma.renderers;

import jb.dvacommon.ui.Resources;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV4Portrait extends CityrailV4
{
    private BufferedImage LineLogo;
    private CityrailLine Line;
    private Font NextPlatformDepartsLabelFont;
    private Font NextDestinationFont;
    private Font NextDestination2Font;
    private Font MiniTextBoxFont;
    private Font LargeDepartureTimeFont;
    private final boolean isConcourse;
    private final double nswTrainlinkTopOffset = 0.08;
    private double initialTop = 0;

    public CityrailV4Portrait(boolean isConcourse, Color headerBackgroundColor) {
        super(headerBackgroundColor);
        this.isConcourse = isConcourse;
        if (!isConcourse) {
            this.initialTop = 0.06;
        }

        stationListInc = 0.04 / PlasmaPanel.FPS;
        stationListSeparation = 0.056;
        stationListPosInitial = initialTop + 0.14 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public int getAspectRatio()
    {
        return PORTRAIT_WS;
    }

    public String toString()
    {
        return "CityRail V4 Single-screen " + (isConcourse? "Concourse" : "Platform") + " (Portrait w/s)";
    }

    public void dimensionsChanged()
    {
        HeaderFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.042));
        HeaderTimeNowFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.025));
        TimeFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.035));
        DestinationFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.074));
        LargeDepartureTimeFont = Resources.RobotoMedium.deriveFont(Font.BOLD, (int)(height * 0.09));
        Destination2Font = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.03));
        PlatformDepartsLabelFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.025));
        PlatformDepartsFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.065));
        PlatformDepartsFontSmall = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.05));
        MainFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.043));
        TextBoxFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.021));

        NextPlatformDepartsLabelFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.022));
        NextDestinationFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.026));
        NextDestination2Font = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.019));
        MiniTextBoxFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.017));

        int logoSize = round(height * (Line != null && Line.IsNswTrainlink ? 0.08 : 0.11));
        LineLogo = TryReloadLineLogo(Line, new Dimension(logoSize, logoSize));
    }

    public void dataChanged(java.util.List<DepartureData> data)
    {
        super.dataChanged(data);
        // Set the line logo from the departuredata
        if (data.size() > 0)
        {
            DepartureData d = data.get(0);
            Line = CityrailLine.get(d.Line);
            int logoSize = round(height * (Line != null && Line.IsNswTrainlink ? 0.08 : 0.11));
            LineLogo = TryReloadLineLogo(Line, new Dimension(logoSize, logoSize));
            if (Line.IsNswTrainlink) {
                stationListPosInitial += nswTrainlinkTopOffset;
            }
        }
        else {
            Line = null;
            LineLogo = null;
        }
    }

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
        double top = initialTop;

        if (!isConcourse) {
            fillRect(0, 0, 1, top, HeaderBackgroundColor);
            drawString("Service", LeftMargin, 0.045, HeaderTextColor, HeaderFont);
            drawString("Time now:", 0.5, 0.045, HeaderTextColor, HeaderTimeNowFont);
            drawStringR(TimeFormat.format(timeNow), 0.96, 0.045, HeaderTextColor, HeaderFont);
        }

        DepartureData d0 = null;
        DepartureData d1 = null;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
            if (DepartureData.size() > 1) {
                d1 = DepartureData.get(1);
            }
        }

        String dueOutString;
        if (d0 != null) {
            double logoOffset = LeftMargin;
            double logoWidth = Line != null && Line.IsNswTrainlink ? 0.08 : 0.11;
            if (LineLogo != null) {
                drawImage(LineLogo, LeftMargin, top + 0.01);
                logoOffset = LeftMargin + logoWidth + 0.08;
            }
            if (Line != null && Line.IsNswTrainlink) {
                drawString(DueOutFormat.format(d0.DueOut), logoOffset, top + 0.085, TextColor, LargeDepartureTimeFont);
                drawString(d0.Destination, LeftMargin, top + 0.16, TextColor, DestinationFont);
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, LeftMargin + 0.006, top + 0.19, TextColor, Destination2Font);
                }
                top += nswTrainlinkTopOffset;
            } else {
                drawString(d0.Destination, logoOffset, top + 0.08, TextColor, DestinationFont);
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, logoOffset + 0.006, top + 0.11, TextColor, Destination2Font);
                }
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
            drawMiniTextBox(0.74, top + 0.24, d0.Cars + " carriages");
            if (d0.Type != null && d0.Type.length() > 0) {
                drawMiniTextBox(LeftMargin, 0.8, d0.Type);
            }
        }

        drawStringR("Platform", RightMargin, top + 0.16, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin,  0.77, TextColor, PlatformDepartsLabelFont);

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
            if (d1.Type != null && d1.Type.length() > 0) {
                drawMiniMiniTextBox(0.3, 0.947, d1.Type);
            }
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g);

        DepartureData d0;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
        } else return;

        double top = initialTop;
        if (Line != null && Line.IsNswTrainlink) {
            top += nswTrainlinkTopOffset;
        }

        // Scrolling list
        boolean shouldScroll = d0.Stops.length > 6;
        g.setClip(round(LeftMargin * width), round((top + 0.14) * height), round(0.7 * width), round((0.64 - top) * height));
        fillRect(LeftMargin, top + 0.14, 0.7, top + 0.74, Color.white);
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
        }    }

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