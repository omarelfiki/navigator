
public class StopDistance {
    private final Stop stop;
    private final double distanceKm;

    public StopDistance(Stop stop, double distanceKm) {
        this.stop = stop;
        this.distanceKm = distanceKm;
    }

    public Stop getStop() {
        return stop;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getWalkingTimeMinutes() {
        return (distanceKm / 5.0) * 60; // 5 km/h walking speed
    }
}
