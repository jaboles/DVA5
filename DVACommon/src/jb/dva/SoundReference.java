package jb.dva;
import java.io.Serializable;
import java.net.URL;

public class SoundReference implements Serializable {
    private static final long serialVersionUID = 4L;

    public SoundReference copy()
    {
        SoundReference other = new SoundReference();
        other.canonicalName = canonicalName;
        other.beginning = beginning;
        other.regular = regular;
        other.ending = ending;
        other.isFallback = isFallback;
        return other;
    }

    public String canonicalName;
    public URL beginning;
    public URL regular;
    public URL ending;
    public boolean isFallback;

    public String toString()
    {
        return canonicalName;
    }
}