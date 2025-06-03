package router;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import static util.TimeUtil.addTime;
import static router.WalkingTime.getWalkingTime;

public class WalkingEdge implements Edge {
    private final String toStopId;
    private final String fromStopId;
    private final String departureTime;
    private final String arrivalTime;
    private final String mode;
    private final Trip trip;
    private final double weight;
    private final double walkTime;
    private final Stop endStop;
    private final Stop startStop;

    public WalkingEdge(String fromStopId, String toStopId, String departureTime) {
        this.toStopId = toStopId;
        this.fromStopId = fromStopId;
        TDSImplement tds = new TDSImplement();
        this.startStop = tds.getStop(fromStopId);
        this.endStop = tds.getStop(toStopId);
        this.departureTime = departureTime;
        this.walkTime = computeWalkingTime();
        this.arrivalTime = computeArrivalTime();
        this.mode = "WALK";
        this.trip = null;
        this.weight = walkTime * 1.8;
    }


    public String computeArrivalTime() {
        return addTime(departureTime, walkTime);
    }

    public double computeWalkingTime() {
        return getWalkingTime(startStop.getStopLat(), startStop.getStopLon(), endStop.getStopLat(), endStop.getStopLon());
    }

    @Override
    public String getToStopId() {
        return toStopId;
    }
    @Override
    public String getMode() {
        return mode;
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
