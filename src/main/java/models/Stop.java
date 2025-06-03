package models;

public class Stop {
   public String stopId;
   public String stopName;
   public double stopLat;
   public double stopLon;
   // Optional fields
   private String stopDesc;
   private String stopURl;
   private int locationType;
   private String parentStation;
   private String stopTimezone;
   private int wheelchairBoarding;

   // Fields for closure analysis
    private double fs;  // Frequency factor
    private int ps;  // Proximity to monument (0 or 1)
    private double es;  // Essentiality factor
    private double ds;     // Population density factor
    private double score; // Final combined score

    // Constructor with required + optional fields
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
    // Constructor with required fields
    public Stop(String stopId, String stopName, double stopLat, double stopLon) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    public Stop()
    {

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

    // Getters and Setters for closure analysis
    // Stop frequency
    public double getFs() { return fs; }
    public void setFs(double fs) { this.fs = fs; }

    // Proximity to monument
    public int getPs() { return ps; }
    public void setPs(int ps) { this.ps = ps; }

    // Stop essentiality
    public double getEs() { return es; }
    public void setEs(double es) { this.es = es; }

    // Stop population density
    public double getDs() { return ds; }
    public void setDs(double ds) { this.ds = this.ds; }

    // Score
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }


    @Override
    public String toString() {
        return "Stop ID: " + stopId + '\n' + "Stop Name: " + stopName + '\n' + "Stop Latitude: " + stopLat + '\n' + "Stop Longitude: " + stopLon;
    }
}
