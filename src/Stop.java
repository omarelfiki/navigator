public class Stop {
    String stopId;
    String stopName;
    double stopLat;
    double stopLon;
    String stopDesc;
    String stopURl;
    int locationType;
    String parentStation;
    String stopTimezone;
    int wheelchairBoarding;

    public Stop(String stopId, String stopName, double stopLat, double stopLon, String stopDesc, String stopURl, int locationType, String parentStation, String stopTimezone, int wheelchairBoarding) {
        this.stopId = stopId;
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

    public Stop(String stopId, String stopName, double stopLat, double stopLon) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    public String getStopId() {
        return stopId;
    }
    public String getStopName() {
        return stopName;
    }
    public double getStopLat() {
        return stopLat;
    }
    public double getStopLon() {
        return stopLon;
    }
    public String getStopDesc() {
        return stopDesc;
    }
    public String getStopURl() {
        return stopURl;
    }
    public int getLocationType() {
        return locationType;
    }
    public String getParentStation() {
        return parentStation;
    }
    public String getStopTimezone() {
        return stopTimezone;
    }
    public int getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    @Override
    public String toString() {
        return "Stop ID: " + stopId + '\n' + "Stop Name: " + stopName + '\n' + "Stop Latitude: " + stopLat + '\n' + "Stop Longitude: " + stopLon;
    }
}
