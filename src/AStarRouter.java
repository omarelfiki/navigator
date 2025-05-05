//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.PriorityQueue;
//
//public class AStarRouter {
//    public List<Edge> findFastestPath(LatLon origin, LatLon destination, int startTimeInSeconds){
//    PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g + n.h));
//        List<String> nearbyStopIds = walkingService.findNearbyStops(origin, radiusMeters);
//        List<Node> startingNodes = new ArrayList<>();
//        for(String stopId : nearbyStopIds) {
//            int arrivalTime = WalkingTime.getWalkingTime();
//            Node node = new Node(stopId, arrivalTime, null, "walk", null);
//            node.mode = "WALK";
//            startingNodes.add(node);
//        }
//        pq.addAll(startingNodes);
//
//        while (!pq.isEmpty()) {
//            Node current = pq.poll();
//
//            if (isAtGoal(current)) {
//                return reconstructPath(current);
//            }
//
//            List<Edge> neighbors = edgeService.getNeighbors(current, destination);
//
//            for (Edge edge : neighbors) {
//                int cost = edge.arrivalTime - current.arrivalTime;
//                int newG = current.g + cost;
//
//                if (newG < bestKnownCostTo(edge.toStopId, edge.arrivalTime)) {
//                    double h = walkingService.getWalkingTime(edge.toCoords, destination);
//                    Node next = new Node(edge.toStopId, edge.arrivalTime, newG, h, current);
//                    pq.add(next);
//                    updateBestCost(edge.toStopId, edge.arrivalTime, newG);
//                }
//            }
//        }
//
//    }
//
//
//}
