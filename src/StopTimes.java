public class StopTimes {
    private Stop stop;
    private String tripID;
    private String departureTime;     // Format: "HH:mm:ss"
    private String arrivalTime;       // Format: "HH:mm:ss"
    private int stopSequence;
    private double shapeDistTravelled;
    private int dropOffType;
    private int pickupType;
    private String stopHeadsign;

    public StopTimes(Stop stop, String tripID , String departureTime, String arrivalTime, int  stopSequence, double shapeDisTravelled, int dropOffType, int pickupType, String stopHeadsign  ) {
    this.stop = stop;
    this.tripID = tripID;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.stopSequence = stopSequence;
    this.shapeDistTravelled = shapeDisTravelled;
    this.dropOffType = dropOffType;
    this.pickupType = pickupType;
    this.stopHeadsign = stopHeadsign;
    }
    public StopTimes(int dropOffType , int pickupType , String stopHeadsign) {
    this.dropOffType = dropOffType;
    this.pickupType = pickupType;
    this.stopHeadsign = stopHeadsign;
    }

    public Stop getStop() {
        return stop;
    }
    public String getTripID() {
        return tripID;
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
    public String getStopHeadsign() {
        return stopHeadsign;
    }
    @Override
    public String toString() {
        return "stopID=" + (stop != null ? stop.getStopID() : "null") +
                ", tripID='" + tripID + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", stopSequence=" + stopSequence +
                ", shapeDistTravelled=" + shapeDistTravelled +
                ", dropOffType=" + dropOffType +
                ", pickupType=" + pickupType +
                ", stopHeadsign='" + stopHeadsign + '\'' +
                '}';
    }

}
