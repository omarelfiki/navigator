package util;

import router.WalkingTime;

import java.util.*;

public class PathCompressor {

    public static List<Map<String, Object>> compress(List<Map<String, Object>> path) {
        // Apply both walk and ride compression
        return compressRides(compressWalks(path));
    }

    public static List<Map<String, Object>> compressWalks(List<Map<String, Object>> path) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> currentWalk = null;

        double startLat = 0, startLon = 0;
        double endLat = 0, endLon = 0;

        for (Map<String, Object> segment : path) {
            String mode = (String) segment.get("mode");

            if ("walk".equals(mode)) {
                if (currentWalk == null) {
                    currentWalk = new HashMap<>(segment);
                    Map<String, Object> to = (Map<String, Object>) segment.get("to");
                    startLat = (double) currentWalk.getOrDefault("fromLat", to.get("lat")); // fallback
                    startLon = (double) currentWalk.getOrDefault("fromLon", to.get("lon")); // fallback
                }

                Map<String, Object> to = (Map<String, Object>) segment.get("to");
                endLat = (double) to.get("lat");
                endLon = (double) to.get("lon");

                // Always update destination
                currentWalk.put("to", to);
            } else {
                if (currentWalk != null) {
                    // Compute actual walking duration based on distance
                    double walkingTime = WalkingTime.getWalkingTime(startLat, startLon, endLat, endLon);
                    int duration = Math.max(1, (int) Math.round(walkingTime / 60.0));
                    currentWalk.put("duration", duration);
                    result.add(currentWalk);
                    currentWalk = null;
                }
                result.add(segment);
            }
        }

        if (currentWalk != null) {
            double walkingTime = WalkingTime.getWalkingTime(startLat, startLon, endLat, endLon);
            int duration = Math.max(1, (int) Math.round(walkingTime / 60.0));
            currentWalk.put("duration", duration);
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
