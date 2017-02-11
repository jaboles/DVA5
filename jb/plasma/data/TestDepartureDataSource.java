package jb.plasma.data;

import java.util.List;

/**
 * Created by jb on 11/2/17.
 */
public class TestDepartureDataSource implements IDepartureDataSource
{
    private List<DepartureData> departureData;

    public TestDepartureDataSource(List<DepartureData> departureData) {
        this.departureData = departureData;
    }

    @Override
    public void notifyDeparture() {

    }

    @Override
    public List<DepartureData> getDepartureData() {
        return departureData;
    }
}
