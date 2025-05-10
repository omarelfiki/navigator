package util;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class EdgeService {
    TDSImplement tds = new TDSImplement();
    TimeUtil timeUtil = new TimeUtil();
    public ArrayList<Edge> getEdges(Node node) {
        ArrayList<Edge> edges = new ArrayList<>();
        Stop startStop = tds.getStop(node.stopId);

        // add stops that can be reached by walking
        List<Stop> walkingDistanceStops = NearbyStops.getNearbyStops(startStop.stopLat, startStop.stopLon, 350);
        //create edges for each of these stops
        for (Stop stop : walkingDistanceStops) {
            if (!stop.stopId.equals(node.stopId)) {
                WalkingEdge edge = new WalkingEdge(
                        startStop.stopId,
                        stop.stopId,
                        node.arrivalTime
                );
                edges.add(edge);
            }
        }

        // add the stop that can be reached directly by following the same route
        if(node.trip!=null) {
            TripEdge tripEdge = new TripEdge(
                    startStop.stopId,
                    node.arrivalTime,
                    node.trip
            );
            edges.add(tripEdge);
        }

        System.out.println("getting upcoming trips for " + node.stopId + " at " + node.arrivalTime);
        //add transfer edges
        List<Trip> upcomingTrips = tds.getUpcomingDistinctRouteTrips(node.stopId, node.arrivalTime);
        System.out.println("upcoming trips: " + upcomingTrips.size());
        for (Trip trip : upcomingTrips) {
            String departureTime = node.arrivalTime;
            TransferEdge transferEdge = new TransferEdge(
                    startStop.stopId,
                    departureTime,
                    trip
            );
            System.out.println("Transfer edge: " + transferEdge.fromStopId + " to " + transferEdge.toStopId + " with weight " + transferEdge.weight);
            edges.add(transferEdge);
        }

        return edges;
    }
}