package com.navigator14;
import db.*;
import util.AStarRouterV;
import util.Node;

import java.util.List;

public class RoutingTest {
    public static void main(String[] args) {
        AStarRouterV router = new AStarRouterV();
        DBaccess db = DBaccessProvider.getInstance();
        if (db == null) {
            System.err.println("DBaccessProvider returned null.");
            return;
        }

        List<Node> path = router.findFastestPath(41.904,12.5004,41.8791,12.5221,"09:30:00");
        if (path != null) {
            System.out.println("Path found:");
            for (Node node : path) {
                System.out.println(node.stopId+" "  + node.arrivalTime +" "+ node.mode + " TRIP " );
                if(node.trip != null) {
                    System.out.println("Trip ID: " + node.trip.tripId);

                }
            }
        } else {
            System.out.println("No path found.");
        }
    }
}
