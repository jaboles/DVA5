package jb.plasma.renderers;
import java.awt.Color;
import java.time.format.DateTimeFormatter;

// The Cityrail V1 indicator (black/blue/turquoise plasma screens)
public abstract class CityrailV1 extends Cityrail
{
    protected static final Color SideColor = new Color(0, 75, 150);
    protected static final Color MiddleColor = new Color(0, 30, 62);
    protected static final Color BackgroundColor = new Color(0, 0, 0);
    protected static final Color TextYellow = Color.yellow;
    protected static final Color TextWhite = Color.white;
    protected static final DateTimeFormatter DueOutFormat = DateTimeFormatter.ofPattern("HH:mm");

    protected double TopOffset;
    protected double BottomOffset;
    protected double LeftOffset;
    protected double RightOffset;

    protected double stationListSeparation;

    protected final boolean isv11;
    protected final boolean isConcourse;

    public CityrailV1(boolean isv11, boolean isConcourse) {
        this.isv11 = isv11;
        this.isConcourse = isConcourse;
    }
}