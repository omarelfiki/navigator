package models;

public record Route(String routeId, Agency agency, String routeShortName, String routeLongName) {
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

