package util;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

class TransferEdge implements Edge {
    String toStopId;
    String fromStopId;
    String departureTime;
    String arrivalTime;
    double weight;
    String mode;
    Trip trip;
    Stop startStop;
    Stop endStop;
    TDSImplement tds;
    double waitingTime;
    double rideTime;
    StopTime currentStopTime;
    StopTime nextStopTime ;



    public TransferEdge(String fromStopId,String departureTime,Trip trip) {
        //this.toStopId = toStopId;
        this.fromStopId = fromStopId;
        this.startStop = tds.getStop(fromStopId);
       // this.endStop = tds.getStop(toStopId);
        this.departureTime = departureTime;
        this.trip = trip;
        currentStopTime = tds.getStopTime(trip,endStop);
        nextStopTime = tds.getNextStopTime(currentStopTime);
        this.waitingTime = calculateWaitingTime(departureTime,arrivalTime);
        this.arrivalTime =nextStopTime.getArrivalTime();
        this.weight = calculateWeight();
        this.mode = "TRANSFER";
        this.rideTime = calculateRideTime();
    }
    public double calculateWaitingTime(String start, String end) {
        TimeUtil timeUtil = new TimeUtil();
        return timeUtil.calculateDifference(start, end);
    }

    public double calculateRideTime() {
        TimeUtil timeUtil = new TimeUtil();
        return timeUtil.calculateDifference(currentStopTime.getDepartureTime(),nextStopTime.getArrivalTime())-waitingTime;
    }

    public double calculateWeight() {
        return waitingTime + rideTime;
    }

    public String getToStopId() { return toStopId; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getMode() { return mode; }
    public Trip getTrip() { return null; }
    public double getWeight() { return weight; }
}
