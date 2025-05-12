package util;

import map.WayPoint;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class NavUtil {
    static double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}

    public static String parsePoint(String origin, String destination, String time, Date date) {
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
            return "Invalid coordinates";
        }
        Future<List<Node>> future = executor.submit(() -> router.findFastestPath(ocoords[0], ocoords[1], dcoords[0], dcoords[1], finalTime));

        try {
            List<Node> path = future.get(30, SECONDS);
            if (path == null) {
                System.err.println("No path found.");
                return "No path found.";
            } else {
                WayPoint.addWaypoint(path);
                return "Found path.";
            }
        } catch (TimeoutException e) {
            System.err.println("findFastestPath timed out.");
            return "Routing timed out.";
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred.";
        } finally {
            executor.shutdown();
        }
    }

    private static boolean checkBounds(double[] ocoords, double[] dcoords) {
        if (ocoords[0] < romeCoords[0] || ocoords[0] > romeCoords[1] || ocoords[1] < romeCoords[2] || ocoords[1] > romeCoords[3]) {
            System.out.println("origin coordinates out of bounds");
            return false;
        } else if (dcoords[0] < romeCoords[0] || dcoords[0] > romeCoords[1] || dcoords[1] < romeCoords[2] || dcoords[1] > romeCoords[3]) {
            System.out.println("destination coordinates out of bounds");
            return false;
        }
        return true;
    }

    public static String parseTime(String time) {
        String[] parts = time.split(":");
        if (parts.length > 3) {
            System.out.println("Invalid time format");
            return null;
        }
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = (parts.length == 3) ? Integer.parseInt(parts[2]) : 0;

        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
            return null;
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    private static double[] parseCoords(String coords) {
        String[] parts = coords.split(",");
        if (parts.length != 2) {
            System.out.println("Invalid coordinates format");
            return null;
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lng};
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates format");
            return null;
        }
    }
}
