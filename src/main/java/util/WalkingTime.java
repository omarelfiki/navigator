package util;

import util.HaversineUtil;

import java.sql.SQLOutput;

public class WalkingTime {

    // Returns the walking time in minutes.
    public static double getWalkingTime(double latFrom, double longFrom, double latTo, double longTo) {
        double distance = HaversineUtil.calculateDistance(latFrom, longFrom, latTo, longTo);
        System.out.println("in getWalkingTime: ");
       return distance / 1.39;
    }

    public static boolean isWalkable(double latFrom, double longFrom, double latTo, double longTo) {
        double distance = HaversineUtil.calculateDistance(latFrom, longFrom, latTo, longTo);
        return distance <= 800.0;
    }

}
