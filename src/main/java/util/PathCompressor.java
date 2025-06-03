package util;
import router.Node;

import java.util.ArrayList;
import java.util.List;

public class PathCompressor {
    public static List<Node> compressPath(List<Node> path) {
        List<Node> compressedPath = new ArrayList<>();
        if (path == null || path.isEmpty()) return compressedPath;

        Node segmentStart = path.get(0);
        Node segmentEnd = segmentStart;

        for (int i = 1; i < path.size(); i++) {
            Node currentNode = path.get(i);

            boolean sameTrip = segmentEnd.trip == null
                    ? currentNode.trip == null
                    : segmentEnd.trip.equals(currentNode.trip);

            if (sameTrip) {
                segmentEnd = currentNode;
            } else {
                compressedPath.add(segmentStart);
                if (!segmentStart.equals(segmentEnd)) {
                    compressedPath.add(segmentEnd);
                }
                segmentStart = currentNode;
                segmentEnd = currentNode;
            }
        }

        compressedPath.add(segmentStart);
        if (!segmentStart.equals(segmentEnd)) {
            compressedPath.add(segmentEnd);
        }

        return compressedPath;
    }

}
