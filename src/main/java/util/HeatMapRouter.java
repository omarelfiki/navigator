package util;

import db.NearbyStops;
import db.TDSImplement;
import models.Stop;


import java.util.*;

import static util.TimeUtil.addTime;


public class HeatMapRouter {
    private final EdgeService edgeService = new EdgeService();
    private final Map<String, Double> bestCost = new HashMap<>();
    private final Map<String, Node> settled = new HashMap<>();

    private final boolean debug = false;


    public Map<String, Node> buildWithWalk(double latStart,
                                           double lonStart,
                                           String startTime) {

        //creates the origin node
        Node ORIGIN = new Node("origin", startTime, null, "WALK", null);
        ORIGIN.stop = new Stop("origin", "START", latStart, lonStart);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g));

        for (Stop s : NearbyStops.getNearbyStops(latStart, lonStart, 500)) {
            double walk = WalkingTime.getWalkingTime(latStart, lonStart, s.stopLat, s.stopLon);
            String arr = addTime(startTime, walk);

            Node n = new Node(s.stopId, arr, ORIGIN, "WALK", null);
            n.g = walk;
            pq.add(n);
            bestCost.put(s.stopId, walk);
        }

        int totalStops = new TDSImplement().getAllStops().size();

        //1. graph expansion
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (settled.containsKey(cur.stopId)) continue;
            settled.put(cur.stopId, cur);

            if (debug && settled.size() % 10_000 == 0)
                System.err.printf("Settled %,d stops …%n", settled.size());

            if (settled.size() == totalStops) break;

            for (Edge e : edgeService.getEdges(cur)) {
                String to = e.getToStopId();
                double g = cur.g + e.getWeight();
                if (g >= bestCost.getOrDefault(to, Double.MAX_VALUE) - 1e-6) continue;

                Node nxt = new Node(to, e.getArrivalTime(), cur, e.getMode(), e.getTrip());
                nxt.g = g;
                nxt.h = 0;//no point in creating a new class just for the heat map, if we want to change this it's a quick fix
                pq.add(nxt);
                bestCost.put(to, g);
            }
        }

        return settled;   // key = stopId, value = Node
    }

    public Map<String, Node> buildWithoutWalk(double latStart,
                                              double lonStart,
                                              String startTime) {

        //Create the origin node from where you start :)
        Node ORIGIN = new Node("origin", startTime, null, "WALK", null);
        ORIGIN.stop = new Stop("origin", "START", latStart, lonStart);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.g));

        for (Stop s : NearbyStops.getNearbyStops(latStart, lonStart, 500)) {
            double walk = WalkingTime.getWalkingTime(latStart, lonStart, s.stopLat, s.stopLon);
            String arr = addTime(startTime, walk);

            Node n = new Node(s.stopId, arr, ORIGIN, "WALK", null);
            n.g = walk;
            pq.add(n);
            bestCost.put(s.stopId, walk);
        }

        int totalStops = new TDSImplement().getAllStops().size();

        // 1. graph expansion
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (settled.containsKey(cur.stopId)) continue;
            settled.put(cur.stopId, cur);

            if (debug && settled.size() % 10_000 == 0)
                System.err.printf("Settled %,d stops …%n", settled.size());

            if (settled.size() == totalStops) break;

            for (Edge e : edgeService.getEdgesNoWalk(cur)) {
                String to = e.getToStopId();
                double g = cur.g + e.getWeight();
                if (g >= bestCost.getOrDefault(to, Double.MAX_VALUE) - 1e-6) continue;

                Node nxt = new Node(to, e.getArrivalTime(), cur, e.getMode(), e.getTrip());
                nxt.g = g;
                nxt.h = 0; //no point in creating a new class just for the heat map, if we want to change this it's a quick fix
                pq.add(nxt);
                bestCost.put(to, g);
            }
        }

        return settled;   // key = stopId, value = Node
    }

    public static void main(String[] args) {
        HeatMapRouter fsr = new HeatMapRouter();

        //Visits 8529 out of 8723 stops
        // takes 1min and 37 sec
        Map<String, Node> bestWithWalk = fsr.buildWithWalk(41.904, 12.5004, "09:30:00");
        System.out.printf("Reachable stops with walking: %,d%n", bestWithWalk.size());


        //visits 7518 out of 8723 stops
        //takes 50 sec
        Map<String, Node> bestWithoutWalk = fsr.buildWithoutWalk(41.904, 12.5004, "09:30:00");
        System.out.printf("Reachable stops without walking: %,d%n", bestWithoutWalk.size());
    }
}
