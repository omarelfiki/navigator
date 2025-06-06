package models;

import closureAnalysis.StopData;

public class Stop {
    private final String stopId;
    private final String stopName;
    private final double stopLat;
    private final double stopLon;

    public Stop(String stopId, String stopName, double stopLat, double stopLon) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    public Stop(){
        this("N/A", "Unknown Stop", 0.0, 0.0);
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

    @Override
    public String toString() {
        return "Stop ID: " + stopId + '\n' + "Stop Name: " + stopName + '\n' + "Stop Latitude: " + stopLat + '\n' + "Stop Longitude: " + stopLon;
    }
}
