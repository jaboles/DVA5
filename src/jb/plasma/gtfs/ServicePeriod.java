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

    public String Id;
    public boolean Monday;
    public boolean Tuesday;
    public boolean Wednesday;
    public boolean Thursday;
    public boolean Friday;
    public boolean Saturday;
    public boolean Sunday;
    public LocalDate StartDate;
    public LocalDate EndDate;
}
