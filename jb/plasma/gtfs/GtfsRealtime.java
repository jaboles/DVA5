package jb.plasma.gtfs;

import com.google.transit.realtime.GtfsRealtime1007Extension;

public class GtfsRealtime
{
    public static void main(String[] args) {
        GtfsRealtime.get();
    }

    public static void get()
    {
        try
        {
            GtfsRealtime1007Extension.FeedMessage msg = GtfsRealtime1007Extension.FeedMessage.parseFrom(GtfsHttpClient.getByteArray("https://api.transport.nsw.gov.au/v2/gtfs/realtime/sydneytrains"));
            for (GtfsRealtime1007Extension.FeedEntity e : msg.getEntityList())
            {
                GtfsRealtime1007Extension.TripUpdate tu = e.getTripUpdate();
                if (tu != null)
                {
                    System.out.println(tu.getTrip().getTripId());
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
