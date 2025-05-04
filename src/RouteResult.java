import models.*;
public class RouteResult {
    private final StopDistance from;
    private final StopDistance to;
    private final Trip trip;
    private final String departureTime;
    private final String arrivalTime;
    private final int walkingBeforeMinutes;
    private final int walkingAfterMinutes;
    private final int inTransitMinutes;
    private final int totalTimeMinutes;

    public RouteResult(StopDistance from, StopDistance to, Trip trip,
                       String departureTime, String arrivalTime,
                       int walkingBeforeMinutes, int walkingAfterMinutes,
                       int inTransitMinutes) {
        this.from = from;
        this.to = to;
        this.trip = trip;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.walkingBeforeMinutes = walkingBeforeMinutes;
        this.walkingAfterMinutes = walkingAfterMinutes;
        this.inTransitMinutes = inTransitMinutes;
        this.totalTimeMinutes = walkingBeforeMinutes + inTransitMinutes + walkingAfterMinutes;
    }

    @Override
    public String toString() {
        return String.format("""
            📍 From Stop: %s (%.2f km, %.1f min walk)
            🛑 To Stop: %s (%.2f km, %.1f min walk)
            🚍 Trip ID: %s | Route: %s
            🕒 Departure: %s | Arrival: %s
            ⏱️ Transit: %d min | Total: %d min
        """,
                from.getStop().getStopName(), from.getDistanceKm(), walkingBeforeMinutes * 1.0,
                to.getStop().getStopName(), to.getDistanceKm(), walkingAfterMinutes * 1.0,
                trip.getTripId(), trip.getRoute().getRouteShortName(),
                departureTime, arrivalTime,
                inTransitMinutes, totalTimeMinutes
        );
    }
}
