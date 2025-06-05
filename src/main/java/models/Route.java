package models;

public record Route(String routeId, Agency agency, String routeShortName, String routeLongName) {
    public Route() {
        this("N/A", new Agency(), "N/A", "Unknown Route");
    }
    public Route {
        if (routeId == null || routeId.isEmpty()) {
            routeId = "N/A";
        }
        if (routeShortName == null || routeShortName.isEmpty()) {
            routeShortName = "N/A";
        }
        if (routeLongName == null || routeLongName.isEmpty()) {
            routeLongName = "Unknown";
        }
        if (agency == null) {
            agency = new Agency();
        }
    }
    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", agencyName='" + (agency != null ? agency.agencyId() : "null") + '\'' +
                ", routeShortName='" + routeShortName + '\'' +
                ", routeLongName='" + routeLongName + '\'' +
                '}';
    }
}

