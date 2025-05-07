import java.util.*;

public class Graph {
    public final Map<String, Node> nodes = new HashMap<>();
    public final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public void addEdge(Node fromNode, Edge edge) {
        adjacencyList.computeIfAbsent(fromNode.stopId, k -> new ArrayList<>()).add(edge);
        nodes.putIfAbsent(fromNode.stopId, fromNode);
        nodes.putIfAbsent(edge.getToStopId(), new Node(edge.getToStopId(), edge.getArrivalTime(), edge.getTripInfo()));
    }

    public List<Edge> getEdges(String stopId) {
        return adjacencyList.getOrDefault(stopId, Collections.emptyList());
    }

    private double toDouble(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]) / 60.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            String from = entry.getKey();
            sb.append("From ").append(from).append(":\n");
            for (Edge edge : entry.getValue()) {
                sb.append("  -> ").append(edge.getToStopId())
                        .append(" [mode=").append(edge.getMode())
                        .append(", dep=").append(edge.getDepartureTime())
                        .append(", arr=").append(edge.getArrivalTime())
                        .append(", weight=").append(edge.getWeight());
                if (edge.getTripInfo() != null) {
                    sb.append(", trip_id=").append(edge.getTripInfo().tripId);
                } else if (edge.getDistanceKm()!=null) {
                    sb.append(", distance=").append(edge.getDistanceKm());
                }
                sb.append("]\n");
            }
        }
        return sb.toString();
    }
}