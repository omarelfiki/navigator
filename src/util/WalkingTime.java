

public class WalkingTime {

    // Returns the walking time in minutes.
    public static double getWalkingTime(double latFrom, double longFrom, double latTo, double longTo) {

        double distance = HaversineUtil.calculateDistance(latFrom, longFrom, latTo, longTo);
        return (distance / 5.0) * 60.0;
    }


}
