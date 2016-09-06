package jb.plasma;

import java.awt.Dimension;

public class NullDrawer extends Drawer
{
    @Override
    public void dimensionsChanged()
    {
    }

    @Override
    public Dimension getAspectRatio()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "None";
    }
}
