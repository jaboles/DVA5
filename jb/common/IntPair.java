package jb.common;

import java.io.Serializable;
import java.util.Calendar;

public class IntPair implements Serializable
{
    private static final long serialVersionUID = 1L;
    public final byte first;
    public final byte second;

    public IntPair(int first, int second)
    {
        this((byte)first, (byte)second);
    }

    public IntPair(byte first, byte second)
    {
        this.first = first;
        this.second = second;
    }

    public Calendar asCalendar()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, first);
        c.set(Calendar.MINUTE, second);
        c.set(Calendar.SECOND, 40);
        return c;
    }
}