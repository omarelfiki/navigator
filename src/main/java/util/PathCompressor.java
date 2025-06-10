package util;

import models.Stop;
import router.Node;
import router.WalkingTime;

import java.util.ArrayList;
import java.util.List;

public class PathCompressor {
    public static List<Node> compressWalks(List<Node> path) {
        List<Node> result = new ArrayList<>();

        Node previousNonWalk = path.getFirst();
        Node walkStartNode = null;
        Node walkEndNode;

        result.add(path.getFirst()); // always include start node

        for (int i = 1; i < path.size(); i++) {
            Node current = path.get(i);
            boolean isWalk = "WALK".equals(current.getMode());
            boolean isLast = (i == path.size() - 1);
            boolean nextIsNotWalk = isLast || !"WALK".equals(path.get(i + 1).getMode());

            if (isWalk) {
                if (walkStartNode == null) {
                    walkStartNode = current;
                }
                walkEndNode = current;

                // At end of walk chain
                if (nextIsNotWalk) {
                    // Set parent
                    walkEndNode.setParent(previousNonWalk);

                    Node parent = walkEndNode.getParent();
                    if (parent != null) {
                        Stop parentStop = parent.getStop();
                        Stop walkStop = walkEndNode.getStop();

                        // Compute walking time from parent to end of walk chain
                        double walkTime = WalkingTime.getWalkingTime(
                                parentStop.getStopLat(), parentStop.getStopLon(),
                                walkStop.getStopLat(), walkStop.getStopLon()
                        );

                        // Update arrivalTime
                        String arrivalTime = TimeUtil.addTime(parent.getArrivalTime(), walkTime);
                        walkEndNode.setArrivalTime(arrivalTime);
                    }

                    result.add(walkEndNode);
                    walkStartNode = null;
                }

            } else {
                result.add(current);
                previousNonWalk = current;
            }
        }

        return result;
    }
}
