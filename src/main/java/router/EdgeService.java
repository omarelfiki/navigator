package router;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import java.util.ArrayList;
import java.util.List;

import static util.DebugUtil.getDebugMode;

public class EdgeService {
    TDSImplement tds = new TDSImplement();

    boolean isDebugMode;

    public ArrayList<Edge> getEdges(Node node, int mode) {
        isDebugMode = getDebugMode();
        ArrayList<Edge> edges = new ArrayList<>();
        Stop startStop = tds.getStop(node.getStopId());

        //mode 0: with walking, mode 1: without walking
        if (mode == 0) {
            // add stops that can be reached by walking
            List<Stop> walkingDistanceStops = tds.getNearbyStops(startStop.getStopLat(), startStop.getStopLon(), 250);
            //create edges for each of these stops
            for (Stop stop : walkingDistanceStops) {
                if (!stop.getStopId().equals(node.getStopId())) {
                    WalkingEdge edge = new WalkingEdge(
                            startStop.getStopId(),
                            stop.getStopId(),
                            node.getArrivalTime()
                    );
                    edges.add(edge);
                }
            }
        }

        // add the stop that can be reached directly by following the same route
        if (!node.getTrip().tripId().equals("N/A")) {
            try {
                TripEdge tripEdge = new TripEdge(
                        startStop.getStopId(),
                        node.getArrivalTime(),
                        node.getTrip()
                );
                edges.add(tripEdge);
            } catch (IllegalArgumentException e) {
                if (isDebugMode) System.err.println("Skipping trip continuation: " + e.getMessage());
            }

        }


        //add transfer edges
        List<Trip> upcomingTrips = tds.getUpcomingDistinctRouteTrips(node.getStopId(), node.getArrivalTime());
        if (isDebugMode) System.err.println("upcoming trips: " + upcomingTrips.size());
        for (Trip trip : upcomingTrips) {
            try {
                TransferEdge transferEdge = new TransferEdge(startStop.getStopId(), node.getArrivalTime(), trip);
                edges.add(transferEdge);
                if (isDebugMode) System.err.println("Transfer " + transferEdge.getFromStopId() + " to " + transferEdge.getToStopId() +
                        " weight " + transferEdge.getWeight() + " by route " + transferEdge.getTrip().route().routeId() +
                        " at " + transferEdge.getDepartureTime() + " waiting until " + transferEdge.getRideStartTime() +
                        " to " + transferEdge.getArrivalTime());
            } catch (IllegalArgumentException e) {
                if (isDebugMode) System.err.println("Skipping trip " + trip.tripId() + ": " + e.getMessage());
            }
        }


        return edges;
    }
}