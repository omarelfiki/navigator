package models;

public class Trip {
    private String tripId;
    private Route route;
    private Service service;
    private Shape shape;
    private String directionalId;
    private String tripShortName;
    private String headSign;
    private boolean wheelChairAccess;

    // optional field
    private String blockId;

    //  Required fields constructor
    public Trip(String tripId, Service service, Shape shape, Route route, String directionalId,
                String tripShortName, String headSign, boolean wheelChairAccess) {
        this.tripId = tripId;
        this.service = service;
        this.shape = shape;
        this.route = route;
        this.directionalId = directionalId;
        this.tripShortName = tripShortName;
        this.headSign = headSign;
        this.wheelChairAccess = wheelChairAccess;
    }

    //  Full constructor (required + optional)
    public Trip(String tripId, Service service, Shape shape, Route route, String directionalId,
                String tripShortName, String headSign, boolean wheelChairAccess, String blockId) {
        this.tripId = tripId;
        this.service = service;
        this.shape = shape;
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

    public Shape getShape() {
        return shape;
    }

    public Route getRoute() {
        return route;
    }

    public String getDirectionalId() {
        return directionalId;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public String getHeadSign() {
        return headSign;
    }

    public boolean isWheelChairAccess() {
        return wheelChairAccess;
    }

    public String getBlockId() {
        return blockId;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", service=" + (service != null ? service.getServiceId() : "null") +
                ", shape=" + (shape != null ? shape.getShapeId() : "null") +
                ", route=" + (route != null ? route.getRouteId() : "null") +
                ", directionalId='" + directionalId + '\'' +
                ", tripShortName='" + tripShortName + '\'' +
                ", headSign='" + headSign + '\'' +
                ", wheelChairAccess=" + wheelChairAccess +
                ", blockId='" + blockId + '\'' +
                '}';
    }
}
