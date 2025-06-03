package models;

public record Trip(String tripId, Route route, String headSign) {

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", route=" + (route != null ? route.routeId() : "null") +
                '}';
    }
}
