package models;

public record Trip(String tripId, Route route, String headSign) {
    public Trip() {
        this("N/A", new Route(), "Unknown");
    }

    public Trip {
        if (tripId == null || tripId.isEmpty()) {
            tripId = "N/A";
        }
        if (route == null) {
            route = new Route();
        }
        if (headSign == null || headSign.isEmpty()) {
            headSign = "Unknown";
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
