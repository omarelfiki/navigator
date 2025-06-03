package models;

public record StopTime(Stop stop, Trip trip, String departureTime, String arrivalTime, int stopSequence) {


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
