package util;

import map.WayPoint;
import router.AStarRouterV;
import router.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static util.DebugUtil.getDebugMode;
import static util.GeoUtil.parseCoords;
import static util.TimeUtil.parseTime;

public class NavUtil {
    static double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}

    public static List<Node> parsePoint(String origin, String destination, String time) {
        boolean isDebugMode = getDebugMode();
        boolean isOnline = NetworkUtil.isNetworkAvailable();
        double[] oCoords;
        double[] dCoords;
        if (isOnline) {
            oCoords = GeoUtil.getCoordinatesFromAddress(origin);
            dCoords = GeoUtil.getCoordinatesFromAddress(destination);
        } else {
            oCoords = parseCoords(origin);
            dCoords = parseCoords(destination);
        }
        time = parseTime(time);
        AStarRouterV router = new AStarRouterV();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String finalTime = time;
        if (oCoords == null || dCoords == null) {
            if (isDebugMode) System.err.println("Invalid coordinates");
            return null;
        }

        //to be changed when needed
        List<String> avoidedStops = new ArrayList<>();
        Future<List<Node>> future = executor.submit(() -> router.findFastestPath(oCoords[0], oCoords[1], dCoords[0], dCoords[1], finalTime,avoidedStops));

        try {
            List<Node> path = future.get(30, SECONDS);
            if (path == null) {
                if (isDebugMode) System.err.println("No path found.");
                return null;
            } else {
                WayPoint.addWaypoint(path);
                return path;
            }
        } catch (TimeoutException e) {
            if (isDebugMode) System.err.println("findFastestPath timed out.");
            return null;
        } catch (Exception e) {
            if (isDebugMode) System.err.println("Error in findFastestPath: " + e);
            return null;
        } finally {
            executor.shutdown();
        }
    }

    @SuppressWarnings("unused")
    private static boolean checkBounds(double[] oCoords, double[] dCoords) {
        boolean isDebugMode = getDebugMode();
        if (oCoords[0] < romeCoords[0] || oCoords[0] > romeCoords[1] || oCoords[1] < romeCoords[2] || oCoords[1] > romeCoords[3]) {
            if (isDebugMode) System.out.println("origin coordinates out of bounds");
            return false;
        } else if (dCoords[0] < romeCoords[0] || dCoords[0] > romeCoords[1] || dCoords[1] < romeCoords[2] || dCoords[1] > romeCoords[3]) {
            if (isDebugMode) System.out.println("destination coordinates out of bounds");
            return false;
        }
        return true;
    }

}
