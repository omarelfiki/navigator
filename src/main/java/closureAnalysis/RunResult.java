package closureAnalysis;

import db.TDSImplement;
import models.Stop;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RunResult {

    public static void main(String[] args) {
        // Access data
        TDSImplement tds = new TDSImplement();
        List<Stop> allStops = tds.getAllStops();
        Map<String, Integer> stopRouteCounts = tds.getStopRouteCounts();
        List<TouristicLocations> monuments = ProximityFactor.getMonuments();

        // Calculate all scores
        FinalScore.calculateFinalScore(allStops, monuments, stopRouteCounts);

        // Sort by score (lowest = best candidates for closure)
        allStops.sort(Comparator.comparingDouble(Stop::getScore));

        // Print top 3 stops with the lowest scores
        System.out.println("Top 3 stops to consider for closure:");
        for (int i = 0; i < 3; i++) {
            Stop stop = allStops.get(i);
            System.out.printf("Stop ID: %s | Name: %s | Score: %.3f%n",
                    stop.getStopId(), stop.getStopName(), stop.getScore());
        }
    }
}
