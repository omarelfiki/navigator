package closureAnalysis;

import db.AllStops;
import models.Stop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RunResult {

    public static void main(String[] args) {

        // Access data
        ArrayList<Stop> allStops = AllStops.getAllStopsFromDatabase();
        List<TouristicLocations> monuments = ProximityFactor.getMonuments();

        // Calculate all scores
        FinalScore.calculateFinalScore(allStops, monuments);

        // Sort by score (lowest = best candidates for closure)
        allStops.sort(Comparator.comparingDouble(Stop::getScore));

        // Print top 3 stops with lowest scores
        System.out.println("Top 3 stops to consider for closure:");
        for (int i = 0; i < 3; i++) {
            Stop stop = allStops.get(i);
            System.out.printf("Stop ID: %s | Name: %s | Score: %.3f%n",
                    stop.getStopId(), stop.getStopName(), stop.getScore());
        }
    }
}
