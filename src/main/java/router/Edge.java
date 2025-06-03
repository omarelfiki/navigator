package router;

import models.Trip;

public interface Edge {
    String getToStopId();
    String getMode();
    Trip getTrip();
    double getWeight();
    String getArrivalTime();
    String getDepartureTime();
    String getFromStopId();
}
