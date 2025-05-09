public class WalkingEdge extends Edge {
    private final double distanceKm;

    public WalkingEdge(String toStopId, String departureTime, String arrivalTime, double distanceKm) {
        super(toStopId, departureTime, arrivalTime, "walk-transfer",null);
        this.distanceKm = distanceKm;
    }

    @Override
    public Trip getTripInfo() {
        return null;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }
}
