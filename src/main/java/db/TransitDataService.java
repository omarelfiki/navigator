package db;

import closureAnalysis.StopFrequencyData;
import models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TransitDataService {
    Stop getStop(String stopId);
    List<Stop> getAllStops();
    ArrayList<Stop> getNearbyStops(double lat, double lon, double radiusMeters);
    Trip getTrip(String tripId);
    Route getRoute(String routeId);
    public Map<String, StopFrequencyData> getStopFrequencyData();
}
