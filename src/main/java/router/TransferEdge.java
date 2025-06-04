package router;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

import java.util.List;
import java.util.Objects;

import static util.TimeUtil.calculateDifference;

class TransferEdge implements Edge {
    private final String toStopId;
    private final String fromStopId;
    private final String departureTime; // Time I arrive at fromStopId
    private final String arrivalTime;   // Time I arrive at toStopId
    private final double weight;
    private final Trip trip;
    private final String rideStartTime;

    public TransferEdge(String fromStopId, String departureTime, Trip trip) {
        this.fromStopId = fromStopId;
        if (departureTime == null || departureTime.isEmpty()) {
            this.departureTime = "00:00:00";
        } else {
            this.departureTime = departureTime; // when I arrive at this stop (and start waiting)
        }
        this.trip = Objects.requireNonNullElseGet(trip, Trip::new); // Fallback if trip is not provided

        List<StopTime> stopTimes = getNextStopTime(fromStopId, departureTime, trip);
        StopTime currentStopTime = stopTimes.get(0);
        StopTime nextStopTime = stopTimes.get(1);

        this.toStopId = nextStopTime.stop().getStopId();
        this.arrivalTime = nextStopTime.arrivalTime();

        // WAIT = time between my arrival at stop and trip's departure
        double waitingTime = calculateDifference(this.departureTime, currentStopTime.departureTime());
        this.rideStartTime = currentStopTime.departureTime();
        // RIDE = time from bus departure to bus arrival at next stop
        double rideTime = calculateDifference(currentStopTime.departureTime(), nextStopTime.arrivalTime());

        // WEIGHT = total cost
        //this.weight = timeUtil.calculateDifference(this.departureTime, this.arrivalTime); // or waitingTime + rideTime
        this.weight = 0.6 * waitingTime + rideTime;  // encourage transfers
    }

    private static List<StopTime> getNextStopTime(String fromStopId, String departureTime, Trip trip) {
        TDSImplement tds = new TDSImplement();
        Stop startStop = Objects.requireNonNullElseGet(tds.getStop(fromStopId), Stop::new); // Fallback if stop is not found
        StopTime currentStopTime = tds.getCurrentStopTime(trip, startStop, departureTime);

        if (currentStopTime == null) {
            throw new IllegalArgumentException("No stop time at stop " + fromStopId + " for trip " + trip.tripId());
        }

        StopTime nextStopTime = tds.getNextStopTime(currentStopTime);

        if (nextStopTime == null) {
            throw new IllegalArgumentException("No next stop after stop " + fromStopId + " on trip " + trip.tripId() + " (possibly the last stop).");
        }
        return List.of(currentStopTime, nextStopTime);
    }

    @Override
    public String getToStopId() {
        return toStopId;
    }

    @Override
    public String getMode() {
        // Mode of transport for this edge
        return "TRANSFER";
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
    public String getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public String getDepartureTime() {
        return departureTime;
    }

    @Override
    public String getFromStopId() {
        return fromStopId;
    }

    public String getRideStartTime() {
        return rideStartTime;
    }

}
