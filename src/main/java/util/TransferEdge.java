package util;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

class TransferEdge implements Edge {
    String toStopId;
    String fromStopId;
    String departureTime; // Time I arrive at fromStopId
    String arrivalTime;   // Time I arrive at toStopId
    double weight;
    String mode;
    Trip trip;
    Stop startStop;
    Stop endStop;
    double waitingTime;
    double rideTime;
    StopTime currentStopTime;
    StopTime nextStopTime;

    TDSImplement tds = new TDSImplement();
    TimeUtil timeUtil = new TimeUtil();

    public TransferEdge(String fromStopId, String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        this.departureTime = departureTime; // when I arrive at this stop (and start waiting)
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

        // WAIT = time between my arrival at stop and trip's departure
        this.waitingTime = timeUtil.calculateDifference(this.departureTime, currentStopTime.getDepartureTime());

        // RIDE = time from bus departure to bus arrival at next stop
        this.rideTime = timeUtil.calculateDifference(currentStopTime.getDepartureTime(), nextStopTime.getArrivalTime());

        // WEIGHT = total cost
        this.weight = timeUtil.calculateDifference(this.departureTime, this.arrivalTime); // or waitingTime + rideTime

        this.mode = "TRANSFER";
    }

    public String getToStopId() { return toStopId; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getMode() { return mode; }
    public Trip getTrip() { return trip; }
    public double getWeight() { return weight; }
}
