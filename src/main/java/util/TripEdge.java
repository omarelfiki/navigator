package util;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

class TripEdge implements Edge {
    String fromStopId;
    String toStopId;
    String departureTime;
    String arrivalTime;
    Trip trip;
    double weight;
    Stop endStop;
    Stop startStop;
    TDSImplement tds = new TDSImplement();
    TimeUtil timeUtil = new TimeUtil();
    String mode;

    public TripEdge(String fromStopId,String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        this.departureTime = departureTime;
        this.trip = trip;
        this.startStop = tds.getStop(fromStopId);
        this.arrivalTime = calculateArrival();
        this.weight = calculateDuration(departureTime,arrivalTime);
        this.mode = "SAME_TRIP";
    }

    public String calculateArrival() {
        StopTime currentStopTime = tds.getStopTime(trip,startStop);
        StopTime nextStopTime = tds.getNextStopTime(currentStopTime);
        this.endStop = nextStopTime.getStop();
        this.toStopId = endStop.getStopId();
        return nextStopTime.getArrivalTime();
    }

    private double calculateDuration(String start, String end) {
        return timeUtil.calculateDifference(start, end);
    }
    public String getToStopId() { return toStopId; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getMode() { return mode; }
    public Trip getTrip() { return trip; }
    public double getWeight() { return weight; }

}
