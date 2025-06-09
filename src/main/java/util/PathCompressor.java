package util;

import models.Stop;
import router.Node;
import router.WalkingTime;

import java.util.ArrayList;
import java.util.List;

public class PathCompressor {

    public static List<Node> compressWalks(List<Node> path) {
        List<Node> result = new ArrayList<>();

        Node previousNonWalk = path.get(0);
        Node walkStartNode = null;
        Node walkEndNode = null;

        result.add(path.get(0)); // always include the start node

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

                // If it's the end of the walk chain, add a compressed walk node
                if (nextIsNotWalk) {
                    walkEndNode.setParent(previousNonWalk);

                    Node parent = walkEndNode.getParent();
                    if (parent != null) {
                        Stop parentStop = parent.getStop();
                        double walkTime = WalkingTime.getWalkingTime(
                                parentStop.getStopLat(), parentStop.getStopLon(),
                                walkEndNode.getStop().getStopLat(), walkEndNode.getStop().getStopLon()
                        );
                        String arrivalTime = TimeUtil.addTime(parent.getArrivalTime(), walkTime);
                        walkEndNode.setArrivalTime(arrivalTime);
                    }

                    result.add(walkEndNode);
                    walkStartNode = null;
                    walkEndNode = null;
                }
            } else {
                result.add(current);
                previousNonWalk = current;
            }
        }

        return result;
    }


}
