package router;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static util.DebugUtil.sendInfo;

public class EdgeService {
    public static ArrayList<Edge> getEdges(Node node, int mode) {
        ArrayList<Edge> edges = new ArrayList<>();
        TDSImplement tds = new TDSImplement();
        Stop startStop = Objects.requireNonNullElseGet(tds.getStop(node.getStopId()), Stop::new);

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
                sendInfo("Skipping trip continuation: " + e.getMessage());
            }

        }


        //add transfer edges
        List<Trip> upcomingTrips = tds.getUpcomingDistinctRouteTrips(node.getStopId(), node.getArrivalTime());
        sendInfo("upcoming trips: " + upcomingTrips.size());
        for (Trip trip : upcomingTrips) {
            try {
                TransferEdge transferEdge = new TransferEdge(startStop.getStopId(), node.getArrivalTime(), trip);
                edges.add(transferEdge);
                sendInfo("Transfer " + transferEdge.getFromStopId() + " to " + transferEdge.getToStopId() +
                        " weight " + transferEdge.getWeight() + " by route " + transferEdge.getTrip().route().routeId() +
                        " at " + transferEdge.getDepartureTime() + " waiting until " + transferEdge.getRideStartTime() +
                        " to " + transferEdge.getArrivalTime());
            } catch (IllegalArgumentException e) {
                sendInfo("Skipping trip " + trip.tripId() + ": " + e.getMessage());
            }
        }


        return edges;
    }
}