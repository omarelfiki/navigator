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
    private int duration = -1; // -1 means "not set" by default

    TDSImplement tds = new TDSImplement();

    public Node(String stopId, String arrivalTime, Node parent, String mode, Trip trip) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.parent = parent;
        this.mode = mode;
        this.trip = Objects.requireNonNullElseGet(trip, Trip::new);
        this.stop = Objects.requireNonNullElseGet(tds.getStop(stopId), Stop::new);
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

    // New: duration getter and setter
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
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
