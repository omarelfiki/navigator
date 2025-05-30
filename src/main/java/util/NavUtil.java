package util;

import map.WayPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static util.DebugUtli.getDebugMode;
import static util.GeoUtil.parseCoords;
import static util.TimeUtil.parseTime;

public class NavUtil {
    static double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}

    public static List<Node> parsePoint(String origin, String destination, String time, Date date) {
        boolean isDebugMode = getDebugMode();
        boolean isOnline = NetworkUtil.isNetworkAvailable();
        double[] ocoords;
        double[] dcoords;
        if (isOnline) {
            ocoords = GeoUtil.getCoordinatesFromAddress(origin);
            dcoords = GeoUtil.getCoordinatesFromAddress(destination);
        } else {
            ocoords = parseCoords(origin);
            dcoords = parseCoords(destination);
        }
        time = parseTime(time);
        AStarRouterV router = new AStarRouterV();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String finalTime = time;
        if (ocoords == null || dcoords == null) {
            if (isDebugMode) System.err.println("Invalid coordinates");
            return null;
        }

        //to be changed when needed
        List<String> avoidedStops = new ArrayList<String>();
        Future<List<Node>> future = executor.submit(() -> router.findFastestPath(ocoords[0], ocoords[1], dcoords[0], dcoords[1], finalTime,avoidedStops));

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

    private static boolean checkBounds(double[] ocoords, double[] dcoords) {
        boolean isDebugMode = getDebugMode();
        if (ocoords[0] < romeCoords[0] || ocoords[0] > romeCoords[1] || ocoords[1] < romeCoords[2] || ocoords[1] > romeCoords[3]) {
            if (isDebugMode) System.out.println("origin coordinates out of bounds");
            return false;
        } else if (dcoords[0] < romeCoords[0] || dcoords[0] > romeCoords[1] || dcoords[1] < romeCoords[2] || dcoords[1] > romeCoords[3]) {
            if (isDebugMode) System.out.println("destination coordinates out of bounds");
            return false;
        }
        return true;
    }

}
