package router;

import db.TDSImplement;
import models.Stop;
import models.Trip;

import java.util.Objects;

public class Node {
    Stop stop;
    private final String stopId;
    String arrivalTime;
    double g;
    double h;
    Node parent;
    private final String mode;
    private final Trip trip;

    TDSImplement tds = new TDSImplement();

    public Node(String stopId, String arrivalTime, Node parent, String mode, Trip trip) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.parent = parent;
        this.mode = mode;
        this.trip = Objects.requireNonNullElseGet(trip, Trip::new);
        this.stop = Objects.requireNonNullElseGet(tds.getStop(stopId), Stop::new);
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Stop getStop() {
        return stop;
    }

    public String getStopId() {
        return stopId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public Node getParent() {
        return parent;
    }

    public String getMode() {
        return mode;
    }

    public Trip getTrip() {
        return trip;
    }

    @Override
    public String toString() {
        // -1 means "not set" by default
        int duration = -1;
        return "Node{" +
                "stopId='" + stopId + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", g=" + g +
                ", h=" + h +
                ", mode='" + mode + '\'' +
                ", trip=" + trip +
                ", duration=" + duration +
                '}';
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
