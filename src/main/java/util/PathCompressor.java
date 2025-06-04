package util;

import java.util.*;

public class PathCompressor {

    public static List<Map<String, Object>> compress(List<Map<String, Object>> path) {
        // Apply both walk and ride compression
        return compressRides(compressWalks(path));
    }

    public static List<Map<String, Object>> compressWalks(List<Map<String, Object>> path) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> currentWalk = null;
        int walkDuration = 0;

        for (Map<String, Object> segment : path) {
            String mode = (String) segment.get("mode");

            if ("walk".equals(mode)) {
                if (currentWalk == null) {
                    currentWalk = new HashMap<>(segment);
                    walkDuration = (int) segment.get("duration");
                } else {
                    // Update destination and accumulate duration
                    currentWalk.put("to", segment.get("to"));
                    walkDuration += (int) segment.get("duration");
                }
            } else {
                if (currentWalk != null) {
                    currentWalk.put("duration", walkDuration);
                    result.add(currentWalk);
                    currentWalk = null;
                    walkDuration = 0;
                }
                result.add(segment);
            }
        }

        if (currentWalk != null) {
            currentWalk.put("duration", walkDuration);
            result.add(currentWalk);
        }

        return result;
    }


    public static List<Map<String, Object>> compressRides(List<Map<String, Object>> path) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> currentRide = null;
        int rideDuration = 0;

        for (Map<String, Object> segment : path) {
            String mode = (String) segment.get("mode");

            if ("ride".equals(mode)) {
                if (currentRide == null) {
                    currentRide = new HashMap<>(segment);
                    rideDuration = (int) segment.get("duration");
                } else {
                    Map<String, Object> currentRoute = (Map<String, Object>) currentRide.get("route");
                    Map<String, Object> nextRoute = (Map<String, Object>) segment.get("route");

                    if (routesMatch(currentRoute, nextRoute)) {
                        currentRide.put("to", segment.get("to"));
                        currentRide.put("stop", segment.get("stop"));
                        currentRide.put("startTime", segment.get("startTime"));
                        rideDuration += (int) segment.get("duration");
                    } else {
                        currentRide.put("duration", rideDuration);
                        result.add(currentRide);
                        currentRide = new HashMap<>(segment);
                        rideDuration = (int) segment.get("duration");
                    }
                }
            } else {
                if (currentRide != null) {
                    currentRide.put("duration", rideDuration);
                    result.add(currentRide);
                    currentRide = null;
                    rideDuration = 0;
                }
                result.add(segment);
            }
        }

        if (currentRide != null) {
            currentRide.put("duration", rideDuration);
            result.add(currentRide);
        }

        return result;
    }


    private static boolean routesMatch(Map<String, Object> route1, Map<String, Object> route2) {
        return Objects.equals(route1.get("operator"), route2.get("operator")) &&
                Objects.equals(route1.get("shortName"), route2.get("shortName")) &&
                Objects.equals(route1.get("longName"), route2.get("longName")) &&
                Objects.equals(route1.get("headSign"), route2.get("headSign"));
    }
}
