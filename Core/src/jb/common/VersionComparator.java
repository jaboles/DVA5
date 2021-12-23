package jb.common;

import java.util.Comparator;

public class VersionComparator implements Comparator<String>
{
    public static final VersionComparator Instance = new VersionComparator();

    public int compare(String v1, String v2)
    {
        return Utilities.compareVersion(v1, v2);
    }
}