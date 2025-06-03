package router;

import models.Trip;

public interface Edge {
    public String getToStopId();
    public String getDepartureTime();
    public String getArrivalTime();
    public String getMode();
    public Trip getTrip();
    public double getWeight();
}
