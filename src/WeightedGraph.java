import java.util.*;

class Node {
    Stop currentStop;
    List<Edges> nextStops;

    public Node(Stop stop, List<Edges> nextStops) {
        this.currentStop = stop;
        this.nextStops = nextStops;
    }

    public Stop getCurrentStop() {
        return currentStop;
    }

    public void setCurrentStop(Stop currentStop) {
        this.currentStop = currentStop;
    }

    public List<Edges> getNextStops() {
        return nextStops;
    }

    public void setNextStops(List<Edges> nextStops) {
        this.nextStops = nextStops;
    }
}

class Edges {
    Stop stop;
    double weight;

    public Edges(Stop stop, double weight) {
        this.stop = stop;
        this.weight = weight;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}

public class WeightedGraph {
    List<Node> nodes;
    HashMap<Stop, List<Edges>> edges;

    public WeightedGraph(List<Node> nodes, HashMap<Stop, List<Edges>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public HashMap<Stop, List<Edges>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<Stop, List<Edges>> edges) {
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    // 📌 Dijkstra's Algorithm Method
    public List<Stop> findShortestPath(Stop start, Stop target) {
        Map<Stop, Double> distances = new HashMap<>();
        Map<Stop, Stop> previous = new HashMap<>();
        PriorityQueue<StopDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(sd -> sd.distance));

        // Initialize distances
        for (Node node : nodes) {
            distances.put(node.getCurrentStop(), Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.add(new StopDistance(start, 0.0));

        while (!queue.isEmpty()) {
            StopDistance current = queue.poll();
            Stop currentStop = current.stop;

            if (currentStop.equals(target)) {
                break; // Found shortest path
            }

            List<Edges> neighbors = edges.getOrDefault(currentStop, new ArrayList<>());
            for (Edges edge : neighbors) {
                Stop neighbor = edge.getStop();
                double newDist = distances.get(currentStop) + edge.getWeight();
                if (newDist < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, currentStop);
                    queue.add(new StopDistance(neighbor, newDist));
                }
            }
        }

        // Reconstruct path
        List<Stop> path = new LinkedList<>();
        for (Stop at = target; at != null; at = previous.get(at)) {
            path.add(0, at);
        }

        if (!path.isEmpty() && path.get(0).equals(start)) {
            return path;
        } else {
            return Collections.emptyList(); // No path found
        }
    }

    // 📌 Helper class for priority queue inside WeightedGraph
    static class StopDistance {
        Stop stop;
        double distance;

        public StopDistance(Stop stop, double distance) {
            this.stop = stop;
            this.distance = distance;
        }
    }
    public static void main(String[] args) {
        // Step 1: Create Stops
        Stop stopA = new Stop("A","termini",0,0);
        Stop stopB = new Stop("B","colosseo",0,0);
        Stop stopC = new Stop("C","pazza dei fiori",0,0);
        Stop stopD = new Stop("D","termini2",0,0);
        Stop stopE = new Stop("E","anfiteatro",0,0);

        // Step 2: Create Edges (connections between stops)
        Edges edgeAB = new Edges(stopB, 5);  // A -> B (5 minutes)
        Edges edgeAC = new Edges(stopC, 10); // A -> C (10 minutes)
        Edges edgeBD = new Edges(stopD, 15); // B -> D (15 minutes)
        Edges edgeCD = new Edges(stopD, 5);  // C -> D (5 minutes)
        Edges edgeDE = new Edges(stopE, 10); // D -> E (10 minutes)

        // Step 3: Create Nodes
        Node nodeA = new Node(stopA, Arrays.asList(edgeAB, edgeAC));
        Node nodeB = new Node(stopB, Collections.singletonList(edgeBD));
        Node nodeC = new Node(stopC, Collections.singletonList(edgeCD));
        Node nodeD = new Node(stopD, Collections.singletonList(edgeDE));
        Node nodeE = new Node(stopE, new ArrayList<>()); // E has no outgoing edges

        // Step 4: Build the edges map
        HashMap<Stop, List<Edges>> edges = new HashMap<>();
        edges.put(stopA, Arrays.asList(edgeAB, edgeAC));
        edges.put(stopB, Collections.singletonList(edgeBD));
        edges.put(stopC, Collections.singletonList(edgeCD));
        edges.put(stopD, Collections.singletonList(edgeDE));
        edges.put(stopE, new ArrayList<>()); // E has no outgoing edges

        // Step 5: Create the graph
        WeightedGraph graph = new WeightedGraph(new ArrayList<>(Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeE)), edges);

        // Step 6: Run Dijkstra's shortest path
        List<Stop> path = graph.findShortestPath(stopA, stopE);

        // Step 7: Print the path
        System.out.println("Shortest path from A to E:");
        if (path.isEmpty()) {
            System.out.println("No path found.");
        } else {
            for (Stop s : path) {
                System.out.print(s.getStopId() + " ");
            }
            System.out.println(); // New line at the end
        }
    }
}
