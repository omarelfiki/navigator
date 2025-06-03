package router;

import db.TDSImplement;
import models.Stop;
import models.StopTime;
import models.Trip;

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
        this.departureTime = departureTime; // when I arrive at this stop (and start waiting)
        this.trip = trip;

        TDSImplement tds = new TDSImplement();
        Stop startStop = tds.getStop(fromStopId);
        StopTime currentStopTime = tds.getCurrentStopTime(trip, startStop, departureTime);
        StopTime nextStopTime = tds.getNextStopTime(currentStopTime);

        if (nextStopTime == null) {
            throw new IllegalArgumentException("No next stop time found for trip " + trip.tripId() +
                    " at stop " + fromStopId + " after time " + departureTime);
        }

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
    public String getArrivalTime() {return arrivalTime;}
    @Override
    public String getDepartureTime() {return departureTime;}
    @Override
    public String getFromStopId() {return fromStopId;}

    public String getRideStartTime() {return rideStartTime;}

}
