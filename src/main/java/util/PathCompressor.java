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
        double startLat = 0, startLon = 0;
        result.add(path.get(0));
        for (int i = 0; i < path.size(); i++) {
            Node current = path.get(i);
            String mode = current.getMode();
            boolean isWalk = "WALK".equals(mode);
            boolean isLastSegment = (i == path.size() - 1);

            if (isWalk) {
                if (walkStartNode == null) {
                    walkStartNode = current;
                    startLat = current.getStop().getStopLat();
                    startLon = current.getStop().getStopLon();
                }

                // If it's the last walk in the chain (last node or next is not WALK), emit final walk node
                boolean nextIsNotWalk = isLastSegment || !"WALK".equals(path.get(i + 1).getMode());
                if (nextIsNotWalk) {
                    current.setParent(previousNonWalk);

                    Node nodeParent = current.getParent();
                    if(nodeParent != null) {
                    Stop parentStop = nodeParent.getStop();
                    double walkTime = WalkingTime.getWalkingTime(
                            parentStop.getStopLat(),parentStop.getStopLon(),
                            current.getStop().getStopLat(), current.getStop().getStopLon()
                    );
                    String realArrivalTime = TimeUtil.addTime(nodeParent.getArrivalTime(),walkTime);
                    current.setArrivalTime(realArrivalTime);
                    }
                    result.add(current);
                    walkStartNode = null;
                }
            } else {
                result.add(current);
                previousNonWalk = current; // update parent for next walk chain
            }
        }

        return result;
    }

}
