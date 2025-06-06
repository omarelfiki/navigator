package closureAnalysis;

public class StopData {
    private double fs;  // Frequency factor
    private double ps;  // Proximity to monument (0 or 1)
    private double es;  // Essentiality factor
    private double ds;  // Population density factor
    private double score; // Final combined score
    private final String stopId; // Unique identifier for the stop
    public StopData(String stopId) {
        this.stopId = stopId;
        this.fs = 0.0;
        this.ps = 0.0;
        this.es = 0.0;
        this.ds = 0.0;
        this.score = 0.0; // Initialize score to 0
    }


    // Getters and Setters

    public double getFs() {return fs;}
    public void setFs(double fs) {this.fs = fs;}
    public double getPs() {return ps;}
    public void setPs(double ps) {this.ps = ps;}
    public double getEs() {return es;}
    public void setEs(double es) {this.es = es;}
    public double getDs() {return ds;}
    public void setDs(double ds) {this.ds = ds;}
    public double getScore() {return score;}
    public void setScore(double score) {this.score = score;}
    public String getStopId() {return stopId;}

}