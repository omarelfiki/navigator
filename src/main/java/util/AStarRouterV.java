package util;

import java.util.*;
import models.*;
public class AStarRouterV {
    Map<String, Double> bestCosts = new HashMap<>();

    public List<Node> findFastestPath(double latStart, double lonStart, double latStop, double lonStop, String startTime) {
        System.out.println("Starting point" + latStart + " " + lonStart);
        ArrayList<Stop> startStops = NearbyStops.getNearbyStops(latStart, lonStart, 200);
        System.out.println("Start stops: " + startStops.size());
        ArrayList<Stop> stopStops = NearbyStops.getNearbyStops(latStop, lonStop, 200);
        EdgeService edgeService = new EdgeService();
        TimeUtil timeUtil = new TimeUtil();

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g + n.h));

        for (Stop stop : startStops) {
            double walkTime = WalkingTime.getWalkingTime(latStart, lonStart, stop.getStopLat(), stop.getStopLon());
            String arrivalTime = timeUtil.addTime(startTime, walkTime);
            Node node = new Node(stop.stopId, arrivalTime, null, "walk", null);
            pq.add(node);
        }

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            System.out.println("Current node: " + current.stopId + " Arrival time: " + current.arrivalTime);
            if (isAtGoal(current, stopStops)) {
                return reconstructPath(current);
            }

            List<Edge> edges = edgeService.getEdges(current);

            for(Edge edge : edges){
                String toStopId = edge.getToStopId();
                String arrivalTime = edge.getArrivalTime();
                double weight = edge.getWeight();

                if (current.g + weight < bestKnownCostTo(toStopId)) {

                    Node nextNode = new Node(toStopId, arrivalTime, current, edge.getMode(), edge.getTrip());
                    double latitude = nextNode.stop.getStopLat();
                    double longitude = nextNode.stop.getStopLon();
                    nextNode.g = current.g + weight;
                    nextNode.h = WalkingTime.getWalkingTime(latitude, longitude, latStop, lonStop);
                    pq.add(nextNode);
                    updateBestCost(toStopId, nextNode.g);
                }
            }
        }
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
