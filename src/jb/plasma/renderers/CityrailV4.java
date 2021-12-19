package jb.plasma.renderers;

import java.awt.*;

public abstract class CityrailV4 extends CityrailV4and5
{
    protected static final Color TextColor = new Color(0, 0, 50);

    public void paintInfrequent(Graphics g)
    {
        super.paintInfrequent(g);
        fillRect(0, 0, 1, 1, BackgroundColor);
    }
}