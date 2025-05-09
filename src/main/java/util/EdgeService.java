package util;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import java.util.ArrayList;
import java.util.List;

public class EdgeService {
    TDSImplement tds;
    TimeUtil timeUtil;
    public ArrayList<Edge> getEdges(Node node) {
        ArrayList<Edge> edges = new ArrayList<>();
        Stop startStop = tds.getStop(node.stopId);

        // add stops that can be reached by walking
        List<Stop> walkingDistanceStops = NearbyStops.getNearbyStops(startStop.stopLat, startStop.stopLon, 200);
        //create edges for each of these stops
        for (Stop stop : walkingDistanceStops) {
            if (!stop.stopId.equals(node.stopId)) {
                double time = WalkingTime.getWalkingTime(startStop.stopLat, startStop.stopLon, stop.stopLat, stop.stopLon);
                String timeInString = timeUtil.addTime(node.arrivalTime, time);
                WalkingEdge edge = new WalkingEdge(
                        startStop.stopId,
                        stop.stopId,
                        timeInString
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

        //add transfer edges
        List<Trip> upcomingTrips = tds.getUpcomingDistinctRouteTrips(node.stopId, node.arrivalTime);
        for (Trip trip : upcomingTrips) {
            String departureTime = node.arrivalTime;
            TransferEdge transferEdge = new TransferEdge(
                    startStop.stopId,
                    departureTime,
                    trip
            );
            edges.add(transferEdge);
        }
        return edges;
    }
}