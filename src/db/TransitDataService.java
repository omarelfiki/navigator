
import java.util.List;

public interface TransitDataService {
    Stop getStop(String stopId);
    List<Stop> getAllStops();
    List<StopTime> getStopTimesForTrip(String tripId);
    List<StopTime> getStopTimesForStop(String stopId);
    List<StopTime> getFutureDepartures(String stopId, int afterTime);
    Trip getTrip(String tripId);
    Route getRoute(String routeId);
}
