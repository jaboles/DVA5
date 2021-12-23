package jb.dva;
import java.io.Serializable;
import java.net.URL;

public class SoundReference implements Serializable {
    private static final long serialVersionUID = 3L;
    
    public SoundReference copy()
    {
        SoundReference other = new SoundReference();
        other.canonicalName = canonicalName;
        other.regular = regular;
        other.rising = rising;
        other.falling = falling;
        other.isFallback = isFallback;
        return other;
    }
    
    public String canonicalName;
    public URL regular;
    public URL rising;
    public URL falling;
    public boolean isFallback;
    
    public String toString()
    {
        return canonicalName;
    }
}