public class Stop {
    String stopID;
    String stopName;
    double stopLat;
    double stopLon;
    String stopDesc;
    String stopURl;
    int locationType;
    String parentStation;
    String stopTimezone;
    int wheelchairBoarding;

    public Stop(String stopID, String stopName, double stopLat, double stopLon, String stopDesc, String stopURl, int locationType, String parentStation, String stopTimezone, int wheelchairBoarding) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopDesc = stopDesc;
        this.stopURl = stopURl;
        this.locationType = locationType;
        this.parentStation = parentStation;
        this.stopTimezone = stopTimezone;
        this.wheelchairBoarding = wheelchairBoarding;

    }

    public Stop(String stopID, String stopName, double stopLat, double stopLon) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    @Override
    public String toString() {
        return "Stop ID: " + stopID + '\n' + "Stop Name: " + stopName + '\n' + "Stop Latitude: " + stopLat + '\n' + "Stop Longitude: " + stopLon;
    }
}
