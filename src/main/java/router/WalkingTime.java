package router;


import util.GeoUtil;

public class WalkingTime {

    // Returns the walking time in minutes.
    public static double getWalkingTime(double latFrom, double longFrom, double latTo, double longTo) {
        double distance = GeoUtil.distance(latFrom, longFrom, latTo, longTo);
       return distance / 1.39;
    }

}
