package router;

import db.NearbyStops;
import models.HeatPoint;
import models.Stop;


import java.util.*;

import static util.TimeUtil.addTime;


public class HeatMapRouter {
    private final EdgeService edgeService = new EdgeService();
    private final Map<String, Double> bestCost = new HashMap<>();
    private final Map<String, Node>    settled  = new HashMap<>();

    private final boolean debug = false;
    final double LIMIT    = 2700;   // 45-minute ceiling in seconds
    final double SENTINEL = 2701;   // marks “beyond limit”

    public Map<String, Node> buildWithWalk(double latStart, double lonStart, String startTime) {
        Node ORIGIN = new Node("origin", startTime, null, "WALK", null);
        ORIGIN.stop = new Stop("origin", "START", latStart, lonStart);
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g));

        // bootstrap – first walking leg (cap immediately)
        for (Stop s : NearbyStops.getNearbyStops(latStart, lonStart, 500)) {
            double walk = WalkingTime.getWalkingTime(latStart, lonStart,
                    s.stopLat, s.stopLon);
            Node n = new Node(s.stopId, addTime(startTime, walk), ORIGIN, "WALK", null);
            n.g = Math.min(walk, LIMIT);
            pq.add(n);
            bestCost.put(s.stopId, n.g);
        }

        //int totalStops = new TDSImplement().getAllStops().size();
        int totalStops = 5000;   // test cap – remove or adjust as needed

        // main loop
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (settled.containsKey(cur.stopId)) continue;
            settled.put(cur.stopId, cur);

            if (debug && settled.size() % 10_000 == 0)
                System.err.printf("Settled %,d stops …%n", settled.size());

            if (settled.size() == totalStops) break;
            if (cur.g >= LIMIT) continue;           // do NOT expand over-limit nodes

            for (Edge e : edgeService.getEdges(cur)) {
                double tentative = cur.g + e.getWeight();
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


    public Map<String, Node> buildWithoutWalk(double latStart, double lonStart, String startTime) {
        // This method is similar to buildWithWalk, but does not include walking edges.
        Node ORIGIN = new Node("origin", startTime, null, "WALK", null);
        ORIGIN.stop = new Stop("origin", "START", latStart, lonStart);

        PriorityQueue<Node> pq =
                new PriorityQueue<>(Comparator.comparingDouble(n -> n.g));

        /* bootstrap — first walking leg */
        for (Stop s : NearbyStops.getNearbyStops(latStart, lonStart, 500)) {
            double walk = WalkingTime.getWalkingTime(latStart, lonStart,
                    s.stopLat, s.stopLon);
            Node n = new Node(s.stopId, addTime(startTime, walk), ORIGIN, "WALK", null);
            n.g = Math.min(walk, LIMIT);          // cap even the first hop
            pq.add(n);
            bestCost.put(s.stopId, n.g);
        }

        //int totalStops = new TDSImplement().getAllStops().size();
        int totalStops = 5000; // testing cap — remove or replace later

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (settled.containsKey(cur.stopId)) continue;
            settled.put(cur.stopId, cur);

            if (debug && settled.size() % 10_000 == 0)
                System.err.printf("Settled %,d stops …%n", settled.size());

            if (settled.size() == totalStops) break;
            if (cur.g >= LIMIT) continue;          // do **not** expand beyond limit

            for (Edge e : edgeService.getEdgesNoWalk(cur)) {
                double tentative = cur.g + e.getWeight();
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
        for (Node n : nodes.values()) {                                           //just as a reminder , also hello little easter egg:)
            hp.add(new HeatPoint(n.stop.getStopLat(), n.stop.getStopLon(), n.g/60)); // g is seconds (SENTINEL = 2701s =>45 min)
        }
        return hp;
    }

    public static void main(String[] args) {
        HeatMapRouter fsr = new HeatMapRouter();
        //WITH WALKING
        //Without setting a limit on how big g can get
        //Visits 8529 out of 8723 stops
        // takes 1min and 37 sec
        //Visits 4000 out of 8723 stops
        // takes 58 sec

        //With setting a limit on how big g can get(2700 seconds)
        //Visits 4501 out of 8723 stops
        // takes 53 sec
        Map<String,Node> bestWithWalk = fsr.buildWithWalk(41.904, 12.5004, "09:30:00");
        System.err.printf("Reachable stops with walking: %,d%n", bestWithWalk.size());


        //WITOUT WALKING
        //Without setting a limit on how big g can get
        //visits 7518 out of 8723 stops
        //takes 50 sec
        //Visits 4000 out of 8723 stops
        // takes 32 sec
        //Visits 6000 out of 8723 stops
        // takes 43 sec

        //With setting a limit on how big g can get(2700 seconds)
        //Visits 1852 out of 8723 stops
        // takes 15 sec
        Map<String,Node> bestWithoutWalk = fsr.buildWithoutWalk(41.904, 12.5004, "09:30:00");
        System.err.printf("Reachable stops without walking: %,d%n", bestWithoutWalk.size());
    }
}
