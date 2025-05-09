//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//public class Graph {
//    public final Map<String, Node> nodes;
//    public final Map<String, List<Edge>> adjacencyList;
//
//    public Graph (Map<String, Node> nodes,Map<String, List<Edge>> adjacencyList){
//        this.adjacencyList=adjacencyList;
//        this.nodes=nodes;
//    }
//    public void addEdge(Node fromNode, Edge edge) {
//        adjacencyList.computeIfAbsent(fromNode.stopId, k -> new ArrayList<>()).add(edge);
//        nodes.putIfAbsent(fromNode.stopId, fromNode);
//        nodes.putIfAbsent(edge.getToStopId(), new Node(edge.getToStopId(), edge.getArrivalTime(), edge.getTripInfo()));
//    }
//
//    public List<Edge> getEdges(String stopId) {
//        return adjacencyList.getOrDefault(stopId, Collections.emptyList());
//    }
//
//    public void addNode(String stopId,Node node ){
//        nodes.put(stopId,node);
//
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
//            String from = entry.getKey();
//            sb.append("From ").append(from).append(":\n");
//            for (Edge edge : entry.getValue()) {
//                sb.append("  -> ").append(edge.getToStopId())
//                        .append(" [mode=").append(edge.getMode())
//                        .append(", dep=").append(edge.getDepartureTime())
//                        .append(", arr=").append(edge.getArrivalTime())
//                        .append(", weight=").append(edge.getWeight());
//                if (edge.getTripInfo() != null) {
//                    sb.append(", trip_id=").append(edge.getTripInfo().tripId);
//                } else if (edge.getDistanceKm()!=null) {
//                    sb.append(", distance=").append(edge.getDistanceKm());
//                }
//                sb.append("]\n");
//            }
//        }
//        return sb.toString();
//    }
//}