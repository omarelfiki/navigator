package models;

public record StopTime(Stop stop, Trip trip, String departureTime, String arrivalTime, int stopSequence) {
    public StopTime {
        if (stop == null) {
            stop = new Stop();
        }
        if (trip == null) {
            trip = new Trip();
        }
        if (departureTime == null || departureTime.isEmpty()) {
            departureTime = "00:00:00";
        }
        if (arrivalTime == null || arrivalTime.isEmpty()) {
            arrivalTime = "00:00:00";
        }
        if (stopSequence < 0) {
            stopSequence = 0;
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
