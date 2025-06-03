package closureAnalysis;

public class StopData {
    private double fs;  // Frequency factor
    private int ps;  // Proximity to monument (0 or 1)
    private double es;  // Essentiality factor
    private double ds;  // Population density factor
    private double score; // Final combined score

    // Getters and Setters
    public double getFs() { return fs; }
    public void setFs(double fs) { this.fs = fs; }

    public int getPs() { return ps; }
    public void setPs(int ps) { this.ps = ps; }

    public double getEs() { return es; }
    public void setEs(double es) { this.es = es; }

    public double getDs() { return ds; }
    public void setDs(double ds) { this.ds = ds; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}