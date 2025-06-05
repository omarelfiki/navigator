package closureAnalysis;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

import java.util.*;
import java.util.stream.Collectors;

public class StopConnectivityAnalyzer {
    private final TDSImplement tds = new TDSImplement();
    public Map<String, Integer> analyzeStopConnectivity(List<Stop> stops) {
        Map<String, Set<String>> connections = new HashMap<>();
        for (Stop stop : stops) {
            connections.put(stop.getStopId(), new HashSet<>());
        }
        List<Trip> allTrips = tds.getAllTrips();
        for (Trip trip : allTrips) {
            List<StopTime> stopTimes = tds.getStopTimesForTrip(trip.tripId());
            stopTimes.sort(Comparator.comparingInt(StopTime::stopSequence));
            for (int i = 0; i < stopTimes.size() - 1; i++) {
                String currentStopId = stopTimes.get(i).stop().getStopId();
                String nextStopId = stopTimes.get(i + 1).stop().getStopId();
                if (connections.containsKey(currentStopId) && connections.containsKey(nextStopId)) {
                    connections.get(currentStopId).add(nextStopId);
                    connections.get(nextStopId).add(currentStopId); // Undirected graph
                }
            }
        }

        Map<String, Integer> connectionCounts = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : connections.entrySet()) {
            connectionCounts.put(entry.getKey(), entry.getValue().size());
        }
        return connectionCounts;
    }
}

