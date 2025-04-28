public class TripMatch {
    private final Trip trip;
    private final StopTime departure;
    private final StopTime arrival;

    public TripMatch(Trip trip, StopTime departure, StopTime arrival) {
        this.trip = trip;
        this.departure = departure;
        this.arrival = arrival;
    }

    public Trip getTrip() {
        return trip;
    }

    public StopTime getDeparture() {
        return departure;
    }

    public StopTime getArrival() {
        return arrival;
    }

    public int getInTransitMinutes() {
        return TimeUtil.timeDiffMinutes(departure.getDepartureTime(), arrival.getArrivalTime());
    }
}
