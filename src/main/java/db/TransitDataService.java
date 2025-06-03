package db;

import models.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public interface TransitDataService {
    Stop getStop(String stopId);
    List<Stop> getAllStops();
    List<StopTime> getStopTimesForTrip(String tripId);
    List<StopTime> getStopTimesForStop(String stopId);
    List<StopTime> getFutureDepartures(String stopId, Time afterTime);
    ArrayList<Stop> getNearbyStops(double lat, double lon, double radiusMeters);
    Trip getTrip(String tripId);
    Route getRoute(String routeId);
}
