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
    
    public long Size;
    public Date LastModified;
    public Map<String,List<String>> Headers;
}
