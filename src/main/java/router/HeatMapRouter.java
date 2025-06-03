package router;

import db.TDSImplement;
import models.HeatPoint;
import models.Stop;
import util.DebugUtil;


import java.util.*;

import static util.TimeUtil.addTime;


public class HeatMapRouter {
    private final EdgeService edgeService = new EdgeService();
    private final Map<String, Double> bestCost = new HashMap<>();
    private final Map<String, Node> settled  = new HashMap<>();

    private final boolean debug;
    final double LIMIT = 2700;   // 45-minute ceiling in seconds
    final double SENTINEL = 2701;   // marks “beyond limit”
    private final int type; // 0 = with walk, 1 = without walk
    final TDSImplement tds = new TDSImplement();

    public HeatMapRouter(int type) {
        if (type < 0 || type > 1) {
            throw new IllegalArgumentException("Type must be 0 (with walk) or 1 (without walk)");
        }
        this.type = type;
        this.debug = DebugUtil.getDebugMode();
        if (debug) System.err.println("HeatMapRouter initialized with type: " + type);
    }

    public List<HeatPoint> build(double latStart, double lonStart, String startTime) {
        Map<String, Node> result = builder(latStart, lonStart, startTime);
        return toHeatPoints(result);
    }

    private Map<String, Node> builder(double latStart, double lonStart, String startTime) {
        Node ORIGIN = new Node("origin", startTime, null, "WALK", null);
        ORIGIN.stop = new Stop("origin", "START", latStart, lonStart);
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getG));

        // bootstrap – first walking leg (cap immediately)
        for (Stop s : tds.getNearbyStops(latStart, lonStart, 500)) {
            double walk = WalkingTime.getWalkingTime(latStart, lonStart,
                    s.getStopLat(), s.getStopLon());
            Node n = new Node(s.getStopId(), addTime(startTime, walk), ORIGIN, "WALK", null);
            n.g = Math.min(walk, LIMIT);
            pq.add(n);
            bestCost.put(s.getStopId(), n.getG());
        }

        int totalStops = 5000; // testing cap — remove or replace later

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (settled.containsKey(cur.getStopId())) continue;
            settled.put(cur.getStopId(), cur);

            if (debug && settled.size() % 10_000 == 0)
                System.err.printf("Settled %,d stops …%n", settled.size());

            if (settled.size() == totalStops) break;
            if (cur.getG() >= LIMIT) continue;           // do NOT expand over-limit nodes

            ArrayList<Edge> edges = (type == 0) ? edgeService.getEdges(cur, 0) : edgeService.getEdges(cur, 1);
            for (Edge e : edges) {
                double tentative = cur.getG() + e.getWeight();
                double g = (tentative > LIMIT) ? SENTINEL : tentative;

                String to = e.getToStopId();
                if (g >= bestCost.getOrDefault(to, Double.MAX_VALUE) - 1e-6) continue;

                Node nxt = new Node(to, e.getArrivalTime(), cur, e.getMode(), e.getTrip());
                nxt.g = g;

                pq.add(nxt);
                bestCost.put(to, g);
            }
        }
        return settled;
    }

    public List<HeatPoint> toHeatPoints(Map<String, Node> nodes){
        List<HeatPoint> hp = new ArrayList<>(nodes.size());
        for (Node n : nodes.values()) { //just as a reminder , also hello little Easter egg:)
            hp.add(new HeatPoint(n.getStop().getStopLat(), n.getStop().getStopLon(), n.getG()/60)); // g is seconds (SENTINEL = 2701s =>45 min)
        }
        return hp;
    }
}
