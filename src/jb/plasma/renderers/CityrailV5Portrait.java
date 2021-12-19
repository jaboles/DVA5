package jb.plasma.renderers;

import jb.dvacommon.ui.Resources;
import jb.plasma.CityrailLine;
import jb.plasma.DepartureData;
import jb.plasma.ui.PlasmaPanel;
import org.javatuples.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CityrailV5Portrait extends CityrailV4and5
{
    protected static final Color TextColor = Color.black;
    protected static final Color TextBoxColor = new Color(54, 57, 56);

    private BufferedImage LineLogo;
    private CityrailLine Line;
    private Font NextPlatformDepartsLabelFont;
    private Font NextDestinationFont;
    private Font NextDestination2Font;
    private Font MiniTextBoxFont;
    private Font LargeDepartureTimeFont;
    private final double nswTrainlinkTopOffset = 0.08;
    private BufferedImage airportIcon;
    protected static final double AirportIconWidth = 0.05;

    public CityrailV5Portrait() {
        stationListInc = 0.04 / PlasmaPanel.FPS;
        stationListSeparation = 0.056;
        stationListPosInitial = 0.14 + (1 * stationListSeparation);
        stationListPos = stationListPosInitial;
    }

    public Dimension getAspectRatio()
    {
        return PORTRAIT_1610;
    }

    public String toString()
    {
        return "CityRail V5 Single-screen (Portrait 16:10)";
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
        TextBoxFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.0225));

        NextPlatformDepartsLabelFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.022));
        NextDestinationFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.035));
        NextDestination2Font = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.025));
        MiniTextBoxFont = Resources.RobotoMedium.deriveFont(Font.PLAIN, (int)(height * 0.017));

        int logoSize = round(height * (Line != null && Line.IsNswTrainlink ? 0.08 : 0.11));
        LineLogo = TryReloadLineLogo(Line, new Dimension(logoSize, logoSize));

        airportIcon = loadSvg("/jb/plasma/renderers/resources/airport.svg", new Dimension(round(AirportIconWidth * width), round(AirportIconWidth * width)));
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
        double top = 0;

        fillRect(0, 0, 1, 0.06, HeaderBackgroundColor);
        drawString("Service", LeftMargin, 0.045, HeaderTextColor, HeaderFont);
        drawString("Time now", 0.5, 0.045, HeaderTextColor, HeaderTimeNowFont);
        drawStringR(TimeFormat.format(timeNow), 0.96, 0.045, HeaderTextColor, HeaderFont);

        DepartureData d0 = null;
        DepartureData d1 = null;
        DepartureData d2 = null;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
            if (DepartureData.size() > 1) {
                d1 = DepartureData.get(1);
                if (DepartureData.size() > 2) {
                    d2 = DepartureData.get(2);
                }
            }
        }

        String dueOutString;
        if (d0 != null) {
            double logoOffset = LeftMargin;
            double logoWidth = Line != null && Line.IsNswTrainlink ? 0.08 : 0.11;
            if (LineLogo != null) {
                drawImage(LineLogo, LeftMargin, 0.07);
                logoOffset = LeftMargin + logoWidth + 0.08;
            }
            if (Line != null && Line.IsNswTrainlink) {
                drawString(DueOutFormat.format(d0.DueOut), logoOffset, 0.145, TextColor, LargeDepartureTimeFont);
                drawString(d0.Destination, LeftMargin, 0.22, TextColor, DestinationFont);
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, LeftMargin + 0.006,  0.25, TextColor, Destination2Font);
                }
                top = nswTrainlinkTopOffset;
            } else {
                drawString(d0.Destination, logoOffset, 0.14, TextColor, DestinationFont);
                if (d0.Destination2 != null) {
                    drawString(d0.Destination2, logoOffset + 0.006, 0.17, TextColor, Destination2Font);
                }
            }

            drawStringR(d0.Platform, RightMargin, top + 0.28, TextColor, PlatformDepartsFont);
            if (d0.DueOut != null) {
                Pair<Integer, Integer> dueOut = getDueOut(d0.DueOut);
                int h = dueOut.getValue0();
                int m = dueOut.getValue1();
                if (h > 0 || m > 0) {
                    dueOutString = m + " min";
                    if (h > 0) {
                        dueOutString = h + " hr " + dueOutString;
                    }
                    drawStringR(dueOutString, RightMargin, 0.78, TextColor, PlatformDepartsFont);
                }
            }
            double x = drawMiniTextBox(g, LeftMargin, 0.75, d0.Cars + " cars");
            if (d0.Type != null && d0.Type.length() > 0) {
                drawMiniTextBox(g, LeftMargin + x + 0.015, 0.75, d0.Type);
            }
        }

        drawStringR("Platform", RightMargin, top + 0.22, TextColor, PlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin,  0.72, TextColor, PlatformDepartsLabelFont);

        drawLine(LeftMargin, top + 0.19, RightMargin, top + 0.19, TextColor);
        drawLine(LeftMargin, 0.80, RightMargin, 0.80, TextColor);

        drawString("Next service", LeftMargin, 0.83, TextColor, NextPlatformDepartsLabelFont);
        drawStringC("Platform", 0.55, 0.83, TextColor, NextPlatformDepartsLabelFont);
        drawStringR("Departs", RightMargin, 0.83, TextColor, NextPlatformDepartsLabelFont);
        if (d1 != null) {
            drawNextDeparture(g, d1, 0.87);
        }
        if (d2 != null) {
            drawNextDeparture(g, d2, 0.952);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        DepartureData d0;
        if (DepartureData.size() > 0) {
            d0 = DepartureData.get(0);
        } else return;

        double top = 0;
        if (Line != null && Line.IsNswTrainlink) {
            top = nswTrainlinkTopOffset;
        }

        // Scrolling list
        boolean shouldScroll = d0.Stops.length > 6;
        g.setClip(round(LeftMargin * width), round((top + 0.2) * height), round(0.7 * width), round((0.53 - top) * height));
        fillRect(LeftMargin, top + 0.2, 0.7, top + 0.75, Color.white);
        for (int i = 0; i < d0.Stops.length; i++) {
            int yAbs = round(stationListPos * height) + round(i * stationListSeparation * height);
            int yCutoff = round((1.05 + stationListSeparation) * height);
            drawScrollingStation(g, yAbs, d0.Stops[i]);
            if (yAbs > yCutoff)
                continue;

            // If scrolling, draw a second copy so that one list scrolls seamlessly into the next
            if (shouldScroll) {
                yAbs = round(stationListPos * height) + round((i + d0.Stops.length + 5) * stationListSeparation * height);
                if (yAbs <= yCutoff)
                    drawScrollingStation(g, yAbs, d0.Stops[i]);
            }
        }
        g.setClip(0, 0, width, height);

        if (shouldScroll) {
            stationListPos -= (stationListInc * realFPSAdjustment);
            if (stationListPos < (-1 * (d0.Stops.length + 5) * stationListSeparation)) {
                stationListPos += (d0.Stops.length + 5) * stationListSeparation;
            }
        }
    }

    private void drawScrollingStation(Graphics g, int yAbs, DepartureData.Stop stop) {
        drawString(stop.Name, LeftMargin, yAbs, TextColor, MainFont);

        if (stop.Airport || stop.CarRange != null) {
            double xOffset = (double)g.getFontMetrics(MainFont).stringWidth(stop.Name) / width + 0.02;
            yAbs = yAbs - round(stationListSeparation * 0.6 * height);
            // Airport icon
            if (stop.Airport) {
                drawImage(airportIcon, LeftMargin + xOffset, yAbs);
                xOffset += AirportIconWidth + 0.02;
            }
            // Car range
            if (stop.CarRange != null)  {
                drawCarRangeTextBox(g, LeftMargin + xOffset, yAbs, stop.CarRange);
            }
        }

    }

    private void drawNextDeparture(Graphics g, DepartureData d, double y)
    {
        drawString(d.Destination, LeftMargin, y, TextColor, NextDestinationFont);
        drawStringC(d.Platform, 0.55, y, TextColor, NextDestinationFont);
        if (d.DueOut != null) {
            Pair<Integer, Integer> dueOut = getDueOut(d.DueOut);
            int h = dueOut.getValue0();
            int m = dueOut.getValue1();
            String dueOutString = m + " min";
            if (h > 0) {
                dueOutString = h + " hr " + dueOutString;
            }
            drawStringR(dueOutString, RightMargin, y, TextColor, NextDestinationFont);
        }

        double textboxOffset = 0;
        if (d.Destination2 != null && d.Destination2.length() > 0) {
            textboxOffset = (double)g.getFontMetrics(NextDestination2Font).stringWidth(d.Destination2) / width + 0.02;
            drawString(d.Destination2, LeftMargin, y + 0.032, TextColor, NextDestination2Font);
        }
        textboxOffset += drawMiniMiniTextBox(g, LeftMargin + textboxOffset, y + 0.01, d.Cars + " cars") + 0.02;
        if (d.Type != null && d.Type.length() > 0) {
            drawMiniMiniTextBox(g, LeftMargin + textboxOffset, y + 0.01, d.Type);
        }
    }

    protected double drawMiniTextBox(Graphics g, double x, double y, String s)
    {
        int widthPx = g.getFontMetrics(TextBoxFont).stringWidth(s);
        double w = (double)widthPx / this.width + 0.02;
        double h = 0.036;
        fillRect(x, y, x + w, y + h, TextBoxColor);
        drawString(s, x + 0.01, y + h - 0.012, Color.white, TextBoxFont);
        return w;
    }

    protected double drawMiniMiniTextBox(Graphics g, double x, double y, String s)
    {
        int widthPx = g.getFontMetrics(MiniTextBoxFont).stringWidth(s);
        double w = (double)widthPx / this.width + 0.02;
        double h = 0.03;
        fillRect(x, y, x + w, y + h, TextBoxColor);
        drawString(s, x + 0.01, y + h - 0.012, Color.white, MiniTextBoxFont);
        return w;
    }

    protected void drawCarRangeTextBox(Graphics g, double x, int yAbs, String s)
    {
        double w = 0.15;
        double h = 0.036;
        int textYRelative = round(0.025 * height);
        int arcSize = round(0.012 * height);
        g.setColor(TextBoxColor);
        g.fillRoundRect(round(x * width), yAbs, round(w * width), round(h * height), arcSize, arcSize);
        drawStringC(s, x + (w / 2), yAbs + textYRelative, Color.white, TextBoxFont);
    }
}