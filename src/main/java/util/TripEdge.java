package util;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

import static util.TimeUtil.calculateDifference;

class TripEdge implements Edge {
    String fromStopId;
    String toStopId;
    String departureTime;
    String arrivalTime;
    Trip trip;
    double weight;
    Stop startStop;
    Stop endStop;
    String mode;
    StopTime nextStopTime;
    StopTime currentStopTime;
    TDSImplement tds = new TDSImplement();

    public TripEdge(String fromStopId, String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        this.departureTime = departureTime;
        this.trip = trip;
        this.mode = "SAME_TRIP";

        this.startStop = tds.getStop(fromStopId);
        this.currentStopTime = tds.getCurrentStopTime(trip, startStop, departureTime);

        if (currentStopTime == null) {
            throw new IllegalArgumentException("No stop time at stop " + fromStopId + " for trip " + trip.tripId);
        }

        this.nextStopTime = tds.getNextStopTime(currentStopTime);

        if (nextStopTime == null) {
            throw new IllegalArgumentException("No next stop after stop " + fromStopId + " on trip " + trip.tripId + " (possibly the last stop).");
        }

        this.endStop = nextStopTime.getStop();
        this.toStopId = endStop.getStopId();
        this.arrivalTime = nextStopTime.getArrivalTime();
        this.weight = calculateDifference(this.departureTime, this.arrivalTime) ;
    }


    public String getToStopId() { return toStopId; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getMode() { return mode; }
    public Trip getTrip() {
        if (trip == null) {
        throw new IllegalStateException("Trip is not initialized.");
        }
        return trip;
    }
    public double getWeight() { return weight; }
}
