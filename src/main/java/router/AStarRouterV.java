package router;

import java.util.*;

import db.NearbyStops;
import db.TDSImplement;
import models.*;
import util.*;

import static util.TimeUtil.*;

public class AStarRouterV {
    Map<String, Double> bestCosts = new HashMap<>();

    boolean debugMode = false;

    public List<Node> findFastestPath(double latStart, double lonStart, double latStop, double lonStop, String startTime, List<String> excludedStops) {
        if (debugMode) System.err.println("Starting point" + latStart + " " + lonStart);
        Node STARTING_NODE = new Node("start", startTime, null, "WALK", null);
        STARTING_NODE.stop = new Stop("start", "STARTING POINT", latStart, lonStart);
        ArrayList<Stop> startStops = NearbyStops.getNearbyStops(latStart, lonStart, 500);
        if (debugMode) System.err.println("Start stops: " + startStops.size());
        ArrayList<Stop> stopStops = NearbyStops.getNearbyStops(latStop, lonStop, 500);
        Node STOP_NODE = new Node("stop", "12:00:00", null, "WALK", null);
        STOP_NODE.stop = new Stop("stop", "END_POINT", latStop, lonStop);
        if (debugMode) System.err.println("Stop stops: " + stopStops.size());
        EdgeService edgeService = new EdgeService();

        double walkingTimeOnly = WalkingTime.getWalkingTime(latStart, lonStart, latStop, lonStop);
        System.err.println("Walking time only: " + walkingTimeOnly);
        if(walkingTimeOnly<420){
            STOP_NODE.parent = STARTING_NODE;
            return reconstructPath(STOP_NODE);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g + n.h));

        for (Stop stop : startStops) {
            if (excludedStops.contains(stop.getStopId())) {
                if (debugMode) System.err.println("Excluding start stop: " + stop.getStopId());
                continue;
            }
            double walkTime = WalkingTime.getWalkingTime(latStart, lonStart, stop.getStopLat(), stop.getStopLon());
            if (debugMode) System.err.println("Walking time to stop: " + stop.getStopId() + " " + walkTime);
            String arrivalTime = addTime(startTime, walkTime);
            Node node = new Node(stop.stopId, arrivalTime, STARTING_NODE, "WALK", null);
            node.g = walkTime;
            updateBestCost(stop.stopId, node.g);
            pq.add(node);
        }

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (isAtGoal(current, stopStops)) {
                STOP_NODE.parent = current;
                TDSImplement tds = new TDSImplement();
                Stop currentStop = tds.getStop(current.stopId);

                double walking_time_to_end = WalkingTime.getWalkingTime(currentStop.getStopLat(), currentStop.getStopLon(), STOP_NODE.stop.getStopLat(), STOP_NODE.stop.getStopLon());
                STOP_NODE.arrivalTime = addTime(current.arrivalTime, walking_time_to_end);
                return reconstructPath(STOP_NODE);
            }

            List<Edge> edges = edgeService.getEdges(current);

            for (Edge edge : edges) {
                String toStopId = edge.getToStopId();
                if (excludedStops.contains(toStopId)) {
                    if (debugMode) System.err.println("Excluding stop: " + toStopId);
                    continue;
                }
                String arrivalTime = edge.getArrivalTime();
                double weight = edge.getWeight();

                if (current.g + weight + 5 < bestKnownCostTo(toStopId)) {
                    Node nextNode = new Node(toStopId, arrivalTime, current, edge.getMode(), edge.getTrip());
                    double latitude = nextNode.stop.getStopLat();
                    double longitude = nextNode.stop.getStopLon();
                    nextNode.g = current.g + weight;

                    nextNode.h = GeoUtil.distance(latitude, longitude, latStop, lonStop) / 2.8;
                    pq.add(nextNode);
                    updateBestCost(toStopId, nextNode.g);
                }
            }
        }
        if (debugMode) System.err.println("best costs for visited nodes: " + bestCosts);
        return null;
    }

    public double bestKnownCostTo(String stopId) {
        return bestCosts.getOrDefault(stopId, Double.MAX_VALUE); // Default to infinity if no cost is known
    }

    // Updates the best-known cost for a stop and arrival time
    public void updateBestCost(String stopId, double cost) {
        bestCosts.put(stopId, cost);
    }


    private boolean isAtGoal(Node current, List<Stop> stopStops) {
        for (Stop stop : stopStops) {
            if (current.stopId.equals(stop.getStopId())) {
                return true;
            }
        }
        return false;
    }

    private List<Node> reconstructPath(Node current) {
        List<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public void reset() {
        bestCosts.clear();
    }



}
