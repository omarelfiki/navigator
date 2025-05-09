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
    Stop stop;
    String stopId;
    String arrivalTime;
    double g;
    double h;
    Node parent;
    String mode;
    Trip trip;
}
