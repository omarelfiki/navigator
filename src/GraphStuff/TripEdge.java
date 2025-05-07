public class TripEdge extends Edge {
    private final Trip trip;

    public TripEdge(String toStopId, String departureTime, String arrivalTime, Trip trip) {
        super(toStopId, departureTime, arrivalTime, "ride");
        this.trip = trip;
    }

    @Override
    public Trip getTripInfo() {
        return trip;
    }
    public Double getDistanceKm() {
        return null;
    }

}

