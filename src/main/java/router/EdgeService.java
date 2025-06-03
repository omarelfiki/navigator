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
        Stop startStop = tds.getStop(node.stopId);

        if (mode == 0) {
            // add stops that can be reached by walking
            List<Stop> walkingDistanceStops = tds.getNearbyStops(startStop.stopLat, startStop.stopLon, 250);
            //create edges for each of these stops
            for (Stop stop : walkingDistanceStops) {
                if (!stop.stopId.equals(node.stopId)) {
                    WalkingEdge edge = new WalkingEdge(
                            startStop.stopId,
                            stop.stopId,
                            node.arrivalTime
                    );
                    if (isDebugMode) System.out.println("Walking " + edge.fromStopId + " to " + edge.toStopId + " weight " + edge.weight + " at " + edge.departureTime + " to " + edge.arrivalTime);
                    edges.add(edge);
                }
            }
        }

        // add the stop that can be reached directly by following the same route
        if (node.trip != null) {
            try {
                TripEdge tripEdge = new TripEdge(
                        startStop.stopId,
                        node.arrivalTime,
                        node.trip
                );
                edges.add(tripEdge);
            } catch (IllegalArgumentException e) {
                if (isDebugMode) System.err.println("Skipping trip continuation: " + e.getMessage());
            }

        }


        //add transfer edges
        List<Trip> upcomingTrips = tds.getUpcomingDistinctRouteTrips(node.stopId, node.arrivalTime);
        if (isDebugMode) System.err.println("upcoming trips: " + upcomingTrips.size());
        for (Trip trip : upcomingTrips) {
            try {
                TransferEdge transferEdge = new TransferEdge(startStop.stopId, node.arrivalTime, trip);
                edges.add(transferEdge);
                if (isDebugMode) System.err.println("Transfer " + transferEdge.fromStopId + " to " + transferEdge.toStopId +
                        " weight " + transferEdge.weight + " by route " + transferEdge.trip.getRoute().routeId +
                        " at " + transferEdge.departureTime + " waiting until " + transferEdge.rideStartTime +
                        " to " + transferEdge.arrivalTime);
            } catch (IllegalArgumentException e) {
                if (isDebugMode) System.err.println("Skipping trip " + trip.tripId + ": " + e.getMessage());
            }
        }


        return edges;
    }
}