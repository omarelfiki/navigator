package util;

import java.sql.Time;
import java.util.*;

import db.TDSImplement;
import models.*;
public class AStarRouterV {
    Map<String, Double> bestCosts = new HashMap<>();

    public List<Node> findFastestPath(double latStart, double lonStart, double latStop, double lonStop, String startTime) {
        System.err.println("Starting point" + latStart + " " + lonStart);
        Node STARTING_NODE = new Node("start", startTime, null, "WALK", null);
        STARTING_NODE.stop = new Stop("start","STARTING POINT", latStart, lonStart);
        ArrayList<Stop> startStops = NearbyStops.getNearbyStops(latStart, lonStart, 500);
        System.err.println("Start stops: " + startStops.size());
        ArrayList<Stop> stopStops = NearbyStops.getNearbyStops(latStop, lonStop, 500);
        Node STOP_NODE = new Node("stop","12:00:00",null,"WALK",null);
        STOP_NODE.stop = new Stop("stop","END_POINT", latStop, lonStop);
        System.err.println("Stop stops: " + stopStops.size());
        EdgeService edgeService = new EdgeService();
        TimeUtil timeUtil = new TimeUtil();

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g + n.h));

        for (Stop stop : startStops) {
            double walkTime = WalkingTime.getWalkingTime(latStart, lonStart, stop.getStopLat(), stop.getStopLon());
            System.err.println("Walking time to stop: " + stop.getStopId() + " " + walkTime);
            String arrivalTime = timeUtil.addTime(startTime, walkTime);
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

                double walking_time_to_end = WalkingTime.getWalkingTime(currentStop.getStopLat(),currentStop.getStopLon(),STOP_NODE.stop.getStopLat(),STOP_NODE.stop.getStopLon());
                STOP_NODE.arrivalTime = timeUtil.addTime(current.arrivalTime, walking_time_to_end);
                return reconstructPath(STOP_NODE);
            }

            List<Edge> edges = edgeService.getEdges(current);

            for(Edge edge : edges){
                String toStopId = edge.getToStopId();
                String arrivalTime = edge.getArrivalTime();
                double weight = edge.getWeight();

                if (current.g + weight+5 < bestKnownCostTo(toStopId)) {
                    Node nextNode = new Node(toStopId, arrivalTime, current, edge.getMode(), edge.getTrip());
//                    System.err.println("Next node: " + nextNode.stopId + " from " + nextNode.parent.stopId + " with arrival time " + nextNode.arrivalTime);
                    double latitude = nextNode.stop.getStopLat();
                    double longitude = nextNode.stop.getStopLon();
                    nextNode.g = current.g + weight;

                    nextNode.h = HaversineUtil.calculateDistance(latitude, longitude, latStop, lonStop)/2.8;
                    pq.add(nextNode);
                    updateBestCost(toStopId, nextNode.g);
//                    System.err.println("new best cost for " + toStopId + " is " + nextNode.g);
                }
            }
        }
        System.err.println("best costs for visited nodes: " + bestCosts);
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


}
