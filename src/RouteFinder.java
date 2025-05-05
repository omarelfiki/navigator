import java.util.*;
public class RouteFinder {

    public static RouteResult findOptimalTrip(double startLat, double startLon, double endLat, double endLon, DBService dbService) {

        // Get 5 closest stops to start and destination
        List<Stop> startRawStops = dbService.getClosestStops(startLat, startLon, 50);
        List<Stop> endRawStops = dbService.getClosestStops(endLat, endLon, 50);

        List<StopDistance> startStops = new ArrayList<>();
        List<StopDistance> endStops = new ArrayList<>();

        for (Stop stop : startRawStops) {
            double dist = HaversineUtil.calculateDistance(startLat, startLon, stop.getStopLat(), stop.getStopLon());
            startStops.add(new StopDistance(stop, dist));
        }
        for (Stop stop : endRawStops) {
            double dist = HaversineUtil.calculateDistance(endLat, endLon, stop.getStopLat(), stop.getStopLon());
            endStops.add(new StopDistance(stop, dist));
        }

        List<TripMatch> validMatches = new ArrayList<>();

        for (StopDistance start : startStops) {
            for (StopDistance end : endStops) {
                if (start.getStop().getStopId().equals(end.getStop().getStopId())) {
                    continue; // Skip same stop
                }

                Set<Trip> tripsFromStart = new HashSet<>(dbService.getTripsByStop(start.getStop().getStopId()));
                Set<Trip> tripsFromEnd = new HashSet<>(dbService.getTripsByStop(end.getStop().getStopId()));

                tripsFromStart.retainAll(tripsFromEnd); // keep only common trips

                for (Trip trip : tripsFromStart) {
                    List<StopTime> stopTimes = dbService.getStopTimesByTripId(trip.getTripId());

                    StopTime dep = null, arr = null;
                    for (StopTime st : stopTimes) {
                        if (st.getStop().getStopId().equals(start.getStop().getStopId())) {
                            dep = st;
                        }
                        if (st.getStop().getStopId().equals(end.getStop().getStopId())) {
                            arr = st;
                        }
                    }

                    if (dep != null && arr != null && dep.getStopSequence() < arr.getStopSequence()) {
                        validMatches.add(new TripMatch(trip, dep, arr));
                    }
                }
            }
        }

        if (validMatches.isEmpty()) {
            System.out.println("❌ No direct trips found.");
            return null;
        }

        RouteResult bestResult = null;
        int bestTime = Integer.MAX_VALUE;

        for (TripMatch match : validMatches) {
            StopDistance from = new StopDistance(match.departure().getStop(),
                    HaversineUtil.calculateDistance(startLat, startLon,
                            match.departure().getStop().getStopLat(),
                            match.departure().getStop().getStopLon()));

            StopDistance to = new StopDistance(match.arrival().getStop(),
                    HaversineUtil.calculateDistance(endLat, endLon,
                            match.arrival().getStop().getStopLat(),
                            match.arrival().getStop().getStopLon()));

            int walkBefore = (int) from.getWalkingTimeMinutes();
            int walkAfter = (int) to.getWalkingTimeMinutes();
            int transitTime = match.getInTransitMinutes();
            int totalTime = walkBefore + transitTime + walkAfter;

            if (totalTime < bestTime) {
                bestTime = totalTime;
                bestResult = new RouteResult(from, to, match.trip(),
                        match.departure().getDepartureTime(), match.arrival().getArrivalTime(),
                        walkBefore, walkAfter, transitTime);
            }
        }

        return bestResult;
    }
}
