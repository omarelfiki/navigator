package models;

public record Trip(String tripId, Route route, String headSign) {

    public Trip {
        if (route == null) {
            route = new Route("unknown", null, "N/A", "Unknown");
        }
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", route=" + (route != null ? route.routeId() : "N/A") +
                '}';
    }
}
