package closureAnalysis;

import models.Stop;
import util.GeoUtil;

import java.util.*;

public final class StopEssentialityCalculator {
    private final String stopId;
    private double nearestDistanceMetres;
    private double Es;

    public StopEssentialityCalculator(String stopId) {
        this.stopId = stopId;
        this.nearestDistanceMetres = Double.MAX_VALUE; // effectively infinity
        this.Es = 0;
    }

    public String getStopId() { return stopId; }
    public double getNearestDistance() { return nearestDistanceMetres; }
    public double getEs() { return Es; }
    void setNearest(double d) { nearestDistanceMetres = d; }
    void setEs(double es) { Es = es; }

    public static List<StopEssentialityCalculator> compute(List<Stop> stops) {
        if (stops == null || stops.size() < 2)
            throw new IllegalArgumentException("Need at least two stops");

        List<StopEssentialityCalculator> essentialities = new ArrayList<>(stops.size());
        double maxNearest = 0.0;

        for (Stop s1 : stops) {
            StopEssentialityCalculator e = new StopEssentialityCalculator(s1.getStopId());
            essentialities.add(e);

            for (Stop s2 : stops) {
                if (s1 == s2) continue;
                if (s1.getStopLat() == s2.getStopLat() && s1.getStopLon() == s2.getStopLon()) continue;

                double d = GeoUtil.distance(s1.getStopLat(), s1.getStopLon(),
                        s2.getStopLat(), s2.getStopLon());

                if (d < e.getNearestDistance()) {
                    e.setNearest(d);
                }
            }

            if (e.getNearestDistance() > maxNearest) {
                maxNearest = e.getNearestDistance();
            }
        }

        final double denom = (maxNearest == 0) ? 1 : maxNearest;
        for (StopEssentialityCalculator e : essentialities) {
            e.setEs(e.getNearestDistance() / denom);

        }
        return essentialities;
    }
}
