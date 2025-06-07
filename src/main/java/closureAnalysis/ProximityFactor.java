package closureAnalysis;

import models.Stop;
import util.GeoUtil;

import java.util.ArrayList;
import java.util.List;

public class ProximityFactor {
    private String stopId;
    private double ps;

    public ProximityFactor(String stopId){
        this.stopId = stopId;
        this.ps = 0.0;
    }
    public String getStopId() {return stopId;}
    public void setPs(double ps) {this.ps = ps;}
    public double getPs() {return ps;}

    // Calculates proximity factor Ps for each stop
    public static List<ProximityFactor> calculateProximityFactor(List<Stop> allStops, List<TouristicLocations> monuments) {
        List<ProximityFactor> proximityFactors = new ArrayList<>();

        for (Stop stop : allStops) {
            ProximityFactor pf = new ProximityFactor(stop.getStopId());
            double stopLat = stop.getStopLat();
            double stopLon = stop.getStopLon();

            for (TouristicLocations monument : monuments) {
                double monumentLat = monument.lat();
                double monumentLon = monument.lon();

                double distance = GeoUtil.distance(stopLat, stopLon, monumentLat, monumentLon);

                if (distance <= 500) {
                    pf.setPs(1.0);
                    break;
                }
            }
        }
        return proximityFactors;
    }

}
