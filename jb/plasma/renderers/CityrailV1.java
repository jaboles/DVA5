package jb.plasma.renderers;
import java.awt.Color;

// The Cityrail V1 indicator (black/blue/turquoise plasma screens)
public abstract class CityrailV1 extends Cityrail
{
    protected static Color SideColor = new Color(0, 75, 150);
    protected static Color MiddleColor = new Color(0, 30, 62);
    protected static Color BackgroundColor = new Color(0, 0, 0);
    protected static Color TextYellow = Color.yellow;
    protected static Color TextWhite = Color.white;

    protected double TopOffset;
    protected double BottomOffset;
    protected double LeftOffset;
    protected double RightOffset;

    protected double stationListSeparation;

    protected boolean isv11;
    protected boolean isConcourse;

    public CityrailV1(boolean isv11, boolean isConcourse) {
        this.isv11 = isv11;
        this.isConcourse = isConcourse;
    }
}