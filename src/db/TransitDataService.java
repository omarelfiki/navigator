package db;
import java.util.List;

import models.Stop;
import models.StopTime;
import models.Trip;
import models.Route;
public class TransitDataService
{

    public interface TDS {
        Stop getStop2(String stopId);
        List<Stop> getAllStops();
        List<StopTime> getStopTimesForTrip(String tripId);
        List<StopTime> getStopTimesForStop(String stopId);
        List<StopTime> getFutureDepartures(String stopId, int afterTime);
        Trip getTrip(String tripId);
        Route getRoute2(String routeId);
    }
}
