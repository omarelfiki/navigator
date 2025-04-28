

public class Route {
    private String routeId;
    private Agency agency;
    private String routeShortName;
    private String routeUrl;
    private String routeType;

    // Optional fields
    private String routeLongName;
    private String routeColor;
    private String routeTextColor;

    // Constructor for required fields only
    public Route(String routeId, Agency agency, String routeShortName, String routeUrl, String routeType) {
        this.routeId = routeId;
        this.agency = agency;
        this.routeShortName = routeShortName;
        this.routeUrl = routeUrl;
        this.routeType = routeType;
    }

    //  Constructor for all fields (required + optional)
    public Route(String routeId, Agency agency, String routeShortName, String routeUrl, String routeType,
                 String routeLongName, String routeColor, String routeTextColor) {
        this.routeId = routeId;
        this.agency = agency;
        this.routeShortName = routeShortName;
        this.routeUrl = routeUrl;
        this.routeType = routeType;
        this.routeLongName = routeLongName;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;
    }

    public String getRouteId() {
        return routeId;
    }

    public Agency getAgency() {
        return agency;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteUrl() {
        return routeUrl;
    }

    public String getRouteType() {
        return routeType;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public String getRouteColor() {
        return routeColor;
    }

    public String getRouteTextColor() {
        return routeTextColor;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", agencyName='" + (agency != null ? agency.getAgencyId() : "null") + '\'' +
                ", routeShortName='" + routeShortName + '\'' +
                ", routeUrl='" + routeUrl + '\'' +
                ", routeType='" + routeType + '\'' +
                ", routeLongName='" + routeLongName + '\'' +
                ", routeColor='" + routeColor + '\'' +
                ", routeTextColor='" + routeTextColor + '\'' +
                '}';
    }
}

