package jb.plasma;

import java.awt.Dimension;

public class NullDrawer extends Drawer
{
    @Override
    public void dimensionsChanged()
    {
    }

    @Override
    public int getAspectRatio()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return "None";
    }
}
