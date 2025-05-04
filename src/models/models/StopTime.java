package models;

public class StopTime{
    private Stop stop;
    private Trip trip;
    private String departureTime;
    private String arrivalTime;
    private int stopSequence;

    // Optional fields
    private double shapeDistTravelled;
    private int dropOffType;
    private int pickupType;
    private String stopHeadSign;

    // Constructor with required fields
    public StopTime(Stop stop, Trip trip, String departureTime, String arrivalTime, int stopSequence) {
        this.stop = stop;
        this.trip = trip;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.stopSequence = stopSequence;
    }

    // Constructor with required + optional fields
    public StopTime(Stop stop, Trip trip, String departureTime, String arrivalTime, int stopSequence,
                    double shapeDistTravelled, int dropOffType, int pickupType, String stopHeadSign) {
        this.stop = stop;
        this.trip = trip;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.stopSequence = stopSequence;
        this.shapeDistTravelled = shapeDistTravelled;
        this.dropOffType = dropOffType;
        this.pickupType = pickupType;
        this.stopHeadSign = stopHeadSign;
    }

    public Stop getStop() {
        return stop;
    }

    public Trip getTrip() {
        return trip;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public double getShapeDistTravelled() {
        return shapeDistTravelled;
    }

    public int getDropOffType() {
        return dropOffType;
    }

    public int getPickupType() {
        return pickupType;
    }

    public String getStopHeadSign() {
        return stopHeadSign;
    }

    @Override
    public String toString() {
        return "StopTime{" +
                "stopId='" + (stop != null ? stop.getStopId() : "null") + '\'' +
                ", tripId='" + (trip != null ? trip.getTripId() : "null") + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", stopSequence=" + stopSequence +
                ", shapeDistTravelled=" + shapeDistTravelled +
                ", dropOffType=" + dropOffType +
                ", pickupType=" + pickupType +
                ", stopHeadSign='" + stopHeadSign + '\'' +
                '}';
    }
}
