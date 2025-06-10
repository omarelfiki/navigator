package closureAnalysis;

import models.Stop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public final class PopulationTile implements Serializable {
    private final double latNorth;
    private final double lonWest;
    private final double latSouth;
    private final double lonEast;
    List<Stop> stopsList;
    private final int population;
    public double popScore;
    public double stopScore;

    public PopulationTile(double latNorth, double lonWest, double latSouth, double lonEast,int population) {

        if (latSouth > latNorth)
            throw new IllegalArgumentException("latSouth must ≤ latNorth");
        if (lonWest > lonEast)
            throw new IllegalArgumentException("lonWest must ≤ lonEast");

        this.latNorth = latNorth;
        this.lonWest  = lonWest;
        this.latSouth = latSouth;
        this.lonEast  = lonEast;
        this.population = population;
        this.stopsList = new ArrayList<>();
        this.popScore = 0.0;
        this.stopScore = 0.0;

    }
    public int getPopulation() { return population; }

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
    public List<Stop> getStopsList() {
        return stopsList;
    }

}