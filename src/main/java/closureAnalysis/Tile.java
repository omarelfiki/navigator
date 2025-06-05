package closureAnalysis;

/**
 * One 1-km census square from the ISTAT 2021 grid.
 * <p>
 * All projected coordinates are in metres in EPSG : 3035.
 * <ul>
 *   <li>{@code minX,minY} – south-west (projected)</li>
 *   <li>{@code maxX,maxY} – north-east (projected)</li>
 *   <li>{@code centreLat, centreLon} – WGS-84 centre of the square</li>
 * </ul>
 */
public record Tile(
        String id,
        int    pop,
        double minX, double minY,
        double maxX, double maxY,
        double centreLat,
        double centreLon)
        implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** Width/height of the square (always 1000 m). */
    public double size() { return maxX - minX; }

    /** True if projected (x, y) lies inside this square. */
    public boolean containsXY(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /** Convenience: centre X in projected metres. */
    public double centreX() { return (minX + maxX) * 0.5; }

    /** Convenience: centre Y in projected metres. */
    public double centreY() { return (minY + maxY) * 0.5; }

    public int getPopulation() {
        return pop;
    }

    public double getCentreLat() {
        return centreLat;
    }

    public double getCentreLon() {
        return centreLon;
    }
}


