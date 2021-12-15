package jb.plasma.gtfs;

import java.io.Serializable;
import java.time.LocalDate;

public class ServicePeriod implements Serializable
{
    private static final long serialVersionUID = 1L;

    public ServicePeriod(String id, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday,
                    boolean saturday, boolean sunday, LocalDate startDate, LocalDate endDate)
    {
        Id = id;
        Monday = monday;
        Tuesday = tuesday;
        Wednesday = wednesday;
        Thursday = thursday;
        Friday = friday;
        Saturday = saturday;
        Sunday = sunday;
        StartDate = startDate;
        EndDate = endDate;
    }

    public final String Id;
    public final boolean Monday;
    public final boolean Tuesday;
    public final boolean Wednesday;
    public final boolean Thursday;
    public final boolean Friday;
    public final boolean Saturday;
    public final boolean Sunday;
    public final LocalDate StartDate;
    public final LocalDate EndDate;
}