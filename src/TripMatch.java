import models.*;
import util.TimeUtil;
public record TripMatch(Trip trip, StopTime departure, StopTime arrival) {

    public int getInTransitMinutes() {
        return TimeUtil.timeDiffMinutes(departure.getDepartureTime(), arrival.getArrivalTime());
    }
}
