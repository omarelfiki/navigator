package models;

public record StopTime(Stop stop, Trip trip, String departureTime, String arrivalTime, int stopSequence) {
    public StopTime {
        if (stop == null) {
            stop = new Stop("unknown", "Unknown Stop", 0.0, 0.0);
        }
        if (trip == null) {
            trip = new Trip("unknown", new Route("unknown", null, "N/A", "Unknown"), "Unknown Trip");
        }
    }

    @Override
    public String toString() {
        return "StopTime{" +
                "stopId='" + (stop != null ? stop.getStopId() : "null") + '\'' +
                ", tripId='" + (trip != null ? trip.tripId() : "null") + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", stopSequence=" + stopSequence +
                '}';
    }
}
