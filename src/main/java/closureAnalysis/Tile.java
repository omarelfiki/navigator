package closureAnalysis;

public record Tile(
        String id,
        int    pop,
        double minX, double minY,
        double maxX, double maxY,
        double centreLat,
        double centreLon)
        implements java.io.Serializable {
    public double size() { return maxX - minX; }
    public int getPopulation() {return pop;}
    public double getCentreLat() {return centreLat;}
    public double getCentreLon() {return centreLon;}
}


