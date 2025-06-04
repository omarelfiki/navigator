package closureAnalysis;

import db.TDSImplement;
import models.Stop;

import java.util.List;
import java.util.Map;

public class FinalScore {

    private static double weightFs = 0.25; // Weight for stop frequency
    private static double weightPs = 0.20; // Weight for proximity to monument
    private static double weightEs = 0.35; // Weight for stop essentiality
    private static double weightDs = 0.20; // Weight for stop population density

    public static void calculateFinalScore(List<Stop> allStops, List<TouristicLocations> monuments, Map<String, Integer> stopRouteCounts) {

        // Calculate frequency factor Fs for each stop
        FrequencyFactor.calculateFrequencyFactor(allStops, stopRouteCounts);

        // Calculate proximity factor Ps for each stop
        ProximityFactor.calculateProximityFactor(allStops, monuments);

        for (Stop stop : allStops) {
            double fs = stop.getFs();
            int ps = stop.getPs();
            double es = stop.getEs();
            double ds = stop.getDs();

            double score = weightFs * fs + weightPs * ps + weightEs * es + weightDs * ds;
            stop.setScore(score);
        }
    }
}
