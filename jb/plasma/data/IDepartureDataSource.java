package jb.plasma.data;

import java.util.List;

public interface IDepartureDataSource
{
    void notifyDeparture();
    List<DepartureData> getDepartureData();
}
