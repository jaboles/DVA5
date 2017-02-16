package jb.plasma.data;

import jb.plasma.data.DepartureData;

import java.util.List;

public interface IDepartureDataSource
{
    void notifyDeparture();
    List<DepartureData> getDepartureData();
}
