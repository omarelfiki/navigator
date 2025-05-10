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

        List<Node> path = router.findFastestPath(41.8298,12.5563,41.8258,12.5623,"08:00:00");
        if (path != null) {
            System.out.println("Path found:");
            for (Node node : path) {
                System.out.println(node.stopId + " " + node.arrivalTime);
            }
        } else {
            System.out.println("No path found.");
        }
    }
}
