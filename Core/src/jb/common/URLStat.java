package jb.common;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class URLStat
{
    public URLStat(long size, Date lastModified, Map<String,List<String>> headers) {
        Size = size;
        LastModified = lastModified;
        Headers = headers;
    }

    public final long Size;
    public final Date LastModified;
    public final Map<String,List<String>> Headers;
}