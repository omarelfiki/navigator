package router;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import static util.TimeUtil.addTime;
import static router.WalkingTime.getWalkingTime;

public class WalkingEdge implements Edge {
    String toStopId;
    String fromStopId;
    String departureTime;
    String arrivalTime;
    String mode;
    Trip trip;
    double weight;
    double walkTime;
    Stop endStop;
    Stop startStop;
    TDSImplement tds = new TDSImplement();

    public WalkingEdge(String fromStopId, String toStopId, String departureTime) {
        this.toStopId = toStopId;
        this.fromStopId = fromStopId;
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
        return getWalkingTime(startStop.stopLat, startStop.stopLon, endStop.stopLat, endStop.stopLon);
    }

    public String getToStopId() {
        return toStopId;
    }
    public String getMode() {
        return mode;
    }

    public Trip getTrip() {
        return trip;
    }

    public double getWeight() {
        return weight;
    }
    public String getArrivalTime() { return arrivalTime; }

}
