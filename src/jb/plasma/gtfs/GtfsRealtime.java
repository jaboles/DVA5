package jb.plasma.gtfs;

import com.google.transit.realtime.GtfsRealtime1007Extension;

import java.io.IOException;

public class GtfsRealtime
{
    public static void main(String[] args)
    {
        try
        {
            get();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static GtfsRealtime1007Extension.FeedMessage get() throws IOException
    {
        return GtfsRealtime1007Extension.FeedMessage.parseFrom(GtfsHttpClient.getByteArray("https://api.transport.nsw.gov.au/v2/gtfs/realtime/sydneytrains"));
    }
}
