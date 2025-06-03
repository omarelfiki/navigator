package router;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

import static util.TimeUtil.calculateDifference;

class TripEdge implements Edge {
    private final String fromStopId;
    private final String toStopId;
    private final String departureTime;
    private final String arrivalTime;
    private final Trip trip;
    private final double weight;

    public TripEdge(String fromStopId, String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        this.departureTime = departureTime;
        this.trip = trip;

        TDSImplement tds = new TDSImplement();
        Stop startStop = tds.getStop(fromStopId);
        StopTime currentStopTime = tds.getCurrentStopTime(trip, startStop, departureTime);

        if (currentStopTime == null) {
            throw new IllegalArgumentException("No stop time at stop " + fromStopId + " for trip " + trip.tripId());
        }

        StopTime nextStopTime = tds.getNextStopTime(currentStopTime);

        if (nextStopTime == null) {
            throw new IllegalArgumentException("No next stop after stop " + fromStopId + " on trip " + trip.tripId() + " (possibly the last stop).");
        }

        Stop endStop = nextStopTime.stop();
        this.toStopId = endStop.getStopId();
        this.arrivalTime = nextStopTime.arrivalTime();
        this.weight = calculateDifference(this.departureTime, this.arrivalTime);
    }

    @Override
    public String getToStopId() {
        return toStopId;
    }
    @Override
    public String getMode() {
        // Mode of transport for this edge
        return "SAME_TRIP";
    }
    @Override
    public Trip getTrip() {
        return trip;
    }
    @Override
    public double getWeight() {
        return weight;
    }
    @Override
    public String getArrivalTime() {return arrivalTime;}
    @Override
    public String getDepartureTime() {return departureTime;}
    @Override
    public String getFromStopId() {return fromStopId;}

}
