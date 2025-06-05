package closureAnalysis;

import models.Stop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * GeoBox – an axis-aligned rectangle on the WGS-84 sphere
 * defined by its north-west and south-east corners, and the
 * total population currently assigned to that area.
 */
public final class PopulationTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double latNorth;
    private final double lonWest;
    private final double latSouth;
    private final double lonEast;
    List<Stop> stopsList;
    private int population;


    public PopulationTile(double latNorth, double lonWest, double latSouth, double lonEast,int population) {

        if (latSouth > latNorth)
            throw new IllegalArgumentException("latSouth must ≤ latNorth");
        if (lonWest > lonEast)
            throw new IllegalArgumentException("lonWest must ≤ lonEast");

        this.latNorth = latNorth;
        this.lonWest  = lonWest;
        this.latSouth = latSouth;
        this.lonEast  = lonEast;
        this.population = Math.max(population, 0);
        this.stopsList = new ArrayList<>();

    }

    public double getLatNorth()     { return latNorth; }
    public double getLonWest()      { return lonWest;  }
    public double getLatSouth()     { return latSouth; }
    public double getLonEast()      { return lonEast;  }
    public int    getPopulation()   { return population; }

    public boolean contains(double lat, double lon) {
        return lat <= latNorth && lat >= latSouth
                && lon >= lonWest  && lon <= lonEast;
    }
    public void addStop(Stop stop) {
        if (contains(stop.getStopLat(), stop.getStopLon())) {
            stopsList.add(stop);
        } else {
            throw new IllegalArgumentException("Stop is outside the tile boundaries");
        }
    }

}