package router;

import java.util.*;

import db.TDSImplement;
import models.*;
import util.*;

import static router.EdgeService.getEdges;
import static util.DebugUtil.*;
import static util.TimeUtil.*;

public class AStarRouterV {
    Map<String, Double> bestCosts = new HashMap<>();
    /**
     * Finds the fastest path between two geographic coordinates using A* algorithm.
     * @param latStart Latitude of the starting point.
     * @param lonStart Longitude of the starting point.
     * @param latStop Latitude of the destination point.
     * @param lonStop Longitude of the destination point.
     * @param startTime The time at which the journey starts, in HH:mm format.
     * @param excludedStops List of stop IDs to exclude from the search.
     * @return A list of nodes representing the fastest path from start to stop.
     */
    public List<Node> findFastestPath(double latStart, double lonStart, double latStop, double lonStop, String startTime, List<String> excludedStops) {
        TDSImplement tds = new TDSImplement();
        reset();
        sendInfo("Starting point" + latStart + " " + lonStart);
        Node STARTING_NODE = new Node("start", startTime, null, "WALK", null);
        STARTING_NODE.stop = new Stop("start", "STARTING POINT", latStart, lonStart);
        ArrayList<Stop> startStops = tds.getNearbyStops(latStart, lonStart, 1500);
        sendInfo("Start stops: " + startStops.size());
        ArrayList<Stop> stopStops = tds.getNearbyStops(latStop, lonStop, 1500);
        Node STOP_NODE = new Node("stop", null, null, "WALK", null);
        STOP_NODE.stop = new Stop("stop", "END_POINT", latStop, lonStop);
        sendInfo("Stop stops: " + stopStops.size());

        double walkingTimeOnly = WalkingTime.getWalkingTime(latStart, lonStart, latStop, lonStop);
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.getG() + n.getH()));

        for (Stop stop : startStops) {
            if (excludedStops.contains(stop.getStopId())) {
                sendInfo("Excluding start stop: " + stop.getStopId());
                continue;
            }
            double walkTime = WalkingTime.getWalkingTime(latStart, lonStart, stop.getStopLat(), stop.getStopLon());
            sendInfo("Walking time to stop: " + stop.getStopId() + " " + walkTime);
            String arrivalTime = addTime(startTime, walkTime);
            Node node = new Node(stop.getStopId(), arrivalTime, STARTING_NODE, "WALK", null);
            node.g = walkTime;
            updateBestCost(stop.getStopId(), node.getG());
            pq.add(node);
        }

        Node bestGoalNode = null;
        double bestGoalCost = Double.MAX_VALUE;

        while (!pq.isEmpty()) {
            Node current = pq.peek();

            if (bestGoalNode != null && (current.getG() + current.getH()) > bestGoalCost) {
                break;
            }

            current = pq.poll();

            if (isAtGoal(current, stopStops)) {
                assert current != null;
                double estimatedTotalCost = current.getG();
                if (estimatedTotalCost < bestGoalCost) {
                    bestGoalCost = estimatedTotalCost;
                    bestGoalNode = current;
                }
                continue;
            }

            assert current != null;
            List<Edge> edges = getEdges(current, 0);

            for (Edge edge : edges) {
                String toStopId = edge.getToStopId();
                if (excludedStops.contains(toStopId)) {
                   sendInfo("Excluding stop: " + toStopId);
                    continue;
                }
                String arrivalTime = edge.getArrivalTime();
                double weight = edge.getWeight();

                if (current.getG() + weight + 5 < bestKnownCostTo(toStopId)) {
                    Node nextNode = new Node(toStopId, arrivalTime, current, edge.getMode(), edge.getTrip());
                    double latitude = nextNode.getStop().getStopLat();
                    double longitude = nextNode.getStop().getStopLon();
                    nextNode.g = current.getG() + weight;
                    nextNode.h = GeoUtil.distance(latitude, longitude, latStop, lonStop) / 2.8;
                    pq.add(nextNode);
                    updateBestCost(toStopId, nextNode.getG());
                }
            }
        }

        if (bestGoalNode != null) {
            Stop currentStop = tds.getStop(bestGoalNode.getStopId());
            double walking_time_to_end = WalkingTime.getWalkingTime(
                    currentStop.getStopLat(),
                    currentStop.getStopLon(),
                    STOP_NODE.getStop().getStopLat(),
                    STOP_NODE.getStop().getStopLon());

            double totalAStarTime = bestGoalNode.getG() + walking_time_to_end;

            sendInfo("A* total time: " + totalAStarTime);
            sendInfo("Direct walking time: " + walkingTimeOnly);

            if (walkingTimeOnly <= totalAStarTime) {
                sendInfo("Returning direct walk as it is faster");
                STOP_NODE.parent = STARTING_NODE;
                STOP_NODE.arrivalTime = addTime(startTime, walkingTimeOnly);
            } else {
                STOP_NODE.parent = bestGoalNode;
                STOP_NODE.arrivalTime = addTime(bestGoalNode.getArrivalTime(), walking_time_to_end);
            }
            List<Node> rawPath = reconstructPath(STOP_NODE);
            return PathCompressor.compressWalks(rawPath);
        }

        // If no goal node was found, return a walk-only path
        sendInfo("No transit path found — falling back to walk-only route");
        STOP_NODE.parent = STARTING_NODE;
        STOP_NODE.arrivalTime = addTime(startTime, walkingTimeOnly);
        List<Node> rawPath = reconstructPath(STOP_NODE);
        return PathCompressor.compressWalks(rawPath);
    }

    /**
     * Retrieves the best known cost to a specific stop.
     * @param stopId The ID of the stop.
     * @return The best known cost to the stop, or Double.MAX_VALUE if not found.
     */
    public double bestKnownCostTo(String stopId) {
        return bestCosts.getOrDefault(stopId, Double.MAX_VALUE);
    }

    public void updateBestCost(String stopId, double cost) {
        bestCosts.put(stopId, cost);
    }

    /**
     * Checks if the current node is at one of the goal stops.
     * @param current The current node being evaluated.
     * @param stopStops The list of goal stops.
     * @return true if the current node is at a goal stop, false otherwise.
     */
    private boolean isAtGoal(Node current, List<Stop> stopStops) {
        for (Stop stop : stopStops) {
            if (current.getStopId().equals(stop.getStopId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reconstructs the path from the end node to the start node.
     * @param current The end node of the path.
     * @return A list of nodes representing the path from start to end.
     */
    private List<Node> reconstructPath(Node current) {
        List<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    public void reset() {
        bestCosts.clear();
    }

}
