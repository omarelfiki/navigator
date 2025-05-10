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
    TDSImplement tds = new TDSImplement();
    double waitingTime;
    double rideTime;
    StopTime currentStopTime;
    StopTime nextStopTime ;



    public TransferEdge(String fromStopId, String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        this.departureTime = departureTime;
        this.trip = trip;
        this.startStop = tds.getStop(fromStopId);
        this.currentStopTime = tds.getStopTime(trip, startStop);
        this.nextStopTime = tds.getNextStopTime(currentStopTime);

        if (nextStopTime == null) {
            throw new IllegalArgumentException("No next stop time found for transfer.");
        }

        this.toStopId = nextStopTime.getStop().getStopId();
        this.endStop = nextStopTime.getStop();
        this.arrivalTime = nextStopTime.getArrivalTime();

        this.waitingTime = calculateWaitingTime(departureTime, currentStopTime.getDepartureTime());
        this.rideTime = calculateRideTime();
        this.weight = calculateWeight();
        this.mode = "TRANSFER";
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
