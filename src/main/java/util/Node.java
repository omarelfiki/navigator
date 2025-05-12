package util;

import db.TDSImplement;
import models.Stop;
import models.Trip;

public class Node {
    TDSImplement tds = new TDSImplement();
    public Node(String stopId, String arrivalTime, Node parent, String mode, Trip trip) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.parent = parent;
        this.mode = mode;
        this.trip = trip;
        this.stop = tds.getStop(stopId);

    }
    public Stop stop;
    public String stopId;
    public String arrivalTime;
    double g;
    double h;
    public Node parent;
    public String mode;
    public Trip trip;

    @Override
    public String toString() {
        return "Node{" +
                "stopId='" + stopId + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", g=" + g +
                ", h=" + h +
                ", mode='" + mode + '\'' +
                ", trip=" + trip +
                '}';
    }
}
