package router;

import models.Trip;

public interface Edge {
     String getToStopId();
     String getArrivalTime();
     String getMode();
     Trip getTrip();
     double getWeight();
}
