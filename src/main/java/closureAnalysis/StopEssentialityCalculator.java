package closureAnalysis;

import models.Stop;

import java.util.*;

public final class StopEssentialityCalculator {


    private String stopId;
    private String nearestStopId;
    private double nearestDistanceMetres;
    private double Es;

    public StopEssentialityCalculator(String stopId) {
        this.stopId = stopId;
        this.nearestStopId = null;
        this.nearestDistanceMetres = 9999999999999999999.0; // effectively infinity
        this.Es = 0;
    }

    public String getStopId(){ return stopId;}
    public double getNearestDistance(){ return nearestDistanceMetres;}
    public double getEs(){ return Es;}
    void setNearest(String id, double d){ nearestStopId = id; nearestDistanceMetres = d;}
    void setEs(double es){ Es = es;}


    private StopEssentialityCalculator() { /* util class */ }
    public static List<StopEssentialityCalculator> compute(List<Stop> stops) {
        if (stops == null)
            throw new IllegalArgumentException("Need at least two stops");
        List<StopEssentialityCalculator> essentialities = new ArrayList<>(stops.size());
        double maxDistance = 0.0;
        for (Stop stop1 : stops) {
            StopEssentialityCalculator essentiality = new StopEssentialityCalculator(stop1.getStopId());
            essentialities.add(essentiality);
            for(Stop stop2 : stops){
                if (stop1 == stop2) continue; // skip self-comparison
                double distance = haversineMetres(stop1.getStopLat(), stop1.getStopLon(),
                        stop2.getStopLat(), stop2.getStopLon());
                if (distance < essentiality.getNearestDistance()) {
                    essentiality.setNearest(stop2.getStopId(), distance);
                }
                if( distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        // Calculate essentiality score based on the nearest stop distance
        for(StopEssentialityCalculator ess : essentialities) {
            ess.setEs(ess.getNearestDistance()/ maxDistance);
        }
        return essentialities;
    }

    private static double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000;
        double f1 = Math.toRadians(lat1), φ2 = Math.toRadians(lat2);
        double df = φ2 - f1;
        double dl = Math.toRadians(lon2 - lon1);

        double a = Math.sin(df/2)*Math.sin(df/2)
                + Math.cos(f1)*Math.cos(φ2)
                * Math.sin(dl/2)*Math.sin(dl/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }
}
