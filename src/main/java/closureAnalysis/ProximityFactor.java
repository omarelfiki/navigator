package closureAnalysis;

import models.Stop;
import util.GeoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProximityFactor {


    public static final List<TouristicLocations> monuments = Arrays.asList(
            new TouristicLocations("Colosseum", 41.890209, 12.492231),
            new TouristicLocations("Pantheon", 41.898610, 12.476872),
            new TouristicLocations("Roman Forum", 41.892465, 12.485324),
            new TouristicLocations("Piazza Navona", 41.8989, 12.4731),
            new TouristicLocations("Trevi Fountain", 41.900932, 12.483313),
            new TouristicLocations("Spanish Steps", 41.9057, 12.4823),
            new TouristicLocations("St. Peter's Basilica", 41.9022, 12.4539),
            new TouristicLocations("Castel Sant'Angelo", 41.9031, 12.4663),
            new TouristicLocations("Piazza Venezia & Altare della Patria", 41.8955, 12.4828),
            new TouristicLocations("Galleria Borghese & Villa Borghese", 41.9142, 12.4923),
            new TouristicLocations("Baths of Caracalla", 41.8796, 12.4930),
            new TouristicLocations("Palatine Hill", 41.8894, 12.4882),
            new TouristicLocations("Ostia Antica", 41.7550, 12.2930)
    );


    public static List<TouristicLocations> getMonuments() {
        return monuments;
    }

    // Calculates proximity factor Ps for each stop
    public static void calculateProximityFactor(ArrayList<Stop> allStops, List<TouristicLocations> monuments) {
        for (Stop stop : allStops) {
            double stopLat = stop.getStopLat();
            double stopLon = stop.getStopLon();

            boolean nearMonument = false;

            for (TouristicLocations monument : monuments) {
                double monumentLat = monument.getLat();
                double monumentLon = monument.getLon();

                double distance = GeoUtil.distance(stopLat, stopLon, monumentLat, monumentLon);

                if (distance <= 100) {
                    nearMonument = true;
                    break;
                }
            }

            stop.setPs(nearMonument ? 1 : 0);
        }
    }

}
