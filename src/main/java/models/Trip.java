package models;

public class Trip {
    public String tripId;
    public Route route;
    private Service service;
    private String directionalId;
    private String tripShortName;
    public String headSign;
    private boolean wheelChairAccess;

    // optional field
    private String blockId;

    //  Required fields constructor
    public Trip(String tripId, Service service, Route route, String directionalId,
                String tripShortName, String headSign, boolean wheelChairAccess) {
        this.tripId = tripId;
        this.service = service;
        this.route = route;
        this.directionalId = directionalId;
        this.tripShortName = tripShortName;
        this.headSign = headSign;
        this.wheelChairAccess = wheelChairAccess;
    }

    public Trip() {}
    //  Full constructor (required + optional)
    public Trip(String tripId, Service service, Route route, String directionalId,
                String tripShortName, String headSign, boolean wheelChairAccess, String blockId) {
        this.tripId = tripId;
        this.service = service;
        this.route = route;
        this.directionalId = directionalId;
        this.tripShortName = tripShortName;
        this.headSign = headSign;
        this.wheelChairAccess = wheelChairAccess;
        this.blockId = blockId;
    }
    public Trip(String tripId, Route route, String headSign) {
        this.tripId = tripId;
        this.route = route;
        this.headSign = headSign;
    }
    public String getTripId() {
        return tripId;
    }
    public Service getService() {
        return service;
    }

    public Route getRoute() {
        return route;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public String getHeadSign() {
        return headSign;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", service=" + (service != null ? service.getServiceId() : "null") +
                ", route=" + (route != null ? route.getRouteId() : "null") +
                ", directionalId='" + directionalId + '\'' +
                ", tripShortName='" + tripShortName + '\'' +
                ", headSign='" + headSign + '\'' +
                ", wheelChairAccess=" + wheelChairAccess +
                ", blockId='" + blockId + '\'' +
                '}';
    }
}
